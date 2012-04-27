// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

public class VFurnace extends ContainerFurnace
{
    public VFurnace(EntityPlayer player, TileEntityFurnace data)
    {
        super(player.inventory, data);
    }
    
    public boolean b(EntityHuman entityhuman)
    {
        return true;
    }
}