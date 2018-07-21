package com.Drekryan.FarmHands;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.Flag;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmHands extends JavaPlugin
{
    @Override public void onLoad()
    {
        WorldGuardPlugin worldGuard = getWorldGuard();

        if ( worldGuard != null )
        {
            try
            {
                Flag customFlag = new BooleanFlag( "replace-crops" );
                worldGuard.getFlagRegistry().register( customFlag );
                this.getLogger().info( "Custom WorldGuard Flag has been registered" );
            }
            catch ( Exception e )
            {
                this.getLogger().severe( "Unable to register flag! Are you running at least WorldGuard 6.1.3?" );
                e.printStackTrace();
                this.onDisable();
            }
        }
        else
        {
            this.getLogger().severe( "Unable to find WorldGuard! Disabling..." );
        }
    }

    @Override public void onEnable()
    {
        //Register BlockListener
        new BlockListener( this );
        this.getLogger().info( "Successfully Enabled!" );
    }

    @Override public void onDisable()
    {
        this.getLogger().info( "Disabled FarmHands..." );
    }

    WorldGuardPlugin getWorldGuard()
    {
        Plugin plugin = getServer().getPluginManager().getPlugin( "WorldGuard" );

        // WorldGuard may not be loaded
        if ( !( plugin instanceof WorldGuardPlugin ) )
        {
            return null;
        }

        return ( WorldGuardPlugin ) plugin;
    }
}