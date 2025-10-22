package io.github.tavstaldev.skyBlockCore.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import io.github.tavstaldev.skyBlockCore.SkyFlags;
import io.github.tavstaldev.skyBlockCore.managers.PlayerCacheManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.time.LocalDateTime;

/**
 * Handles player-related events such as movement, joining, and quitting.
 * Manages AFK pond interactions and player data caching.
 */
public class PlayerEventListener implements Listener {

    /**
     * Initializes the event listener by registering it with the plugin manager.
     */
    public static void init() {
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), SkyBlockCore.Instance);
    }

    /**
     * Handles the PlayerMoveEvent to manage AFK pond entry and exit.
     * Sends localized messages to players when they enter or leave the AFK pond.
     *
     * @param event The PlayerMoveEvent triggered when a player moves.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!SkyBlockCore.config().afkPondEnabled)
            return;

        Player player = event.getPlayer();
        // Ignore if the player is in creative or spectator mode
        if (!(player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SURVIVAL))
            return;

        var playerId = player.getUniqueId();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        ApplicableRegionSet toRegions = query.getApplicableRegions(BukkitAdapter.adapt(event.getTo()));
        var afkTime = PlayerCacheManager.getAfkTime(playerId);
        if (toRegions.getRegions().stream().anyMatch(region -> region.getFlag(SkyFlags.AfkPondFlag) == StateFlag.State.ALLOW)) {
            if (afkTime != null)
                return;
            PlayerCacheManager.addToAfkPond(playerId, LocalDateTime.now());
            SkyBlockCore.Instance.sendLocalizedMsg(player, "AfkPond.Enter");
            return;
        }
        if (afkTime != null) {
            PlayerCacheManager.removeFromAfkPond(playerId);
            SkyBlockCore.Instance.sendLocalizedMsg(player, "AfkPond.Exit");
        }
    }

    /**
     * Handles the PlayerJoinEvent to initialize player data and cache their join time.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        if (SkyBlockCore.database().getPlayerData(playerId).isEmpty())
            SkyBlockCore.database().addPlayerData(playerId);

        PlayerCacheManager.addJoinTime(playerId, LocalDateTime.now());
    }

    /**
     * Handles the PlayerQuitEvent to clean up player data from caches.
     * Removes players from the AFK pond and clears their join time.
     *
     * @param event The PlayerQuitEvent triggered when a player leaves the server.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        var afkTime = PlayerCacheManager.getAfkTime(playerId);
        if (afkTime != null)
            PlayerCacheManager.removeFromAfkPond(playerId);

        PlayerCacheManager.removeJoinTime(playerId);
    }
}