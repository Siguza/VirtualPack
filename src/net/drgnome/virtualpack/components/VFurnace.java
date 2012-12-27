// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;

public class VFurnace extends ContainerFurnace
{
    public VFurnace(EntityPlayer player, TileEntityFurnace data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
    }
}