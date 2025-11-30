package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import io.github.tavstaldev.skyBlockCore.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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

        final var now = LocalDateTime.now();
        final var rewards = config.afkPondRewards;
        Map<UUID, Set<String>> playerCommandsToExecute = new HashMap<>();
        for (var playerId : playersInPond.keySet()) {
            for (var reward : rewards) {
                var nextRewardTime = PlayerCacheManager.getAfkRewardTime(playerId, reward.command);
                if (nextRewardTime == null || now.isBefore(nextRewardTime))
                    continue;

                PlayerCacheManager.addAfkRewardTime(playerId, reward.command, now.plusMinutes(reward.interval));
                if (playerCommandsToExecute.containsKey(playerId)) {
                    playerCommandsToExecute.get(playerId).add(reward.command);
                } else {
                    Set<String> commands = new HashSet<>();
                    commands.add(reward.command);
                    playerCommandsToExecute.put(playerId, commands);
                }
            }
        }

        // Execute the reward commands on the main thread
        Bukkit.getScheduler().runTask(SkyBlockCore.Instance, () -> {
            var server = Bukkit.getServer();
            var console = server.getConsoleSender();
            for (var entry : playersInPond.entrySet()) {
                var playerId = entry.getKey();
                Player player = Bukkit.getPlayer(playerId);
                // Validate the player's state before executing commands
                if (player == null || !player.isOnline() || player.isDead() || player.isFlying() || player.isInsideVehicle()) {
                    PlayerCacheManager.removeFromAfkPond(playerId);
                    continue;
                }
                String time = SkyBlockCore.Instance.getTranslator().localize(player, "AfkPond.ActionBar", Map.of(
                        "time", TimeUtil.formatDate(player, entry.getValue())
                ));
                player.sendActionBar(ChatUtils.translateColors(time, true));

                var commandsToRun = playerCommandsToExecute.get(playerId);
                if (commandsToRun == null || commandsToRun.isEmpty())
                    continue;
                for (var command : commandsToRun) {
                    server.dispatchCommand(console, command.replace("%player%", player.getName()));
                }
            }
        });
    }
}