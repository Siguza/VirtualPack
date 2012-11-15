// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

import static net.drgnome.virtualpack.Util.*;

public class VEnchantTable extends ContainerEnchantTable
{
    private Random rand = new Random();
    private int bookshelves;
    
    public VEnchantTable(EntityPlayer player, int bookshelves)
    {
        super(player.inventory, null, 0, 0, 0);
        this.checkReachable = false;
        this.bookshelves = bookshelves;
    }
    
    public void #FIELD_CONTAINERENCHANTTABLE_1#(IInventory iinventory) // Derpnote
    {
        if(iinventory == enchantSlots)
        {
            ItemStack itemstack = iinventory.getItem(0);
            if((itemstack != null) && (itemstack.#FIELD_ITEMSTACK_2#())) // Derpnote
            {
                #FIELD_CONTAINERENCHANTTABLE_4# = rand.nextLong(); // Derpnote
                for(int i = 0; i < 3; i++)
                {
                    costs[i] = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_1#(rand, i, bookshelves, itemstack); // Derpnote
                }
                #FIELD_CONTAINER_1#(); // Derpnote
            }
            else
            {
                for(int i = 0; i < 3; i++)
                {
                    costs[i] = 0;
                }
            }
        }
    }
    
    public boolean #FIELD_CONTAINERENCHANTTABLE_2#(EntityHuman entityhuman, int i) // Derpnote
    {
        ItemStack itemstack = enchantSlots.getItem(0);
        if(costs[i] > 0 && itemstack != null && ((entityhuman.expLevel >= costs[i]) || entityhuman.abilities.canInstantlyBuild || hasPermission(entityhuman.name, "vpack.use.enchanttable.free")))
        {
            List list = EnchantmentManager.#FIELD_ENCHANTMENTMANAGER_2#(rand, itemstack, costs[i]); // Derpnote
            if(list != null)
            {
                if(!entityhuman.abilities.canInstantlyBuild && !hasPermission(entityhuman.name, "vpack.use.enchanttable.free"))
                {
                    entityhuman.levelDown(#FIELD_BLUBB_1#costs[i]);
                }
                Iterator iterator = list.iterator();
                while(iterator.hasNext())
                {
                    EnchantmentInstance enchantmentinstance = (EnchantmentInstance)iterator.next();
                    itemstack.addEnchantment(enchantmentinstance.enchantment, enchantmentinstance.level);
                }
                #FIELD_CONTAINERENCHANTTABLE_5#(enchantSlots); // Derpnote
            }
            return true;
        }
        return false;
    }
    
    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        PlayerInventory playerinventory = entityhuman.inventory;
        if(playerinventory.getCarried() != null)
        {
            entityhuman.drop(playerinventory.getCarried());
            playerinventory.setCarried((ItemStack) null);
        }
        ItemStack itemstack = enchantSlots.splitWithoutUpdate(0);
        if(itemstack != null)
        {
            entityhuman.drop(itemstack);
        }
    }
}