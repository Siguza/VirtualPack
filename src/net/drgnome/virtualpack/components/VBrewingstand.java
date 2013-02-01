// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;

public class VBrewingstand extends ContainerBrewingStand
{    
    public VBrewingstand(EntityPlayer player, TileEntityBrewingStand data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}