package io.github.tavstaldev.skyBlockCore.tasks;

import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A task that resets rewards (daily, weekly, and hourly) at their respective intervals.
 * This task checks the current time against the configured reset times and performs the necessary resets.
 */
public class RewardResetTask extends BukkitRunnable {

    /**
     * The main logic of the task, executed periodically.
     * Resets daily, weekly, and hourly rewards if the current time has passed their respective reset times.
     * Updates the configuration and database accordingly.
     */
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
            config.set("rewardReset.nextDailyReset", config.nextDailyReset.toString());
            database.resetDailyRewards();
        }

        // Reset weekly rewards
        if (now.isAfter(config.nextWeeklyReset)) {
            shouldSave = true;
            config.nextWeeklyReset = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
                    .plusDays(8 - now.getDayOfWeek().getValue());
            config.set("rewardReset.nextWeeklyReset", config.nextWeeklyReset.toString());
            database.resetWeeklyRewards();
        }

        // Reset hourly rewards
        if (now.isAfter(config.nextHourlyReset)) {
            shouldSave = true;
            config.nextHourlyReset = now.withMinute(0).withSecond(0).withNano(0).plusHours(1);
            config.set("rewardReset.nextHourlyReset", config.nextHourlyReset.toString());
            database.resetHourlyRewards();
        }

        // Save the configuration if any reset occurred
        if (shouldSave) {
            config.save();
        }
    }
}