package io.github.tavstaldev.skyBlockCore;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.skyBlockCore.models.config.AfkPondReward;

import java.time.LocalDateTime;
import java.util.*;

public class SkyBlockConfig extends ConfigurationBase {
    public SkyBlockConfig() {
        super(SkyBlockCore.Instance, "config.yml", null);
    }

    // General
    public String prefix;
    public boolean checkForUpdates, debug;

    // Storage
    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    // Reward Reset
    public LocalDateTime nextDailyReset, nextWeeklyReset, nextHourlyReset;

    // AfkPond
    public boolean afkPondEnabled;
    public HashSet<AfkPondReward> afkPondRewards;

    // GameTime reward
    public boolean gameTimeRewardEnabled;
    public long gameTimeRewardRequiredOnlineTime;
    public String gameTimeRewardCommand;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "eng");
        resolve("usePlayerLocale", true);
        checkForUpdates = resolveGet("checkForUpdates", true);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bSky&3Block &8»");

        // Storage
        storageType = resolveGet("storage.type", "sqlite");
        storageFilename = resolveGet("storage.filename", "database");
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "sbc");

        // Reward Reset
        final var date = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        nextDailyReset = LocalDateTime.parse(resolveGet("rewardReset.nextDailyReset", date.plusDays(1).toString()));
        nextWeeklyReset = LocalDateTime.parse(resolveGet("rewardReset.nextWeeklyReset", date.plusDays(8 - date.getDayOfWeek().getValue()).toString()));
        nextHourlyReset = LocalDateTime.parse(resolveGet("rewardReset.nextHourlyReset", date.plusHours(1).toString()));

        // AfkPond
        afkPondEnabled = resolveGet("afkPond.enabled", true);
        //#region Load AfkPond rewards
        afkPondRewards = new LinkedHashSet<>();
        // Default rewards
        if (get("afkPond.rewards") == null) {
            Map<String, Object> coinsReward = new LinkedHashMap<>(Map.of(
                    "interval", 5,
                    "command", "banyaszermeadmin give %player% 2")
            );
            Map<String, Object> keyReward = new LinkedHashMap<>(Map.of(
                    "interval", 60,
                    "command", "excellentcrates:crate key give %player% common 1")
            );
            List<Map<String, Object>> rewardsList = new ArrayList<>() {
                {
                    add(coinsReward);
                    add(keyReward);
                }
            };
            resolve("afkPond.rewards", rewardsList);
        }
        var rawRewards = resolveGet("afkPond.rewards", new ArrayList<>());
        for (var rawReward : rawRewards) {
            //noinspection PatternVariableHidesField
            if (!(rawReward instanceof HashMap<?, ?> map))
                continue;
            try {
                var interval = (Integer) map.get("interval");
                var command = (String) map.get("command");
                afkPondRewards.add(new AfkPondReward(interval, command));
            } catch (Exception ex) {
                SkyBlockCore.Instance.getLogger().warning("Invalid afkPond reward configuration: \n" + ex);
            }
        }
        resolveComment("afkPond.rewards", List.of("List of rewards to give to players in the afk pond", "interval: Interval in minutes to give the reward", "command: Command to execute, %player% will be replaced with the player's name"));
        //#endregion

        // GameTime reward
        gameTimeRewardEnabled = resolveGet("gameTimeReward.enabled", true);
        gameTimeRewardRequiredOnlineTime = resolveGet("gameTimeReward.requiredOnlineTime", 60);
        resolveComment("gameTimeReward.requiredOnlineTime", List.of("Required online time in minutes to get the reward"));
        gameTimeRewardCommand = resolveGet("gameTimeReward.command", "levelxp add %player% 1");
    }
}
