package dev.error110.claimingRewards;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.palmergames.bukkit.towny.object.Town;

import java.util.*;

public class NewDayListener implements Listener {

    @EventHandler
    public void onNewDay(PreNewDayEvent event) {
        TownyMessaging.sendGlobalMessage(ChatColor.YELLOW + "oOo_______[" + ChatColor.GOLD + " Claim Rewards Leaderboard " + ChatColor.YELLOW + "]_______oOo");
        TownyMessaging.sendGlobalMessage("Towns Eligible For Rewards:");

        ClaimingRewards plugin = ClaimingRewards.getInstance();

        List<Map.Entry<Town, Integer>> sortedTowns = plugin.SortTownToClaimsList();
        Map<UUID, Integer> townToRewards = plugin.getAllTownRewards();

        for (int i = 0; i < Math.min(sortedTowns.size(), plugin.getConfig().getInt("towns-rewarded-count")); i++) {
            Map.Entry<Town, Integer> entry = sortedTowns.get(i);
            String rewardKey = switch (i) {
                case 0 -> "first-total-given";
                case 1 -> "second-total-given";
                case 2 -> "third-total-given";
                default -> "default-total-given";
            };

            TownyMessaging.sendGlobalMessage(ChatColor.AQUA + entry.getKey().getName() + ChatColor.AQUA + " (" + entry.getValue() + " Claims" + ")" + ChatColor.DARK_GRAY + " - " + ChatColor.GOLD + plugin.getConfig().getInt(rewardKey) + " Rewards/Day");

            giveRewards(rewardKey, townToRewards, entry, plugin);
        }
    }

    private void giveRewards(String reward, Map<UUID, Integer> townToRewards, Map.Entry<Town, Integer> entry, ClaimingRewards plugin) {
        int rewardAmount = plugin.getConfig().getInt(reward);
        UUID townId = entry.getKey().getUUID();
        int current = townToRewards.getOrDefault(townId, 0);
        plugin.setRewardForTown(entry.getKey(), current + rewardAmount);
    }
}