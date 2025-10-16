package io.github.tavstaldev.skyBlockCore.util;

import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class TimeUtil
{
    public static String formatDuration(Player player, LocalDateTime date) {
        final PluginTranslator translator = SkyBlockCore.Instance.getTranslator();
        final long seconds = Duration.between(LocalDateTime.now(), date).abs().getSeconds();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(translator.localize(player, "Time.Days", Map.of("time", String.valueOf(days)))).append(" ");
        }
        if (hours > 0) {
            sb.append(translator.localize(player,"Time.Hours", Map.of("time",String.valueOf(hours)))).append(" ");
        }
        if (minutes > 0) {
            sb.append(translator.localize(player,"Time.Minutes", Map.of("time",String.valueOf(minutes)))).append(" ");
        }
        if (secs > 0) {
            sb.append(translator.localize(player,"Time.Seconds", Map.of("time",String.valueOf(secs)))).append(" ");
        }

        return sb.toString().trim();
    }
}
