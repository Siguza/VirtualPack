// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.tmp;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;
import net.drgnome.virtualpack.item.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.data.TransmutationHelper;

public class TmpMatter extends VContainer
{
    private TmpMatterInv _inv;
    
    public TmpMatter(EntityPlayer player, TmpMatterInv inv)
    {
        super(player, inv);
        _inv = inv;
        #FIELD_CONTAINER_2# = new ArrayList(); // Derpnote
        #FIELD_CONTAINER_3# = new ArrayList(); // Derpnote
        for(int i = 0; i < 6; i++)
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
        }
    }
    
    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        for(int i = 1; i < 9; i++)
        {
            ItemStack itemstack = container.splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack);
            }
        }
    }
    
    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        if(slot == 0)
        {
            return false;
        }
        else if((slot >= 9) && (slot < 54))
        {
            if((_inv.getItem(slot) == null) || ((human.inventory.getCarried() != null) && ((shift > 0) || !human.inventory.getCarried().doMaterialsMatch(_inv.getItem(slot)))))
            {
                return false;
            }
            double value = TransmutationHelper.getValue(CraftItemStack.asBukkitCopy(_inv.getItem(slot)));
            if(_inv._value < value)
            {
                return false;
            }
            if(shift > 0)
            {
                int max = Util.min(Util.floor(_inv._value / value), _inv.getItem(slot).getMaxStackSize());
                org.bukkit.inventory.ItemStack stack = CraftItemStack.asBukkitCopy(_inv.getItem(slot)).clone();
                stack.setAmount(max);
                HashMap<Integer, org.bukkit.inventory.ItemStack> map = Bukkit.getPlayer(human.name).getInventory().addItem(stack);
                if(map.size() > 0)
                {
                    max -= map.values().iterator().next().getAmount();
                }
                _inv._value -= (value * (double)max);
            }
            else
            {
                int max = (mouse > 0) ? Util.min(Util.floor(_inv._value / value), human.inventory.getCarried() == null ? _inv.getItem(slot).getMaxStackSize() : (human.inventory.getCarried().getMaxStackSize() - human.inventory.getCarried().count)) : 1;
                if(human.inventory.getCarried() == null)
                {
                    ItemStack stack = Util.copy_old(_inv.getItem(slot));
                    stack.count = max;
                    human.inventory.setCarried(stack);
                }
                else
                {
                    human.inventory.getCarried().count += max;
                }
                _inv._value -= (value * (double)max);
            }
            _inv.updateInfo();
            _inv.updateInv();
            return false;
        }
        else if((shift > 0) && (slot >= 54))
        {
            for(int i = 1; i < 8; i++)
            {
                if(_inv.getItem(i) == null)
                {
                    _inv.setItem(i, human.inventory.getItem(toInventorySlot(slot - 54)));
                    human.inventory.setItem(toInventorySlot(slot - 54), null);
                    break;
                }
            }
            return false;
        }
        return true;
    }
}