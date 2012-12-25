// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import net.drgnome.virtualpack.util.Config;

import static net.drgnome.virtualpack.util.Global.*;

public class VEvents implements Listener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleEntityDeath(EntityDeathEvent event)
    {
        if((event == null) || (event.getEntity() == null) || !(event.getEntity() instanceof Player))
        {
            return;
        }
        Player player = (Player)event.getEntity();
        if(Config.worldEnabled(player.getWorld().getName()))
        {
            _plugin.handleDeath(player.getName());
        }
    }
}