package io.github.tavstaldev.skyBlockCore;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import io.github.tavstaldev.banyaszLib.api.BanyaszApi;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.VersionUtils;
import io.github.tavstaldev.skyBlockCore.events.PlayerEventListener;
import io.github.tavstaldev.skyBlockCore.tasks.AfkPondTask;
import org.bukkit.Bukkit;

public final class SkyBlockCore extends PluginBase {
    public static SkyBlockCore Instance;
    private AfkPondTask afkPondTask;
    private BanyaszApi _banyaszApi;
    public BanyaszApi BanyaszApi() {
        return _banyaszApi;
    }

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

        if (VersionUtils.isLegacy()) {
            _logger.error("The plugin is not compatible with legacy versions of Minecraft. Please use a newer version of the game.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
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
            _banyaszApi = BanyaszApi.getInstance();
            _logger.info("BanyaszLib found and hooked into it.");
        }

        // Load Localizations
        if (!_translator.load())
        {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
}
