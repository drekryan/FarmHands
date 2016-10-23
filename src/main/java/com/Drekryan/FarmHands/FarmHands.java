package com.Drekryan.FarmHands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmHands extends JavaPlugin {
    private WorldGuardPlugin worldGuard;
    private BlockListener blockListener;
    private static Flag REPLACE_CROPS = new BooleanFlag("replace-crops");

    @Override
    public void onLoad() {
        this.worldGuard = getWorldGuard();

        if (worldGuard != null) {
            worldGuard.getFlagRegistry().register(REPLACE_CROPS);
            this.getLogger().info("Custom WorldGuard Flag has been registered");
        }
    }

    @Override
    public void onEnable() {
        //Register BlockListener
        blockListener = new BlockListener(this);
        this.getLogger().info("Successfully Enabled!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled FarmHands...");
    }

    WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }
}