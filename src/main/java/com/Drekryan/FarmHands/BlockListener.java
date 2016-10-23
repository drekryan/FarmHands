package com.Drekryan.FarmHands;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

class BlockListener implements Listener {
    private FarmHands plugin;
    private WorldGuardPlugin worldguard;

    BlockListener(FarmHands plugin) {
        this.plugin = plugin;
        this.worldguard = plugin.getWorldGuard();
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        boolean replace = false;

        if (block.getType() == Material.CARROT || block.getType() == Material.BEETROOT_BLOCK || block.getType() == Material.CROPS ||
                block.getType() == Material.WHEAT || block.getType() == Material.POTATO) {
            Material mat = block.getType();
            RegionManager rm = worldguard.getRegionManager(block.getWorld());
            ApplicableRegionSet regions = rm.getApplicableRegions(BukkitUtil.toVector(loc));

            for (ProtectedRegion region : regions.getRegions()) {
                Flag flag = DefaultFlag.fuzzyMatchFlag(worldguard.getFlagRegistry(), "replace-crops");

                if ((boolean) region.getFlag(flag)) {
                    replace = true;
                }
            }

            if (replace) {
                int age = block.getData();
                int reqAge = (block.getType() == Material.BEETROOT_BLOCK) ? 3 : 7;

                event.setCancelled(true);
                if (age == reqAge) {
                    block.breakNaturally();
                    loc.getWorld().getBlockAt(loc).setType(mat, true);
                } else {
                    event.getPlayer().sendMessage(ChatColor.GOLD + "[FarmHands] " + ChatColor.RED + "Sorry, these crops aren't ready yet...");
                }
            }
        }
    }
}
