package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A task that manages the AFK pond rewards for players.
 * Periodically checks players in the AFK pond and executes reward commands based on their AFK time.
 */
public class AfkPondTask extends BukkitRunnable {

    /**
     * The main logic of the task, executed periodically.
     * Checks players in the AFK pond, calculates their AFK time, and executes reward commands if applicable.
     */
    @Override
    public void run() {
        // Retrieve the list of players currently in the AFK pond
        var playersInPond = PlayerCacheManager.getPlayersInAfkPond();
        if (playersInPond.isEmpty())
            return;

        // Retrieve the plugin configuration
        var config = SkyBlockCore.config();
        if (!config.afkPondEnabled) {
            // If AFK pond rewards are disabled, cancel the task
            this.cancel();
            return;
        }

        // Map to store commands to execute for each player
        Map<UUID, Set<String>> playerCommandsToExecute = new HashMap<>();
        for (var playerId : playersInPond.keySet()) {
            // Get the player's AFK start time
            var afkTime = PlayerCacheManager.getAfkTime(playerId);
            if (afkTime == null)
                continue;

            // Calculate the duration the player has been AFK
            var duration = Duration.between(afkTime, LocalDateTime.now()).abs();
            var minutes = duration.toMinutes();
            if (minutes < 1)
                continue;

            // Determine the reward commands to execute based on the player's AFK time
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
                // Validate the player's state before executing commands
                if (player == null || !player.isOnline() || player.isDead() || player.isFlying() || player.isInsideVehicle()) {
                    PlayerCacheManager.removeFromAfkPond(playerId);
                    return;
                }

                // Execute the commands for the player
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