package io.github.tavstaldev.skyBlockCore;

import com.samjakob.spigui.SpiGUI;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.github.tavstaldev.banyaszLib.api.BanyaszApi;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.skyBlockCore.commands.*;
import io.github.tavstaldev.skyBlockCore.database.IDatabase;
import io.github.tavstaldev.skyBlockCore.database.MySqlDatabase;
import io.github.tavstaldev.skyBlockCore.database.SqlLiteDatabase;
import io.github.tavstaldev.skyBlockCore.events.PlayerEventListener;
import io.github.tavstaldev.skyBlockCore.placeholders.SkyBlockExpansion;
import io.github.tavstaldev.skyBlockCore.tasks.AfkPondTask;
import io.github.tavstaldev.skyBlockCore.tasks.GameTimeTask;
import io.github.tavstaldev.skyBlockCore.tasks.RewardResetTask;
import org.bukkit.Bukkit;

/**
 * Main class for the SkyBlockCore plugin.
 * Extends PluginBase to provide core plugin functionality.
 */
public final class SkyBlockCore extends PluginBase {
    public static SkyBlockCore Instance; // Singleton instance of the plugin
    private IDatabase database; // Database instance for storing plugin data
    private BanyaszApi banyaszApi; // API instance for interacting with BanyaszLib
    @SuppressWarnings("FieldCanBeLocal")
    private SkyBlockExpansion skyBlockExpansion; // PlaceholderAPI expansion for SkyBlockCore
    private AfkPondTask afkPondTask; // Task for managing AFK pond functionality
    private GameTimeTask gameTimeTask; // Task for managing game time rewards
    private RewardResetTask rewardResetTask; // Task for resetting rewards periodically
    private SpiGUI spiGUI; // SpiGUI instance for managing GUI interactions

    //#region Public Accessors
    /**
     * Provides access to the database instance.
     * @return The database instance.
     */
    public static IDatabase database() { return Instance.database; }

    /**
     * Provides access to the BanyaszApi instance.
     * @return The BanyaszApi instance.
     */
    public static BanyaszApi banyaszApi() {
        return Instance.banyaszApi;
    }

    /**
     * Provides access to the plugin logger.
     * @return The plugin logger instance.
     */
    public static PluginLogger logger() { return Instance.getCustomLogger(); }

    /**
     * Provides access to the plugin configuration.
     * @return The SkyBlockConfig instance.
     */
    public static SkyBlockConfig config() { return (SkyBlockConfig) Instance.getConfig(); }

    /**
     * Retrieves the SpiGUI instance.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI spiGui() { return Instance.spiGUI; }
    //#endregion

    /**
     * Constructor for the SkyBlockCore plugin.
     * Initializes the plugin with update checking disabled and a specified update URL.
     */
    public SkyBlockCore() {
        super(false, "https://github.com/TavstalDev/SkyBlockCore/releases/latest");
    }

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, checks dependencies, loads configurations, and starts tasks.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _config = new SkyBlockConfig();
        _config.load();
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.info(String.format("Loading %s...", getProjectName()));

        // Check Minecraft version compatibility
        if (VersionUtils.isLegacy()) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Hook into PlaceholderAPI
        if (!Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            _logger.error("PlaceholderAPI is not installed... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            skyBlockExpansion = new SkyBlockExpansion(this);
            skyBlockExpansion.register();
            _logger.ok("Found PlaceholderAPI and registered...");
        }

        // Check for WorldGuard plugin
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            _logger.error("WorldGuard plugin not found! This plugin requires WorldGuard to function properly. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            _logger.ok("WorldGuard plugin found and hooked into it.");
        }

        // Hook into BanyaszLib plugin
        _logger.debug("Hooking into BanyaszLib...");
        if (!Bukkit.getPluginManager().isPluginEnabled("BanyaszLib")) {
            _logger.warn("BanyaszLib not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            banyaszApi = BanyaszApi.getInstance();
            _logger.info("BanyaszLib found and hooked into it.");
        }

        // Load localizations
        if (!_translator.load()) {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize the database
        String databaseType = config().storageType;
        if (databaseType == null)
            databaseType = "sqlite";
        switch (databaseType.toLowerCase()) {
            case "mysql":
            case "mariadb": {
                database = new MySqlDatabase();
                break;
            }
            case "sqlite":
            default: {
                database = new SqlLiteDatabase();
                break;
            }
        }
        database.load();
        database.checkSchema();

        // Register commands
        new CommandSkyBlockCore();
        new CommandLevel();
        new CommandLevelXp();
        new CommandHourlyRewards();
        new CommandDailyRewards();
        new CommandWeeklyRewards();

        // Register event listeners
        PlayerEventListener.init();

        // Register tasks
        // AFK Pond task
        if (config().afkPondEnabled) {
            if (afkPondTask != null && !afkPondTask.isCancelled())
                afkPondTask.cancel();
            afkPondTask = new AfkPondTask();
            afkPondTask.runTaskTimerAsynchronously(this, 20 * 60, config().afkPondInterval * 20);
        }
        // GameTime task
        if (config().gameTimeRewardEnabled) {
            if (gameTimeTask != null && !gameTimeTask.isCancelled())
                gameTimeTask.cancel();
            gameTimeTask = new GameTimeTask();
            gameTimeTask.runTaskTimerAsynchronously(this, 20 * 60, config().gameTimeRewardInterval * 20);
        }
        // Reward reset task
        if (rewardResetTask != null && !rewardResetTask.isCancelled())
            rewardResetTask.cancel();
        rewardResetTask = new RewardResetTask();
        rewardResetTask.runTaskTimerAsynchronously(this, 20 * 60, 20 * 60 * 60); // Run every hour

        // Initialize SpiGUI
        _logger.debug("Initializing SpiGUI...");
        spiGUI = new SpiGUI(this);

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));
        if (config().checkForUpdates) {
            isUpToDate().thenAccept(upToDate -> {
                if (upToDate) {
                    _logger.ok("Plugin is up to date!");
                } else {
                    _logger.warn("A new version of the plugin is available: " + getDownloadUrl());
                }
            }).exceptionally(e -> {
                _logger.error("Failed to determine update status: " + e.getMessage());
                return null;
            });
        }
    }

    /**
     * Called when the plugin is disabled.
     * Logs the unloading of the plugin.
     */
    @Override
    public void onDisable() {
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Called when the plugin is loaded.
     * Registers custom WorldGuard flags.
     */
    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("afk-pond", true);
            if (registry.get("afk-pond") != null) {
                SkyFlags.AfkPondFlag = (StateFlag) registry.get("afk-pond");
                return;
            }
            registry.register(flag);
            SkyFlags.AfkPondFlag = flag;
        } catch (FlagConflictException e) {
            _logger.error("Failed to register flags...");
            Bukkit.getPluginManager().disablePlugin(this);
        } catch (IllegalStateException ex) {
            _logger.error("Failed to register flags...");
        }
    }

    /**
     * Reloads the plugin configuration and tasks.
     * Updates the database and restarts tasks based on the new configuration.
     */
    public void reload() {
        _logger.info("Reloading...");
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        this._config.load();
        this.database.update();

        // Update tasks
        // Step 1. Cancel existing tasks
        if (afkPondTask != null && !afkPondTask.isCancelled())
            afkPondTask.cancel();
        if (gameTimeTask != null && !gameTimeTask.isCancelled())
            gameTimeTask.cancel();
        // Step 2. Start tasks if enabled
        if (config().afkPondEnabled) {
            afkPondTask = new AfkPondTask();
            afkPondTask.runTaskTimerAsynchronously(this, 0, config().afkPondInterval * 20);
        }
        if (config().gameTimeRewardEnabled) {
            gameTimeTask = new GameTimeTask();
            gameTimeTask.runTaskTimerAsynchronously(this, 0, config().gameTimeRewardInterval * 20);
        }
        if (rewardResetTask != null && !rewardResetTask.isCancelled())
            rewardResetTask.cancel();
        rewardResetTask = new RewardResetTask();
        rewardResetTask.runTaskTimerAsynchronously(this, 0, 20 * 60 * 60); // Run every hour

        _logger.debug("Configuration reloaded.");
    }
}