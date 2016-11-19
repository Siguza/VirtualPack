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

public class TmpMatter extends VContainer implements VGUI
{
    private TmpMatterInv _inv;
    private final boolean _readonly;

    public TmpMatter(EntityPlayer player, TmpMatterInv inv, boolean canEdit)
    {
        super(player, inv);
        _inv = inv;

        ---------- PRE 1.11 START ----------
        #FIELD_CONTAINER_2# = new ArrayList(); // Derpnote
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        #FIELD_CONTAINER_2# = NonNullList.<ItemStack>#F_NEW_NONNULLLIST#();
        ---------- SINCE 1.11 END ----------
        #FIELD_CONTAINER_3# = new ArrayList(); // Derpnote
        for(int i = 0; i < 6; i++)
        {
            for(int j = 0; j < 9; j++)
            {
                #FIELD_CONTAINER_4#(new Slot(this.#FIELD_CONTAINERCHEST_1#(), j + 9 * i, 8 + j * 18, 18)); // Derpnote
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
        _readonly = !canEdit;
    }

    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        for(int i = 1; i < 9; i++)
        {
            ItemStack itemstack = this.#FIELD_CONTAINERCHEST_1#().splitWithoutUpdate(i);
            if(itemstack != null && itemstack != #F_ITEMSTACK_NULL#)
            {
                entityhuman.drop(itemstack, false); // Another "false"
            }
        }
    }

    public boolean allowClick(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        update();
        if(_readonly || (slot == 0))
        {
            return false;
        }
        else if((slot >= 9) && (slot < 54))
        {
            ItemStack carried = human.inventory.getCarried();
            if((_inv.getItem(slot) == #F_ITEMSTACK_NULL#) || ((carried != null && carried != #F_ITEMSTACK_NULL#) && ((meta != #F_INVCLICK_PICKUP#) || !carried.doMaterialsMatch(_inv.getItem(slot)))))
            {
                return false;
            }
            double value = TransmutationHelper.getValue(CraftItemStack.asBukkitCopy(_inv.getItem(slot)));
            if(_inv._value < value)
            {
                return false;
            }
            if(meta != #F_INVCLICK_PICKUP#)
            {
                int max = Util.min(Util.floor(_inv._value / value), _inv.getItem(slot).getMaxStackSize());
                org.bukkit.inventory.ItemStack stack = CraftItemStack.asBukkitCopy(_inv.getItem(slot)).clone();
                stack.setAmount(max);
                HashMap<Integer, org.bukkit.inventory.ItemStack> map = Bukkit.getPlayer(human.getName()).getInventory().addItem(stack);
                if(map.size() > 0)
                {
                    max -= map.values().iterator().next().getAmount();
                }
                _inv._value -= (value * (double)max);
            }
            else
            {
                int max = (mouse > 0) ? Util.min(Util.floor(_inv._value / value), (carried == null || carried == #F_ITEMSTACK_NULL#) ? _inv.getItem(slot).getMaxStackSize() :
                ---------- PRE 1.11 START ----------
                (carried.getMaxStackSize() - carried.count)) : 1;
                ---------- PRE 1.11 END ----------
                ---------- SINCE 1.11 START ----------
                (carried.getMaxStackSize() - carried.getCount())) : 1;
                ---------- SINCE 1.11 END ----------
                if(carried == null || carried == #F_ITEMSTACK_NULL#)
                {
                    carried = Util.copy_old(_inv.getItem(slot));
                    ---------- PRE 1.11 START ----------
                    carried.count = max;
                    ---------- PRE 1.11 END ----------
                    ---------- SINCE 1.11 START ----------
                    carried.setCount(max);
                    ---------- SINCE 1.11 END ----------
                    human.inventory.setCarried(carried);
                }
                else
                {
                    ---------- PRE 1.11 START ----------
                    carried.count += max;
                    ---------- PRE 1.11 END ----------
                    ---------- SINCE 1.11 START ----------
                    carried.setCount(carried.getCount() + max);
                    ---------- SINCE 1.11 END ----------
                }
                _inv._value -= (value * (double)max);
            }
            _inv.updateInfo();
            _inv.updateInv();
            return false;
        }
        else if((meta != #F_INVCLICK_PICKUP#) && (slot >= 54))
        {
            for(int i = 1; i < 8; i++)
            {
                if(_inv.getItem(i) == #F_ITEMSTACK_NULL#)
                {
                    _inv.setItem(i, human.inventory.getItem(toInventorySlot(slot - 54)));
                    human.inventory.setItem(toInventorySlot(slot - 54), #F_ITEMSTACK_NULL#);
                    break;
                }
            }
            return false;
        }
        return true;
    }
}
