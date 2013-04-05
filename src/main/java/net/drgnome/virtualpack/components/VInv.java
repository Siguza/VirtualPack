// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import org.bukkit.craftbukkit.v1_5_R2.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import net.minecraft.server.v1_5_R2.*;
import net.drgnome.virtualpack.util.*;

public class VInv implements IInventory
{
    private ItemStack[] contents = new ItemStack[0];
    private long lastUpdate;
    
    public VInv(int rows)
    {
        contents = new ItemStack[rows * 9];
        lastUpdate = System.currentTimeMillis();
    }
    
    public VInv(int rows, String data[])
    {
        this(rows);
        for(int i = 0; i < Util.min(data.length, contents.length); i++)
        {
            contents[i] = Util.stringToItemStack(data[i]);
        }
    }
    
    public VInv(int rows, ItemStack[] items)
    {
        this(rows);
        int max = (contents.length > items.length) ? items.length : contents.length;
        for(int i = 0; i < max; i++)
        {
            contents[i] = items[i];
        }
    }
    
    public String[] save()
    {
        String string[] = new String[contents.length];
        for(int i = 0; i < contents.length; i++)
        {
            string[i] = Util.itemStackToString(contents[i]);
        }
        return string;
    }
    
    public void clear()
    {
        contents = new ItemStack[contents.length];
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
                ItemStack item = Util.copy_old(contents[slot]);
                setItem(slot, null);
                return item;
            }
            ItemStack item = contents[slot].a(size); // Derpnote
            if(contents[slot].count <= 0)
            {
                setItem(slot, null);
            }
            return item;
        }
        return null;
    }
    
    public ItemStack splitWithoutUpdate(int slot)
    {
        if((slot < contents.length) && (contents[slot] != null))
        {
            ItemStack item = Util.copy_old(contents[slot]);
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
        lastUpdate = System.currentTimeMillis();
    }
    
    public long getLastUpdate()
    {
        return lastUpdate;
    }
    
    public String getName()
    {
        return "Blubb";
    }
    
    public boolean c() // Has name?
    {
        return true;
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
    
    public boolean b(int slot, ItemStack item)
    {
        return true;
    }
    
    public void update()
    {
    }
    
    public void g() // Derpnote
    {
    }
    
    public void startOpen() // Derpnote
    {
    }
    
    public void onOpen(CraftHumanEntity paramCraftHumanEntity)
    {
    }
    
    public void onClose(CraftHumanEntity paramCraftHumanEntity)
    {
    }
}