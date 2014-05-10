// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;

public class VUncrafter extends VContainer
{
    public VUncrafter(EntityPlayer player)
    {
        super(player, new VUncrafterInv(player));
        /*#FIELD_CONTAINER_2# = new ArrayList(); // Derpnote
        #FIELD_CONTAINER_3# = new ArrayList(); // Derpnote
        for(int i = 0; i < 2; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                #FIELD_CONTAINER_4#(new Slot(container, j + 9 * i, 8 + j * 18, 18)); // Derpnote
            }
        }
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                #FIELD_CONTAINER_4#(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 67 + i * 18)); // Derpnote
            }
        }
        for(int i = 0; i < 9; i++)
        {
            #FIELD_CONTAINER_4#(new Slot(player.inventory, i, 8 + i * 18, 125)); // Derpnote
        }*/
    }

    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        for(int i = 0; i < 18; i++)
        {
            ItemStack itemstack = container.splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack, false); // Whatever "false" does
            }
        }
    }

    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        if(shift == 1)
        {
            if(slot >= this.container.getSize())
            {
                for(int i = 0; i < 9; i++)
                {
                    if(this.container.getItem(i) == null)
                    {
                        return isItemAllowed(human, human.inventory.getItem(toInventorySlot(slot - this.container.getSize())));
                    }
                }
                return false;
            }
            return true;
        }
        else if((slot >= 0) && (slot < 9))
        {
            return isItemAllowed(human, human.inventory.getCarried());
        }
        else if((slot >= 9) && (slot < 18))
        {
            return human.inventory.getCarried() == null;
        }
        return super.allowClick(slot, mouse, shift, human);
    }

    private boolean isItemAllowed(EntityHuman human, ItemStack item)
    {
        if(item == null)
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
        return !Config.isBlacklisted(human.world.getWorld().getName(), human.getUniqueID(), "uncrafter", CraftItemStack.asBukkitCopy(item));
    }
}
