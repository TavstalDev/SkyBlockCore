package io.github.tavstaldev.skyBlockCore;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import io.github.tavstaldev.skyBlockCore.models.config.AfkPondReward;

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

    // AfkPond
    public boolean afkPondEnabled;
    public long afkPondInterval;
    public HashSet<AfkPondReward> afkPondRewards;

    // GameTime reward
    public boolean gameTimeRewardEnabled;
    public long gameTimeRewardInterval;
    public long gameTimeRewardRequiredOnlineTime;
    public String gameTimeRewardCommand;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "hun");
        resolve("usePlayerLocale", false);
        checkForUpdates = resolveGet("checkForUpdates", false);
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

        // AfkPond
        afkPondEnabled = resolveGet("afkPond.enabled", true);
        afkPondInterval = resolveGet("afkPond.interval", 60);
        //#region Load AfkPond rewards
        afkPondRewards = new LinkedHashSet<>();
        // Default rewards
        if (get("afkPond.rewards") == null) {
            Map<String, Object> coinsReward = new LinkedHashMap<>(Map.of(
                    "interval", 900,
                    "command", "banyaszermeadmin give %player% 2")
            );
            Map<String, Object> keyReward = new LinkedHashMap<>(Map.of(
                    "interval", 3600,
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
                var interval = (long) map.get("interval");
                var command = (String) map.get("command");
                afkPondRewards.add(new AfkPondReward(interval, command));
            } catch (Exception ignored) {
                SkyBlockCore.Instance.getLogger().warning("Invalid afkPond reward configuration, skipping...");
            }
        }
        //#endregion

        // GameTime reward
        gameTimeRewardEnabled = resolveGet("gameTimeReward.enabled", true);
        gameTimeRewardInterval = resolveGet("gameTimeReward.interval", 60);
        gameTimeRewardRequiredOnlineTime = resolveGet("gameTimeReward.requiredOnlineTime", 300);
        gameTimeRewardCommand = resolveGet("gameTimeReward.command", "levelxp give %player% 1");
    }
}
