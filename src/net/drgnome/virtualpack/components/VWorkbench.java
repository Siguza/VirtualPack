// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;

public class VWorkbench extends ContainerWorkbench
{
    public VWorkbench(EntityPlayer player)
    {
        super(player.inventory, player.world, 0, 0, 0);
        this.checkReachable = false;
    }
}