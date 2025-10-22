package io.github.tavstaldev.skyBlockCore;

import com.sk89q.worldguard.protection.flags.StateFlag;

/**
 * A utility class for managing custom WorldGuard flags used in the SkyBlockCore plugin.
 */
public class SkyFlags {
    /**
     * A custom WorldGuard flag to determine if the AFK pond feature is enabled.
     * This flag is registered during the plugin's loading phase.
     */
    public static StateFlag AfkPondFlag;
}