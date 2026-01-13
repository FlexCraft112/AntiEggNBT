package me.antiegghnbt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AntiEggNBT extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("AntiEggNBT enabled (spawn egg NBT reset)");
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {

        // Только спавн из яйца
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG)
            return;

        EntityType originalType = event.getEntityType();

        // Определяем материал яйца по типу моба
        Material eggMaterial = Material.getMaterial(originalType.name() + "_SPAWN_EGG");
        if (eggMaterial == null)
            return;

        // Если это проблемные мобы — всегда сбрасываем до ванили
        if (originalType == EntityType.SLIME
                || originalType == EntityType.MAGMA_CUBE
                || originalType == EntityType.GIANT) {

            event.setCancelled(true);

            Location loc = event.getLocation();

            // Спавним ЧИСТОГО ванильного моба
            EntityType spawnType = originalType == EntityType.GIANT
                    ? EntityType.ZOMBIE
                    : originalType;

            loc.getWorld().spawnEntity(loc, spawnType);

            getLogger().warning(
                    "Reset NBT spawn egg: " + originalType.name()
            );
        }
    }
}
