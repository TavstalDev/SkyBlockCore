package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;

public class AfkPondTask extends BukkitRunnable {
    @Override
    public void run() {
        var playersAfking = PlayerCacheManager.getPlayersInAfkPond();
        if (playersAfking.isEmpty())
            return;

        for (var playerId : playersAfking.keySet()) {
            var afkTime = PlayerCacheManager.getAfkTime(playerId);
            if (afkTime == null)
                continue;

            var player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline() || player.isDead() || player.isFlying() || player.isInsideVehicle()) {
                PlayerCacheManager.removeFromAfkPond(playerId);
                continue;
            }

            var duration = Duration.between(afkTime, LocalDateTime.now()).abs();
            var minutes = duration.toMinutes();
            if (minutes < 1)
                continue;

            var server = Bukkit.getServer();
            if (minutes % 15 == 0) {
                server.dispatchCommand(server.getConsoleSender(), String.format("banyaszermeadmin give %s 2", player.getName()));
                server.dispatchCommand(server.getConsoleSender(), String.format("codex unlock %s objectives first_afk_reward", player.getName()));
            }
            if (minutes % 60 == 0) {
                server.dispatchCommand(server.getConsoleSender(), String.format("excellentcrates:crate key give %s common 1", player.getName()));
                server.dispatchCommand(server.getConsoleSender(), String.format("codex unlock %s objectives first_afk_reward", player.getName()));
            }
        }
    }
}
