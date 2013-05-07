// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import net.drgnome.virtualpack.components.BaseInv;
import net.drgnome.virtualpack.components.BaseView;
import net.drgnome.virtualpack.util.Config;

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
        if(Config.worldEnabled(player.getWorld().getName()))
        {
            _plugin.handleDeath(player);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handlePlayerLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        for(String world : Config.worlds())
        {
            if(!_plugin.hasPack(world, player.getName()))
            {
                continue;
            }
            _plugin.getPack(world, player.getName()).processSent();
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
    }
}