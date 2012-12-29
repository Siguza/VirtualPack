// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;

public abstract class VContainer extends VChest
{    
    public VContainer(EntityPlayer player, IInventory data)
    {
        super(player, data);
    }
    
    public ItemStack clickItem(int i, int j, int meta, EntityHuman entityhuman)
    {
        ItemStack item = super.clickItem(i, j, meta, entityhuman);
        update();
        return item;
    }
    
    protected void update()
    {
        player.updateInventory(player.activeContainer);
    }
}