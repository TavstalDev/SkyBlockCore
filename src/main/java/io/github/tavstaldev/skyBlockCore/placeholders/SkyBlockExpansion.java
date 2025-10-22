package io.github.tavstaldev.skyBlockCore.placeholders;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * A custom PlaceholderAPI expansion for the SkyBlockCore plugin.
 * Provides placeholders for various player and plugin-related data.
 */
public class SkyBlockExpansion extends PlaceholderExpansion {
    private final SkyBlockCore plugin;

    /**
     * Constructs a new SkyBlockExpansion instance.
     *
     * @param plugin The main instance of the SkyBlockCore plugin.
     */
    public SkyBlockExpansion(SkyBlockCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the author(s) of the plugin.
     *
     * @return A comma-separated string of the plugin authors.
     */
    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    /**
     * Gets the identifier for this PlaceholderAPI expansion.
     *
     * @return The identifier string for the expansion.
     */
    @Override
    @NotNull
    public String getIdentifier() {
        return "skyblockcore";
    }

    /**
     * Gets the version of the plugin.
     *
     * @return The version string of the plugin.
     */
    @Override
    @NotNull
    public String getVersion() {
        return plugin.getVersion();
    }

    /**
     * Determines whether this expansion should persist across server reloads.
     *
     * @return True if the expansion should persist, false otherwise.
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * Handles placeholder requests and returns the corresponding value.
     *
     * @param player The player for whom the placeholder is being requested.
     * @param params The placeholder parameter string.
     * @return The value of the requested placeholder, or null if not found.
     */
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        var playerData = SkyBlockCore.database().getPlayerData(player.getUniqueId());
        if (playerData.isEmpty())
            return "0";
        var data = playerData.get();
        //noinspection ConstantValue
        if (data == null)
            return "0";
        final var config = SkyBlockCore.config();
        return switch (params.toLowerCase()) {
            case "level" -> String.valueOf(data.getLevel());
            case "experience" -> String.valueOf(data.getExperience());
            case "factories", "factory-count" -> String.valueOf(data.getFactories());
            case "completedfactories", "factory-completed" -> String.valueOf(data.getCompletedFactories());
            case "maxfactories", "factory-max-ongoing" -> String.valueOf(data.getMaxFactories());
            case "ongoingfactories", "factory-ongoing" -> String.valueOf(data.getOngoingFactories());
            case "factoryresearch", "factory-research" -> String.valueOf(data.getFactoryResearch());
            case "daily-reward-claimed" -> data.isDailyRewardClaimed() ? "yes" : "no";
            case "weekly-reward-claimed" -> data.isWeeklyRewardClaimed() ? "yes" : "no";
            case "hourly-reward-claimed" -> data.isHourlyRewardClaimed() ? "yes" : "no";
            case "daily-reset" -> TimeUtil.formatDuration(player.getPlayer(), config.nextDailyReset);
            case "weekly-reset" -> TimeUtil.formatDuration(player.getPlayer(), config.nextWeeklyReset);
            case "hourly-reset" -> TimeUtil.formatDuration(player.getPlayer(), config.nextHourlyReset);
            default -> null;
        };
    }
}