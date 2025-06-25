package dev.error110.claimingRewards;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public final class ClaimingRewards extends JavaPlugin {
    private final File rewardsFile = new File(getDataFolder(), "town_rewards.json");
    private final Gson gson = new Gson();
    private static ClaimingRewards instance;
    private final HashMap<UUID, Integer> townToRewards = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        load();
        register();
        instance = this;
    }

    @Override
    public void onDisable() {
        save();
    }

    public void register() {
        new RewardsCommand();
        this.getServer().getPluginManager().registerEvents(new NewDayListener(), this);
    }

    public void save() {
        saveConfig();
        try (Writer writer = new FileWriter(rewardsFile)) {
            gson.toJson(townToRewards, writer);
        } catch (IOException e) {
            getLogger().warning("Could not save rewards: " + e.getMessage());
        }
    }

    public void load() {
        townToRewards.clear();
        if (!rewardsFile.exists()) return;
        Type type = new TypeToken<HashMap<UUID, Integer>>(){}.getType();
        try (Reader reader = new FileReader(rewardsFile)) {
            HashMap<UUID, Integer> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                townToRewards.putAll(loaded);
            }
        } catch (IOException e) {
            getLogger().warning("Could not load rewards: " + e.getMessage());
        }
    }

    public static ClaimingRewards getInstance() {
        return instance;
    }

    public void setRewardForTown(Town town, int rewards) {
        townToRewards.put(town.getUUID(), rewards);
    }

    public Integer getRewardForTown(Town town) {
        return townToRewards.get(town.getUUID());
    }

    public void removeRewardForTown(Town town) {
        townToRewards.remove(town.getUUID());
    }

    public Map<UUID, Integer> getAllTownRewards() {
        return new HashMap<>(townToRewards);
    }

    public List<Map.Entry<Town, Integer>> SortTownToClaimsList() {
        return TownyAPI.getInstance().getTowns().stream().collect(Collectors.toMap(town -> town, Town::getNumTownBlocks, (a, b) -> b, LinkedHashMap::new)).entrySet().stream().sorted(Map.Entry.<Town, Integer>comparingByValue().reversed()).toList();
    }
}
