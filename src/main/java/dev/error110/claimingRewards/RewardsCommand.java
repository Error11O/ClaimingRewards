package dev.error110.claimingRewards;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RewardsCommand extends BaseCommand implements TabExecutor {

    public RewardsCommand() {
        AddonCommand addonCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "rewards", this);
        TownyCommandAddonAPI.addSubCommand(addonCommand);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of("claim", "list");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // checks if the sender is a player and has permission to use the command
        if (sender instanceof Player player) {
            if (!player.hasPermission("claimingrewards.collectrewards")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
                return true;
            }
            // Check that at least one argument is provided
            if (args.length <= 0) {
                sender.sendMessage(ChatColor.RED + "Please specify a subcommand (claim or list).");
                return true;
            }
            // checks if the command is valid
            switch (args[0].toLowerCase()) {
                case "claim": {
                    ClaimingRewards plugin = ClaimingRewards.getInstance();
                    Town town = TownyAPI.getInstance().getTown(player);
                    if (town == null) {
                        sender.sendMessage(ChatColor.RED + "You are not in a town.");
                        return true;
                    }

                    Map<UUID, Integer> allTownRewards = plugin.getAllTownRewards();
                    int rewards = 0;
                    if (allTownRewards.containsKey(town.getUUID())) {
                        rewards = plugin.getRewardForTown(town);
                    }
                    if (rewards <= 0) {
                        sender.sendMessage(ChatColor.RED + "You do not have any rewards to claim!");
                        return true;
                    }
                    giveReward(player, rewards);
                    plugin.removeRewardForTown(town);
                    return true;
                }
                case "list": {
                    ClaimingRewards plugin = ClaimingRewards.getInstance();
                    List<Map.Entry<Town, Integer>> sortedTowns = plugin.SortTownToClaimsList();

                    sender.sendMessage(ChatColor.YELLOW + "oOo_______[" + ChatColor.GOLD + " Claim Rewards Leaderboard " + ChatColor.YELLOW + "]_______oOo");
                    sender.sendMessage(ChatColor.DARK_AQUA + "Towns Eligible For Rewards:");
                    for (int i = 0; i < Math.min(sortedTowns.size(), plugin.getConfig().getInt("towns-rewarded-count")); i++) {
                        Map.Entry<Town, Integer> entry = sortedTowns.get(i);

                        String rewardKey = switch (i) {
                            case 0 -> "first-total-given";
                            case 1 -> "second-total-given";
                            case 2 -> "third-total-given";
                            default -> "default-total-given";
                        };
                        //done
                        sender.sendMessage(ChatColor.AQUA + entry.getKey().getName() + ChatColor.AQUA + " (" + entry.getValue() + " Claims" + ")" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + plugin.getConfig().getInt(rewardKey) + " Rewards/Day");
                    }
                    return true;
                }
                default:
                    sender.sendMessage(ChatColor.RED + "Invalid subcommand");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command");
        }
        return true;
    }

    //gives the player the item from the config
    private void giveReward(Player player, int amount) {
        ClaimingRewards plugin = ClaimingRewards.getInstance();
        List<String> itemList = plugin.getConfig().getStringList("item");
        if (itemList.isEmpty()) {
            player.sendMessage("Item configuration is missing!");
            return;
        }
        Material material = Material.getMaterial(itemList.get(0));
        if (material == null) {
            player.sendMessage("Invalid item material: " + itemList.get(0));
            return;
        }
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null && itemList.size() > 1) {
            meta.setDisplayName(itemList.get(1));
            if (itemList.size() > 2) {
                List<String> lore = itemList.subList(2, itemList.size());
                meta.setLore(lore);
            }
            itemStack.setItemMeta(meta);
        }
        player.getInventory().addItem(itemStack);
        player.sendMessage(ChatColor.GREEN + "" + amount + " claimed");
    }
}