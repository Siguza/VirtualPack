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

    public void #FIELD_SLOT_5#(EntityHuman entityhuman, ItemStack itemstack)
    {
        if(!VAnvil.playerFree(entityhuman))
        {
            entityhuman.levelDown(-_anvil.#FIELD_CONTAINERANVIL_5#);
        }
        VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(0, null);
        if(VAnvil.#FIELD_CONTAINERANVIL_7#(_anvil) > 0)
        {
            ItemStack itemstack1 = VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).getItem(1);
            if(itemstack1 != null && itemstack1.count > VAnvil.#FIELD_CONTAINERANVIL_7#(_anvil))
            {
                itemstack1.count -= VAnvil.#FIELD_CONTAINERANVIL_7#(_anvil);
                VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, itemstack1);
            }
            else
            {
                VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, null);
            }
        }
        else
        {
            VAnvil.#FIELD_CONTAINERANVIL_1#(_anvil).setItem(1, null);
        }
        _anvil.#FIELD_CONTAINERANVIL_5# = 0;
        _anvil.updatePlayerInventory();
    }
}