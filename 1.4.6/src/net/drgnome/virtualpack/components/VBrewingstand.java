// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;

import #PACKAGE_MINECRAFT#.*;

public class VBrewingstand extends ContainerBrewingStand
{    
    public VBrewingstand(EntityPlayer player, TileEntityBrewingStand data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}