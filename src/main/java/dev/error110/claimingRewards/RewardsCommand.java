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
import java.util.stream.Collectors;

public class RewardsCommand extends BaseCommand implements TabExecutor {

    public RewardsCommand() {
        AddonCommand addonCommand = new AddonCommand(TownyCommandAddonAPI.CommandType.TOWN, "rewards", this);
        TownyCommandAddonAPI.addSubCommand(addonCommand);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of("claim", "list");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        //checks if the sender is a player and has permission to use the command
        if (sender instanceof Player player) {
            if (!player.hasPermission("claimingrewards.collectrewards")) {
                sender.sendMessage("You do not have permission to use this command");
                return true;
            }
            //checks if the command is valid
            switch (args[0].toLowerCase()) {
                case "claim": {
                    ClaimingRewards plugin = ClaimingRewards.getInstance();
                    Town town = TownyAPI.getInstance().getTown(player);
                    if (town == null) return true;

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
                //uses same sorting as newdaylistener then shows the leaderboard msg
                case "list": {
                    ClaimingRewards plugin = ClaimingRewards.getInstance();
                    List<Map.Entry<Town, Integer>> sortedTowns = TownyAPI.getInstance().getTowns().stream()
                            .collect(Collectors.toMap(
                                    town -> town,
                                    Town::getNumTownBlocks,
                                    (a, b) -> b,
                                    LinkedHashMap::new))
                            .entrySet().stream()
                            .sorted(Map.Entry.<Town, Integer>comparingByValue().reversed())
                            .toList();
                    sender.sendMessage(ChatColor.DARK_AQUA + "Rewards Leaderboard:");
                    for (int i = 0; i < Math.min(sortedTowns.size(), plugin.getConfig().getInt("towns-rewarded-count")); i++) {
                        Map.Entry<Town, Integer> entry = sortedTowns.get(i);
                        sender.sendMessage(ChatColor.AQUA + entry.getKey().getName() + ChatColor.DARK_GRAY + " - " + ChatColor.AQUA + "(" + entry.getValue() + ")");
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