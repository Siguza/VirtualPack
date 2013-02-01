// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.util.*;

public class VUncrafter extends VContainer
{
    public VUncrafter(EntityPlayer player)
    {
        super(player, new VUncrafterInv());
        #FIELD_CONTAINER_2# = new ArrayList(); // Derpnote
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
        }
    }
    
    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        for(int i = 0; i < 18; i++)
        {
            ItemStack itemstack = container.splitWithoutUpdate(i);
            if(itemstack != null)
            {
                entityhuman.drop(itemstack);
            }
        }
    }
}