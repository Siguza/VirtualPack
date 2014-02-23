// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.Config;

public class VChest extends VContainer implements VGUI
{
    private final boolean _readonly;
    
    public VChest(EntityPlayer player, IInventory data, boolean canEdit)
    {
        super(player, data);
        _readonly = !canEdit;
    }
    
    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        if(_readonly)
        {
            return false;
        }
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
        return !Config.isBlacklisted(human.world.getWorld().getName(), human.getName(), "store", CraftItemStack.asBukkitCopy(item));
    }
}