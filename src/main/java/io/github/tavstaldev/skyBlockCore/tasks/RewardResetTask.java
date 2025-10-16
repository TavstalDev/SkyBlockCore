package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import org.bukkit.scheduler.BukkitRunnable;

public class RewardResetTask extends BukkitRunnable {
    @Override
    public void run() {
        final var now = java.time.LocalDateTime.now();
        final var database = SkyBlockCore.database();
        var config = SkyBlockCore.config();
        boolean shouldSave = false;
        // Reset daily rewards
        if (now.isAfter(config.nextDailyReset)) {
            shouldSave = true;
            config.nextDailyReset = now.withHour(0).withMinute(0).withSecond(0).withNano(0).plusDays(1);
            database.resetDailyRewards();
        }
        // Reset weekly rewards
        if (now.isAfter(config.nextWeeklyReset)) {
            shouldSave = true;
            config.nextWeeklyReset = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .plusDays(8 - now.getDayOfWeek().getValue());
            database.resetWeeklyRewards();
        }
        // Reset hourly rewards
        if (now.isAfter(config.nextHourlyReset)) {
            shouldSave = true;
            config.nextHourlyReset = now.withMinute(0).withSecond(0).withNano(0).plusHours(1);
            database.resetHourlyRewards();
        }

        if (shouldSave) {
            config.save();
        }
    }
}
