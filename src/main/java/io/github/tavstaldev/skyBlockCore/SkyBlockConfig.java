package io.github.tavstaldev.skyBlockCore;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;

public class SkyBlockConfig extends ConfigurationBase {
    public SkyBlockConfig() {
        super(SkyBlockCore.Instance, "config.yml", null);
    }

    public String prefix;
    public boolean checkForUpdates, debug;

    @Override
    protected void loadDefaults() {
        // General
        resolve("locale", "hun");
        resolve("usePlayerLocale", false);
        checkForUpdates = resolveGet("checkForUpdates", false);
        debug = resolveGet("debug", false);
        prefix = resolveGet("prefix", "&bSky&3Block &8»");
    }
}
