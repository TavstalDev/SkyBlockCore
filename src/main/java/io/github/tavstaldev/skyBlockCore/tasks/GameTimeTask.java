package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A task that rewards players based on their total game time.
 * Periodically checks the online duration of players and executes a reward command if conditions are met.
 */
public class GameTimeTask extends BukkitRunnable {

    /**
     * The main logic of the task, executed periodically.
     * Checks the online duration of players and rewards them if they meet the required game time.
     */
    @Override
    public void run() {
        // Check if game time rewards are enabled in the configuration
        var config = SkyBlockCore.config();
        if (!config.gameTimeRewardEnabled) {
            // Game time rewards are disabled, cancel the task
            this.cancel();
            return;
        }

        // Set to store players eligible for rewards
        Set<UUID> playersToBeRewarded = new HashSet<>();
        final var now = LocalDateTime.now();
        for (var playerId : PlayerCacheManager.getPlayersJoinTime().keySet()) {
            // Retrieve the player's join time
            var joinTime = PlayerCacheManager.getJoinTime(playerId);
            if (joinTime == null)
                continue;

            // Calculate the duration the player has been online
            if (now.isBefore(joinTime))
                continue;

            // Add the player to the reward list
            playersToBeRewarded.add(playerId);
            PlayerCacheManager.addJoinTime(playerId, now.plusMinutes(config.gameTimeRewardRequiredOnlineTime));
        }

        // If no players are eligible for rewards, exit
        if (playersToBeRewarded.isEmpty())
            return;

        // Execute the reward command on the main thread
        Bukkit.getScheduler().runTask(SkyBlockCore.Instance, () -> {
            for (var playerId : playersToBeRewarded) {
                // Retrieve the player object
                Player player = Bukkit.getPlayer(playerId);
                if (player == null || !player.isOnline())
                    continue; // Player might have logged out between threads

                // Execute the reward command for the player
                Bukkit.getServer().dispatchCommand(
                        Bukkit.getServer().getConsoleSender(),
                        SkyBlockCore.config().gameTimeRewardCommand.replace("%player%", player.getName())
                );
            }
        });
    }
}