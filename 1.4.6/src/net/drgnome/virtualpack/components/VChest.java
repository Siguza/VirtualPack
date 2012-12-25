// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;

import #PACKAGE_MINECRAFT#.*;

public class VChest extends ContainerChest
{    
    public VChest(EntityPlayer player, IInventory data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}