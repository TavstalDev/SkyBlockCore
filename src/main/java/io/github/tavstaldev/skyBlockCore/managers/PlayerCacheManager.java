package io.github.tavstaldev.skyBlockCore.managers;

import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

public class PlayerCacheManager {
    private static final Map<UUID, LocalDateTime> _playersInAfkPond = new HashMap<>();

    //#region Protections
    public static void addToAfkPond(UUID playerId, LocalDateTime time) {
        _playersInAfkPond.put(playerId, time);
    }

    public static void removeFromAfkPond(UUID playerId) {
        _playersInAfkPond.remove(playerId);
    }

    public static @Nullable LocalDateTime getAfkTime(UUID playerId) {
        return _playersInAfkPond.get(playerId);
    }

    public static Map<UUID, LocalDateTime> getPlayersInAfkPond() {
        return Collections.unmodifiableMap(_playersInAfkPond);
    }
    //#endregion
}