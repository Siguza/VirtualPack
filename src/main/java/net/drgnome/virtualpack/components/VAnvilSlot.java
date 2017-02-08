// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.util.*;

public class VAnvilSlot extends Slot
{
    private VAnvil _anvil;

    VAnvilSlot(VAnvil anvil, IInventory iinventory, int i, int j, int k)
    {
        super(iinventory, i, j, k);
        _anvil = anvil;
    }

    public boolean isAllowed(ItemStack itemstack)
    {
        return false;
    }

    public boolean #FIELD_SLOT_4#(EntityHuman entityhuman)
    {
        return (VAnvil.playerFree(entityhuman) || entityhuman.expLevel >= _anvil.#FIELD_CONTAINERANVIL_5#) && (_anvil.#FIELD_CONTAINERANVIL_5# > 0) && #FIELD_SLOT_1#();
    }

    public #F_SLOT_RETURN# #FIELD_SLOT_5#(EntityHuman entityhuman, ItemStack itemstack)
    {
        boolean free = VAnvil.playerFree(entityhuman);
        if(!free)
        {
            entityhuman.levelDown(-_anvil.#FIELD_CONTAINERANVIL_5#);
        }

        ---------- SINCE 1.11.2 START ----------
        ItemStack itemstack2 = VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).getItem(0);
        if(itemstack2.getCount() != 1 && !free && !(itemstack2.getItem() instanceof ItemNameTag))
        {
            itemstack2.setCount(itemstack2.getCount() - 1);
        }
        else
        {
        ---------- SINCE 1.11.2 END ----------
            VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(0, #F_ITEMSTACK_NULL#);
        ---------- SINCE 1.11.2 START ----------
        }
        ---------- SINCE 1.11.2 END ----------

        int whatever = _anvil.getWhatever();
        if(whatever > 0)
        {
            ItemStack itemstack1 = VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).getItem(1);

            ---------- PRE 1.11 START ----------
            if(itemstack1 != null && itemstack1.count > whatever)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(itemstack1 != null && itemstack1.getCount() > whatever)
            ---------- SINCE 1.11 END ----------
            {
                ---------- PRE 1.11 START ----------
                itemstack1.count -= whatever;
                ---------- PRE 1.11 END ----------
                ---------- SINCE 1.11 START ----------
                itemstack1.setCount(itemstack1.getCount() - whatever);
                ---------- SINCE 1.11 END ----------
                VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, itemstack1);
            }
            else
            {
                VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, #F_ITEMSTACK_NULL#);
            }
        }
        else
        {
            VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, #F_ITEMSTACK_NULL#);
        }
        _anvil.#FIELD_CONTAINERANVIL_5# = 0;
        _anvil.updatePlayerInventory();
        ---------- SINCE 1.11 START ----------
        return itemstack;
        ---------- SINCE 1.11 END ----------
    }
}
