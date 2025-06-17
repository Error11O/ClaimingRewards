package dev.error110.claimingRewards;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.event.PreNewDayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import java.util.*;
import java.util.stream.Collectors;

public class NewDayListener implements Listener {

    @EventHandler
    public void onNewDay(PreNewDayEvent event) {
        TownyMessaging.sendGlobalMessage("Townys top Claimers:");
        //this gets the towns list gets the number of blocks of each town then sorts it by highest to lowest.
        List<Map.Entry<Town, Integer>> sortedTowns =
                TownyAPI.getInstance().getTowns()
                        .stream()
                        .collect(Collectors.toMap(town -> town, Town::getNumTownBlocks, (a, b) -> b, LinkedHashMap::new))
                        .entrySet().stream().sorted(Map.Entry.<Town, Integer>comparingByValue().reversed()).toList();
        //gets the town ammount of towns rewarded
        ClaimingRewards plugin = ClaimingRewards.getInstance();
        Map<UUID, Integer> townToRewards = plugin.getAllTownRewards();
        int rewardedCount = plugin.getConfig().getInt("towns-rewarded-count");
        //for loop until max towns on the server or the town rewarded count
        for (int i = 0; i < Math.min(sortedTowns.size(), rewardedCount); i++) {
            Map.Entry<Town, Integer> entry = sortedTowns.get(i);
            //selects what config will be used for the reward
            String rewardKey = switch (i) {
                case 0 -> "first-total-given";
                case 1 -> "second-total-given";
                case 2 -> "third-total-given";
                default -> "default-total-given";
            };
            //adds the reward to the town and sends a message to the server
            giveRewards(rewardKey, townToRewards, entry, plugin);
            TownyMessaging.sendGlobalMessage(entry.getKey().getName() + ": " + entry.getValue());
        }
    }

    private void giveRewards(String reward, Map<UUID, Integer> townToRewards, Map.Entry<Town, Integer> entry, ClaimingRewards plugin) {
        int rewardAmount = plugin.getConfig().getInt(reward);
        UUID townId = entry.getKey().getUUID();
        int current = townToRewards.getOrDefault(townId, 0);
        plugin.setRewardForTown(entry.getKey(), current + rewardAmount);
    }
}