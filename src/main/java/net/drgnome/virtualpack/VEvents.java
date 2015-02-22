// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
//import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.drgnome.virtualpack.util.*;

import static net.drgnome.virtualpack.util.Global.*;

public class VEvents implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleEntityDeath(EntityDeathEvent event)
    {
        if(!(event.getEntity() instanceof Player))
        {
            return;
        }
        Player player = (Player)event.getEntity();
        if(Perm.has(player, "vpack.use") && Config.worldEnabled(player.getWorld().getName()))
        {
            _plugin.handleDeath(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handlePlayerLogin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();
        if(Perm.has(player, "vpack.use"))
        {
            /*Thread thrd = new Thread()
            {
                public void run()
                {
                    for(String world : Config.worlds())
                    {
                        if(_plugin.hasPack(world, player))
                        {
                            _plugin.getPack(world, player).processSent();
                        }
                    }
                }
            };
            thrd.setPriority(Thread.MIN_PRIORITY);
            thrd.start();*/
            new BukkitRunnable()
            {
                public void run()
                {
                    for(String world : Config.worlds())
                    {
                        if(_plugin.hasPack(world, player))
                        {
                            _plugin.getPack(world, player).processSent();
                        }
                    }
                }
            }.runTaskAsynchronously(_plugin);
        }
    }

    // Since it doesn't work anyway...
    /*@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleGUIClose(InventoryCloseEvent event)
    {
        if(!(event.getInventory() instanceof BaseInv))
        {
            return;
        }
        ((BaseInv)event.getInventory()).onClose(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleGUIClick(InventoryClickEvent event)
    {
        if((event.getView() instanceof BaseView) && !((BaseView)event.getView()).allowClick(event.getRawSlot(), event.isRightClick(), event.isShiftClick()))
        {
            event.setCancelled(true);
        }
    }*/

    // vpack.admin.createsign
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleSignPlace(SignChangeEvent event)
    {
        Player p = event.getPlayer();
        if((event.getLine(0).equalsIgnoreCase("[VirtualPack]") || event.getLine(0).equalsIgnoreCase("[VPack]")) && !Perm.has(p, "vpack.admin.createsign"))
        {
            sendMessage(p, Lang.get(p, "vpack.nosignperm"), ChatColor.RED);
            event.setCancelled(true);
            event.getBlock().breakNaturally();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleSignClick(PlayerInteractEvent event)
    {
        if(event.hasBlock() && (event.getAction() == Action.RIGHT_CLICK_BLOCK))
        {
            BlockState bs = event.getClickedBlock().getState();
            if(bs instanceof Sign)
            {
                Sign sign = (Sign)bs;
                if(sign.getLine(0).equalsIgnoreCase("[VirtualPack]") || sign.getLine(0).equalsIgnoreCase("[VPack]"))
                {
                    String[] args = sign.getLine(1).split(" ");
                    VPlugin._commandHandler.tools(event.getPlayer(), _plugin.getPack(event.getPlayer()), VCommands.longname(args[0]), Util.cut(args, 1), false, true, 2);
                }
            }
        }
    }
}
