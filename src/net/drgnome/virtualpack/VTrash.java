// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

public class VTrash extends VChest
{
    private EntityPlayer player;
    
    public VTrash(EntityPlayer player)
    {
        super(player, new VTrashInv());
        this.player = player;
    }
    
    public ItemStack clickItem(int i, int j, #FIELD_CONTAINER_7#, EntityHuman entityhuman)
    {
        ItemStack item = super.clickItem(i, j, #FIELD_CONTAINER_9#, entityhuman);
        player.updateInventory(player.activeContainer);
        return item;
    }
}