package org.venompvp.spawners;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.venompvp.venom.module.Module;
import org.venompvp.venom.module.ModuleInfo;
import org.venompvp.venom.utils.Utils;

@ModuleInfo(name = "VenomSpawners", author = "LilProteinShake", description = "Breaking spawners costs money", version = "1.0")
public class Spawners extends Module implements Listener {

    @Override
    public void onEnable() {
        setupModule(this);
        getServer().getPluginManager().registerEvents(this, this);

        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock() != null && event.getBlock().getType() == Material.MOB_SPAWNER && !event.isCancelled() && Utils.canEdit(player, event.getBlock().getLocation())) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) event.getBlock().getState();
            final String path = "spawner-type." + creatureSpawner.getSpawnedType().name().toLowerCase().replace("_", "-");
            if (getConfig().isSet(path)) {
                double cost = getConfig().getDouble(path);
                EconomyResponse economyResponse = getVenom().getEconomy().withdrawPlayer(player, cost);
                if (economyResponse.transactionSuccess()) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("broken-spawner")
                            .replace("{spawner}", Utils.capitalizeEveryWord(creatureSpawner.getSpawnedType().name().replace("_", " ")
                                    .replace("{cost}", String.valueOf(cost))))));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("got-no-money")));
                    event.setCancelled(true);
                }
            }
        }
    }
}
