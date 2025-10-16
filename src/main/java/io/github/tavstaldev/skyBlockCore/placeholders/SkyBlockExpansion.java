package io.github.tavstaldev.skyBlockCore.placeholders;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.util.TimeUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class SkyBlockExpansion extends PlaceholderExpansion {
    private final SkyBlockCore plugin;

    public SkyBlockExpansion(SkyBlockCore plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "skyblockcore";
    }

    @Override
    @NotNull
    public String getVersion() {
        return plugin.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

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
            case "daily-reset" -> TimeUtil.formatDuration(player.getPlayer(),config.nextDailyReset);
            case "weekly-reset" -> TimeUtil.formatDuration(player.getPlayer(), config.nextWeeklyReset);
            case "hourly-reset" -> TimeUtil.formatDuration(player.getPlayer(), config.nextHourlyReset);
            default -> null;
        };
    }
}
