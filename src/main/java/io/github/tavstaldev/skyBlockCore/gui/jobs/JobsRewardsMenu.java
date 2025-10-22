package io.github.tavstaldev.skyBlockCore.gui.jobs;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// Please read the note in the main menu
public class JobsRewardsMenu {
    private static final PluginLogger _logger = SkyBlockCore.logger().withModule(JobsRewardsMenu.class);
    private static final PluginTranslator _translator = SkyBlockCore.Instance.getTranslator();
    private static final Integer itemsPerPage = 28; // Maximum number of items per page
    private static final Integer rows = 6; // Number of rows in the GUI

    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = SkyBlockCore.spiGui().create(_translator.localize(player, "..."), rows);

            // Create placeholders for empty slots in the GUI.
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(SkyBlockCore.Instance, Material.GRAY_STAINED_GLASS_PANE, " "));
            int slots = rows * 9;
            for (int i = 0; i < slots; i++) {
                menu.setButton(0, i, placeholderButton);
            }

            // Add the close button to the GUI.
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(SkyBlockCore.Instance, Material.BARRIER, _translator.localize(player, "GUI.Close"))
            ).withListener(event -> close(player));
            menu.setButton(0, 54, closeButton);

            return menu;
        }
        catch (Exception ex) {
            _logger.error("Failed to create Jobs Rewards Menu for player " + player.getName());
            _logger.error(ex);
            return null;
        }
    }

    public static void open(@NotNull Player player) {
        //var playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI.
        /*playerCache.setGuiOpened(true);
        player.openInventory(playerCache.getMainMenu().getInventory());
        refresh(player);*/
    }

    public static void close(@NotNull Player player) {
        /*var playerCache = PlayerCacheManager.get(player.getUniqueId());
        playerCache.setGuiOpened(false);*/
        player.closeInventory();
    }

    public static void refresh(@NotNull Player player) {
        try {
            var playerId = player.getUniqueId();
            /*var playerCache = PlayerCacheManager.get(playerId);
            var menu = playerCache.getMainMenu();*/
            // TODO: Implement buttons and pagination logic here.
        } catch (Exception ex) {
            _logger.error("Failed to refresh Jobs Rewards Menu for player " + player.getName());
            _logger.error(ex);
        }
    }
}
