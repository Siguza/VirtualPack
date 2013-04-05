// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v1_5_R2.*;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.Config;

public class VChest extends VContainer
{
    public VChest(EntityPlayer player, IInventory data)
    {
        super(player, data);
    }
    
    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        if(shift == 1)
        {
            if(slot >= this.container.getSize())
            {
                return isItemAllowed(human, human.inventory.getItem(toInventorySlot(slot - this.container.getSize())));
            }
            return true;
        }
        else if((slot >= 0) && (slot < this.container.getSize()))
        {
            return isItemAllowed(human, human.inventory.getCarried());
        }
        return true;
    }
    
    private boolean isItemAllowed(EntityHuman human, ItemStack item)
    {
        return !Config.isBlacklisted(human.world.getWorld().getName(), human.name, "store", CraftItemStack.asBukkitCopy(item));
    }
}