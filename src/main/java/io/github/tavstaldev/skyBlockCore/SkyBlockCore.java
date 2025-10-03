package io.github.tavstaldev.skyBlockCore;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.github.tavstaldev.banyaszLib.api.BanyaszApi;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.skyBlockCore.commands.CommandLevel;
import io.github.tavstaldev.skyBlockCore.commands.CommandLevelXp;
import io.github.tavstaldev.skyBlockCore.commands.CommandSkyBlockCore;
import io.github.tavstaldev.skyBlockCore.database.IDatabase;
import io.github.tavstaldev.skyBlockCore.database.MySqlDatabase;
import io.github.tavstaldev.skyBlockCore.database.SqlLiteDatabase;
import io.github.tavstaldev.skyBlockCore.events.PlayerEventListener;
import io.github.tavstaldev.skyBlockCore.placeholders.SkyBlockExpansion;
import io.github.tavstaldev.skyBlockCore.tasks.AfkPondTask;
import org.bukkit.Bukkit;

public final class SkyBlockCore extends PluginBase {
    public static SkyBlockCore Instance;
    private IDatabase database;
    private AfkPondTask afkPondTask;
    private BanyaszApi banyaszApi;
    @SuppressWarnings("FieldCanBeLocal")
    private  SkyBlockExpansion skyBlockExpansion;
    //#region Public Accessors
    public static IDatabase Database() { return Instance.database; }
    public static BanyaszApi BanyaszApi() {
        return Instance.banyaszApi;
    }
    public static PluginLogger Logger() { return Instance.getCustomLogger();}
    public static SkyBlockConfig Config() { return (SkyBlockConfig) Instance.getConfig();}
    //#endregion
    public SkyBlockCore() {
        super(false, "https://github.com/TavstalDev/SkyBlockCore/releases/latest");
    }

    @Override
    public void onEnable() {
        Instance = this;
        _config = new SkyBlockConfig();
        _config.load();
        _translator = new PluginTranslator(this, new String[]{"hun"});
        _logger.info(String.format("Loading %s...", getProjectName()));

        // Check Minecraft version
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

        // Check for WorldGuard
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") == null) {
            _logger.error("WorldGuard plugin not found! This plugin requires WorldGuard to function properly. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        else {
            _logger.ok("WorldGuard plugin found and hooked into it.");
        }

        // Check BanyaszLib Plugin
        _logger.debug("Hooking into BanyaszLib...");
        if (!Bukkit.getPluginManager().isPluginEnabled("BanyaszLib")) {
            _logger.warn("BanyaszLib not found. Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            banyaszApi = BanyaszApi.getInstance();
            _logger.info("BanyaszLib found and hooked into it.");
        }

        // Load Localizations
        if (!_translator.load())
        {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Create Database
        String databaseType = Config().storageType;
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

        // Register Commands
        new CommandSkyBlockCore();
        new CommandLevel();
        new CommandLevelXp();

        // Register Events
        PlayerEventListener.init();

        // Register tasks
        if (afkPondTask != null && !afkPondTask.isCancelled())
            afkPondTask.cancel();
        afkPondTask = new AfkPondTask(); // Runs every 1 minute
        afkPondTask.runTaskTimer(this, 0, 60 * 20);

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));
    }

    @Override
    public void onDisable() {
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("afk-pond", true);
            registry.register(flag);
            SkyFlags.AfkPondFlag = flag;
        } catch (FlagConflictException e) {
            _logger.error("Failed to register flags! Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public void reload() {
        _logger.info("Reloading...");
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        this._config.load();
        this.database.update();
        _logger.debug("Configuration reloaded.");
    }
}
