package io.github.tavstaldev.skyBlockCore.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the /sbc command and its subcommands.
 * Provides functionality for displaying help, checking the plugin version, and reloading the plugin configuration.
 */
public class CommandSkyBlockCore implements CommandExecutor {
    private final PluginLogger _logger = SkyBlockCore.logger().withModule(CommandSkyBlockCore.class); // Logger for command-related operations.
    private final String baseCommand = "sbc"; // Base command name.
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP subcommand
            add(new SubCommandData("help", "skyblockcore.commands.help", Map.of(
                    "syntax", "",
                    "description", "Commands.Help.Desc"
            )));
            // VERSION subcommand
            add(new SubCommandData("version", "skyblockcore.commands.version", Map.of(
                    "syntax", "",
                    "description", "Commands.Version.Desc"
            )));
            // RELOAD subcommand
            add(new SubCommandData("reload", "skyblockcore.commands.reload", Map.of(
                    "syntax", "",
                    "description", "Commands.Reload.Desc"
            )));
        }
    };

    /**
     * Initializes the /sbc command by setting its executor.
     * Logs an error if the command is not found in the plugin.yml file.
     */
    public CommandSkyBlockCore() {
        var command = SkyBlockCore.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the /sbc command and its subcommands.
     *
     * @param sender  The sender of the command (player or console).
     * @param command The command being executed.
     * @param label   The alias of the command used.
     * @param args    The arguments provided with the command.
     * @return True if the command was successfully executed, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        // Handle subcommands based on the first argument
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    // Handle the help subcommand
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    if (!player.hasPermission("skyblockcore.commands.help")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    // Handle the version subcommand
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    if (!player.hasPermission("skyblockcore.commands.version")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("version", SkyBlockCore.Instance.getVersion());
                    SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.Current", parameters);

                    SkyBlockCore.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.UpToDate");
                        } else {
                            SkyBlockCore.Instance.sendCommandReply(player, "Commands.Version.Outdated", Map.of("link", SkyBlockCore.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    // Handle the reload subcommand
                    if (sender instanceof ConsoleCommandSender) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.NotPlayer");
                        return true;
                    }
                    Player player = (Player) sender;

                    if (!player.hasPermission("skyblockcore.commands.reload")) {
                        SkyBlockCore.Instance.sendCommandReply(player, "General.NoPermission");
                        return true;
                    }

                    SkyBlockCore.Instance.reload();
                    SkyBlockCore.Instance.sendCommandReply(player, "Commands.Reload.Done");
                    return true;
                }
            }

            // Handle invalid subcommands
            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
            return true;
        }

        // Default to the help command if no arguments are provided
        if (!sender.hasPermission("skyblockcore.commands.help")) {
            SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
            return true;
        }
        help(sender, 1);
        return true;
    }

    /**
     * Displays the help menu for the /sbc command.
     *
     * @param sender The sender of the command.
     * @param page   The page number of the help menu to display.
     */
    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(SkyBlockCore.Instance, sender, baseCommand);
        }

        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player) {
            previousBtn = SkyBlockCore.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = SkyBlockCore.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = SkyBlockCore.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        } else {
            previousBtn = SkyBlockCore.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = SkyBlockCore.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = SkyBlockCore.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", String.valueOf(page),
                    "max_page", String.valueOf(maxPage))
            );
        }

        Map<String, Component> bottomParams = new HashMap<>();
        bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                .clickEvent(page > 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1)) : null));
        bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                .clickEvent(!reachedEnd && maxPage >= page + 1 ? ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1)) : null));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }
}