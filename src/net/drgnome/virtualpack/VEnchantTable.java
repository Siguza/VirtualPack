// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import net.minecraft.server.*;

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
    
    public void a(IInventory iinventory) // Derpnote
    {
        if(iinventory == enchantSlots)
        {
            ItemStack itemstack = iinventory.getItem(0);
            if((itemstack != null) && (itemstack.u())) // Derpnote
            {
                f = rand.nextLong();
                for(int i = 0; i < 3; i++)
                {
                    costs[i] = EnchantmentManager.a(rand, i, bookshelves, itemstack); // Derpnote
                }
                a(); // Derpnote
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
    
    public boolean a(EntityHuman entityhuman, int i) // Derpnote
    {
        ItemStack itemstack = enchantSlots.getItem(0);
        if(costs[i] > 0 && itemstack != null && entityhuman.expLevel >= costs[i])
        {
            List list = EnchantmentManager.b(rand, itemstack, costs[i]); // Derpnote
            if(list != null)
            {
                entityhuman.levelDown(costs[i]);
                Iterator iterator = list.iterator();
                while(iterator.hasNext())
                {
                    EnchantmentInstance enchantmentinstance = (EnchantmentInstance)iterator.next();
                    itemstack.addEnchantment(enchantmentinstance.enchantment, enchantmentinstance.level);
                }
                a(enchantSlots); // Derpnote
            }
            return true;
        }
        return false;
    }
    
    public void a(EntityHuman entityhuman) // Derpnote
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