// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;

import #PACKAGE_MINECRAFT#.*;

public class VFurnace extends ContainerFurnace
{
    public VFurnace(EntityPlayer player, TileEntityFurnace data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}