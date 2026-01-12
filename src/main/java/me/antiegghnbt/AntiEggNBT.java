package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // Нас интересуют ТОЛЬКО яйца
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        // SLIME
        if (event.getEntityType() == EntityType.SLIME) {
            Slime slime = (Slime) event.getEntity();

            if (slime.getSize() != 1) {
                slime.setSize(1);
            }
            return;
        }

        // MAGMA CUBE
        if (event.getEntityType() == EntityType.MAGMA_CUBE) {
            MagmaCube magma = (MagmaCube) event.getEntity();

            if (magma.getSize() != 1) {
                magma.setSize(1);
            }
        }
    }
}
