package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class AfkPondTask extends BukkitRunnable {
    @Override
    public void run() {
        var playersAfking = PlayerCacheManager.getPlayersInAfkPond();
        if (playersAfking.isEmpty())
            return;

        var config = SkyBlockCore.config();
        if (!config.afkPondEnabled) {
            // Afk pond rewards are disabled, cancel the task
            this.cancel();
            return;
        }

        Map<UUID, Set<String>> playerCommandsToExecute = new HashMap<>();
        for (var playerId : playersAfking.keySet()) {
            var afkTime = PlayerCacheManager.getAfkTime(playerId);
            if (afkTime == null)
                continue;

            var duration = Duration.between(afkTime, LocalDateTime.now()).abs();
            var minutes = duration.toMinutes();
            if (minutes < 1)
                continue;

            Set<String> commandsToRun = new HashSet<>();
            for (var reward : config.afkPondRewards) {
                if (minutes % reward.interval != 0)
                    continue;

                commandsToRun.add(reward.command);
            }
            if (!commandsToRun.isEmpty())
                playerCommandsToExecute.put(playerId, commandsToRun);
        }

        if (playerCommandsToExecute.isEmpty())
            return;

        // Execute the reward commands on the main thread
        Bukkit.getScheduler().runTask(SkyBlockCore.Instance, () -> {
            for (var entry : playerCommandsToExecute.entrySet()) {
                var playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline() || player.isDead() || player.isFlying() || player.isInsideVehicle()) {
                    PlayerCacheManager.removeFromAfkPond(playerId);
                    return;
                }

                var server = Bukkit.getServer();
                var console = server.getConsoleSender();
                var commandsToRun = entry.getValue();
                for (var command : commandsToRun) {
                    server.dispatchCommand(console, command.replace("%player%", player.getName()));
                }
            }
        });
    }
}
