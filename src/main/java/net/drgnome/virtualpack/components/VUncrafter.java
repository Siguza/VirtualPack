// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;

public class VUncrafter extends VContainer
{
    public VUncrafter(EntityPlayer player)
    {
        super(player, new VUncrafterInv(player));
    }

    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        for(int i = 0; i < 18; i++)
        {
            ItemStack itemstack = this.#FIELD_CONTAINERCHEST_1#().splitWithoutUpdate(i);
            if(itemstack != null && itemstack != #F_ITEMSTACK_NULL#)
            {
                entityhuman.drop(itemstack, false); // Whatever "false" does
            }
        }
    }

    public boolean allowClick(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        IInventory container = this.#FIELD_CONTAINERCHEST_1#();
        if(meta == #F_INVCLICK_QUICK_MOVE#)
        {
            if(slot >= container.getSize())
            {
                for(int i = 0; i < 9; i++)
                {
                    if(container.getItem(i) == #F_ITEMSTACK_NULL#)
                    {
                        return isItemAllowed(human, human.inventory.getItem(toInventorySlot(slot - container.getSize())));
                    }
                }
                return false;
            }
            return true;
        }
        else if(meta == #F_INVCLICK_SWAP#)
        {
            return slot >= container.getSize() || isItemAllowed(human, human.inventory.getItem(toInventorySlot(27 + mouse))); // "mouse"
        }
        else if((slot >= 0) && (slot < 9))
        {
            return isItemAllowed(human, human.inventory.getCarried());
        }
        else if((slot >= 9) && (slot < 18))
        {
            return human.inventory.getCarried() == #F_ITEMSTACK_NULL#;
        }
        return super.allowClick(slot, mouse, meta, human);
    }

    private boolean isItemAllowed(EntityHuman human, ItemStack item)
    {
        if(item == null || item == #F_ITEMSTACK_NULL#)
        {
            return true;
        }
        if(!Config.bool(human.world.getWorld().getName(), "uncraft-enchanted"))
        {
            NBTTagList ench = item.getEnchantments();
            if((ench != null) && (ench.size() > 0))
            {
                return false;
            }
        }
        return !Config.isBlacklisted(human.world.getWorld().getName(), (Player)human.getBukkitEntity(), "uncrafter", CraftItemStack.asBukkitCopy(item));
    }
}
