package io.github.tavstaldev.skyBlockCore.placeholders;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
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
        var playerData = plugin.Database().getPlayerData(player.getUniqueId());
        if (playerData.isEmpty())
            return "0";
        var data = playerData.get();
        //noinspection ConstantValue
        if (data == null)
            return "0";
        return switch (params.toLowerCase()) {
            case "level" -> String.valueOf(data.getLevel());
            case "experience" -> String.valueOf(data.getExperience());
            case "factories" -> String.valueOf(data.getFactories());
            case "completedfactories" -> String.valueOf(data.getCompletedFactories());
            case "maxfactories" -> String.valueOf(data.getMaxFactories());
            case "ongoingfactories" -> String.valueOf(data.getOngoingFactories());
            case "factoryresearch" -> String.valueOf(data.getFactoryResearch());
            default -> null;
        };
    }
}
