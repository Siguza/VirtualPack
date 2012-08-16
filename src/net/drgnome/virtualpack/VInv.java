// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.*;

import static net.drgnome.virtualpack.Util.*;

public class VInv implements IInventory
{
    private ItemStack[] contents = new ItemStack[0];
    
    public VInv(int size)
    {
        contents = new ItemStack[size];
    }
    
    public VInv(int size, String data[])
    {
        this(size, data, 0);
    }
    
    public VInv(int size, String data[], int offset)
    {
        contents = new ItemStack[size];
        int max = data.length - offset < size ? data.length - offset : size;
        for(int i = 0; i < max; i++)
        {
            contents[i] = stringToItemStack(data[i + offset]);
        }
    }
    
    public String[] save()
    {
        String string[] = new String[contents.length];
        for(int i = 0; i < contents.length; i++)
        {
            string[i] = itemStackToString(contents[i]);
        }
        return string;
    }
    
    public int getSize()
    {
        return contents.length;
    }
    
    public ItemStack getItem(int slot)
    {
        return (slot < contents.length) && (slot >= 0) ? contents[slot] : null;
    }
    
    public ItemStack splitStack(int slot, int size)
    {
        if((slot < contents.length) && (contents[slot] != null))
        {
            if(contents[slot].count <= size)
            {
                ItemStack item = contents[slot];
                contents[slot] = null;
                return item;
            }
            ItemStack item = contents[slot].a(size); // Derpnote
            if(contents[slot].count <= 0)
            {
                contents[slot] = null;
            }
            return item;
        }
        return null;
    }
    
    public ItemStack splitWithoutUpdate(int slot)
    {
        if((slot < contents.length) && (contents[slot] != null))
        {
            ItemStack item = contents[slot];
            contents[slot] = null;
            return item;
        }
        return null;
    }
    
    public void setItem(int slot, ItemStack item)
    {
        contents[slot] = item;
        if((item != null) && (item.count > getMaxStackSize()))
        {
            item.count = getMaxStackSize();
        }
    }
    
    public String getName()
    {
        return "Blubb";
    }
    
    public int getMaxStackSize()
    {
        return 64;
    }
    
    public void setMaxStackSize(int i)
    {
    }
    
    public boolean a(EntityHuman entityhuman) // Derpnote
    {
        return true;
    }
    
    public List<HumanEntity> getViewers()
    {
        return new ArrayList<HumanEntity>();
    }
    
    public InventoryHolder getOwner()
    {
        return null;
    }
    
    public ItemStack[] getContents()
    {
        return contents;
    }
    
    public void update()
    {
    }
    
    public void f() // Derpnote
    {
    }
    
    public void startOpen()
    {
    }
    
    public void onOpen(CraftHumanEntity paramCraftHumanEntity)
    {
    }
    
    public void onClose(CraftHumanEntity paramCraftHumanEntity)
    {
    }
}