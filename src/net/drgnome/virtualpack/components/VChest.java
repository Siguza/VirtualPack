// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;

public class VChest extends ContainerChest
{    
    public VChest(EntityPlayer player, IInventory data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}