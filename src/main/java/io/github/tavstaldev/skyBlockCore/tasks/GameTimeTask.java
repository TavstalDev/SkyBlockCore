package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameTimeTask extends BukkitRunnable {
    @Override
    public void run() {
        if (!SkyBlockCore.Config().gameTimeRewardEnabled) {
            // Game time rewards are disabled, cancel the task
            this.cancel();
            return;
        }

        Set<UUID> playersToBeRewarded = new HashSet<>();
        for (var playerId : PlayerCacheManager.getPlayersJoinTime().keySet()) {
            var joinTime = PlayerCacheManager.getJoinTime(playerId);
            if (joinTime == null)
                continue;

            var duration = Duration.between(joinTime, LocalDateTime.now()).abs();
            var seconds = duration.toSeconds();
            if (seconds < 1)
                continue;

            if (seconds % SkyBlockCore.Config().gameTimeRewardRequiredOnlineTime != 0)
                continue;

            playersToBeRewarded.add(playerId);
        }

        if (playersToBeRewarded.isEmpty())
            return;

        // Execute the reward command on the main thread
        Bukkit.getScheduler().runTask(SkyBlockCore.Instance, () -> {
            for (var playerId : playersToBeRewarded) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline())
                    return; // Player might have logged out between threads

                Bukkit.getServer().dispatchCommand(
                        Bukkit.getServer().getConsoleSender(),
                        SkyBlockCore.Config().gameTimeRewardCommand.replace("%player%", player.getName())
                );
            }
        });
    }
}