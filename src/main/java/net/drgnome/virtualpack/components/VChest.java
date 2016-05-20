// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.entity.Player;
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

    public boolean allowClick(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        if(_readonly)
        {
            return false;
        }
        IInventory container = this.#FIELD_CONTAINERCHEST_1#();
        if(meta == #F_INVCLICK_QUICK_MOVE#)
        {
            return slot < container.getSize() || isItemAllowed(human, human.inventory.getItem(toInventorySlot(slot - container.getSize())));
        }
        else if(meta == #F_INVCLICK_SWAP#)
        {
            return slot >= container.getSize() || isItemAllowed(human, human.inventory.getItem(toInventorySlot(27 + mouse))); // "mouse"
        }
        else if((slot >= 0) && (slot < container.getSize()))
        {
            return isItemAllowed(human, human.inventory.getCarried());
        }
        return true;
    }

    private boolean isItemAllowed(EntityHuman human, ItemStack item)
    {
        return !Config.isBlacklisted(human.world.getWorld().getName(), (Player)human.getBukkitEntity(), "store", CraftItemStack.asBukkitCopy(item));
    }
}
