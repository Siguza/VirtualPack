// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v1_5_R2.*;

public class VWorkbench extends ContainerWorkbench implements VGUI
{
    public VWorkbench(EntityPlayer player)
    {
        super(player.inventory, player.world, 0, 0, 0);
        this.checkReachable = false;
    }
}