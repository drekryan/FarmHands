package com.Drekryan.FarmHands;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEvent;

class BlockListener implements Listener
{
    private FarmHands plugin;
    private WorldGuardPlugin worldguard;

    BlockListener( FarmHands plugin )
    {
        this.plugin = plugin;
        this.worldguard = plugin.getWorldGuard();
        this.plugin.getServer().getPluginManager().registerEvents( this, plugin );
    }

    @EventHandler( priority = EventPriority.HIGHEST )
    public void onBlockBreak( BlockBreakEvent event )
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        World world = block.getWorld();

        //Check that block is a normal crop or Nether Wart
        if ( isCrop( block ) )
        {
            //Allow players in creative mode to bypass protection
            if ( playerInCreativeMode( player ) )
            {
                return;
            }

            Material material = block.getType();
            if ( shouldReplaceCrops( block ) )
            {
                event.setCancelled( true );

                //Check to see if the crop is fully grown
                if ( isFullyGrown( block ) )
                {
                    //Break and replace crop
                    block.breakNaturally();
                    world.getBlockAt( location ).setType( material );
                }
                else
                {
                    //TODO: Should a sound be played here?
                    player.sendMessage( ChatColor.RED + "This region only allows farming fully grown crops!" );
                }
            }
        }

        //For now just prevent breaking these blocks all together
        Material material = block.getType();
        if ( ( material == Material.CACTUS || material == Material.SUGAR_CANE || material == Material.MELON_STEM ||
                material == Material.PUMPKIN_STEM || material == Material.CHORUS_PLANT || material == Material.CHORUS_FLOWER )
                && !playerInCreativeMode( player ) )
        {
            event.setCancelled( true );
        }
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        Block block = event.getClickedBlock();
        if ( block == null )
        {
            return;
        }

        //Check if the player is trampling a crop
        if ( event.getAction() == Action.PHYSICAL && block.getType() == Material.FARMLAND && shouldReplaceCrops( block.getRelative( BlockFace.UP, 1 ) ) )
        {
            event.setUseInteractedBlock( Event.Result.DENY );
            event.setCancelled( true );
        }
    }

    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockFromTo( BlockFromToEvent event )
    {
        //Prevents harvesting crops with water
        Block block = event.getBlock();
        Block toBlock = event.getToBlock();

        if ( isCrop( toBlock ) && block.getType() == Material.WATER )
        {
            Material material = toBlock.getType();
            BlockData data = toBlock.getBlockData();
            event.setCancelled( true );

            toBlock.setType( material );
            toBlock.setBlockData( data );
        }
    }

    private boolean playerInCreativeMode( Player player )
    {
        return player.getGameMode().equals( GameMode.CREATIVE );
    }

    private boolean shouldReplaceCrops( Block block )
    {
        Location location = block.getLocation();
        World world = block.getWorld();
        RegionManager rm = worldguard.getRegionManager( world );
        ApplicableRegionSet regions = rm.getApplicableRegions( BukkitUtil.toVector( location ) );

        for ( ProtectedRegion region : regions.getRegions() )
        {
            Flag flag = DefaultFlag.fuzzyMatchFlag( worldguard.getFlagRegistry(), "replace-crops" );

            //TODO: Possibly more safeguards here?
            if ( ( boolean ) region.getFlag( flag ) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean isCrop( Block block )
    {
        return block.getBlockData() instanceof Ageable;
    }

    private boolean isFullyGrown( Block block )
    {
        if ( block.getBlockData() instanceof Ageable )
        {
            Ageable data = (Ageable) block.getBlockData();
            return data.getAge() == data.getMaximumAge();
        }
        return false;
    }
}
