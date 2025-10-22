package io.github.tavstaldev.skyBlockCore.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.skyBlockCore.SkyBlockCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the /hourlyrewards command and its subcommands.
 * Provides functionality for managing hourly rewards, including help, claiming, and resetting rewards.
 */
public class CommandHourlyRewards implements CommandExecutor {
    @SuppressWarnings("FieldCanBeLocal")
    private final PluginLogger _logger = SkyBlockCore.logger().withModule(CommandHourlyRewards.class); // Logger for command-related operations.
    private final String baseCommand = "hourlyrewards"; // Base command name.
    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP subcommand
            add(new SubCommandData("help", "skyblockcore.commands.hourlyrewards.help", Map.of(
                    "syntax", "",
                    "description", "Commands.HourlyRewards.Help.Desc"
            )));
            // GET subcommand
            add(new SubCommandData("get", "skyblockcore.commands.hourlyrewards.get", Map.of(
                    "syntax", "Commands.HourlyRewards.Get.Syntax",
                    "description", "Commands.HourlyRewards.Get.Desc"
            )));
            // COMPLETE subcommand
            add(new SubCommandData("add", "skyblockcore.commands.hourlyrewards.complete", Map.of(
                    "syntax", "Commands.HourlyRewards.Complete.Syntax",
                    "description", "Commands.HourlyRewards.Complete.Desc"
            )));
            // RESET subcommand
            add(new SubCommandData("reset", "skyblockcore.commands.hourlyrewards.reset", Map.of(
                    "syntax", "Commands.HourlyRewards.Reset.Syntax",
                    "description", "Commands.HourlyRewards.Reset.Desc"
            )));
        }
    };

    /**
     * Initializes the /hourlyrewards command by setting its executor.
     * Logs an error if the command is not found in the plugin.yml file.
     */
    public CommandHourlyRewards() {
        var command = SkyBlockCore.Instance.getCommand(baseCommand);
        if (command == null) {
            _logger.error("Could not get command /" + baseCommand + " from plugin.yml! Disabling command...");
            return;
        }
        command.setExecutor(this);
    }

    /**
     * Handles the execution of the /hourlyrewards command and its subcommands.
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
                    // Check if the player has permission to use the help command
                    if (!sender.hasPermission("skyblockcore.commands.level.help")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    // Parse the page number for the help command
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(sender, page);
                    return true;
                }
                case "complete": {
                    // Check if the player has permission to use the complete command
                    if (!sender.hasPermission("skyblockcore.commands.hourlyrewards.complete")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setHourlyRewardClaimed(true);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.DailyRewards.Complete.Success", Map.of(
                                    "player", args[1]
                            )
                    );
                    return true;
                }
                case "reset": {
                    // Check if the player has permission to use the reset command
                    if (!sender.hasPermission("skyblockcore.commands.hourlyrewards.reset")) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
                        return true;
                    }

                    OfflinePlayer target = SkyBlockCore.Instance.getServer().getOfflinePlayer(args[1]);
                    if (!target.hasPlayedBefore() && !target.isOnline()) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidPlayer", Map.of("player", args[1]));
                        return true;
                    }

                    var playerData = SkyBlockCore.database().getPlayerData(target.getUniqueId()).orElse(null);
                    if (playerData == null) {
                        SkyBlockCore.Instance.sendCommandReply(sender, "General.NoPlayerData", Map.of("player", args[1]));
                        return true;
                    }

                    playerData.setHourlyRewardClaimed(false);
                    SkyBlockCore.database().updatePlayerData(playerData);
                    SkyBlockCore.Instance.sendCommandReply(sender, "Commands.DailyRewards.Reset.Success", Map.of(
                                    "player", args[1]
                            )
                    );
                    return true;
                }
            }

            // Send an error message if the subcommand is invalid
            SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Common.InvalidArguments");
            return true;
        }

        help(sender, 1);
        return true;
    }

    /**
     * Displays the help menu for the /hourlyrewards command.
     *
     * @param sender The sender of the command.
     * @param page   The page number of the help menu to display.
     */
    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        SkyBlockCore.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
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

        // Display navigation buttons for the help menu
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