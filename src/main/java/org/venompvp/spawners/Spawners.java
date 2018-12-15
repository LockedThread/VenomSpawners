package org.venompvp.spawners;

import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.venompvp.venom.module.Module;
import org.venompvp.venom.module.ModuleInfo;
import org.venompvp.venom.utils.Utils;

@ModuleInfo(name = "VenomSpawners", author = "LilProteinShake", description = "Breaking spawners costs money", version = "1.0")
public class Spawners extends Module implements Listener {

    @Override
    public void onEnable() {
        setupModule(this);
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (event.getBlock() != null &&
                event.getBlock().getType() == Material.MOB_SPAWNER &&
                !event.isCancelled() &&
                Utils.canEdit(player, event.getBlock().getLocation())) {
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

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Item && (event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
            ItemStack itemStack = ((Item) entity).getItemStack();
            if (getConfig().getBoolean("blacklist-diamond-armor")) {
                if (itemStack.getType() == Material.DIAMOND_CHESTPLATE
                        || itemStack.getType() == Material.DIAMOND_LEGGINGS
                        || itemStack.getType() == Material.DIAMOND_BOOTS
                        || itemStack.getType() == Material.DIAMOND_HELMET) {
                    event.setCancelled(true);
                }
            }
            if (getConfig().getStringList("blacklisted-item-destroy").contains(itemStack.getType().name())) {
                event.setCancelled(true);
            }
        }
    }
}
