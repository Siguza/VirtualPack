// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import net.minecraft.server.v#MC_VERSION#.*;
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
                ItemStack item = Util.copy(contents[slot]);
                setItem(slot, null);
                return item;
            }
            ItemStack item = contents[slot].#FIELD_ITEMSTACK_3#(size); // Derpnote
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
            ItemStack item = Util.copy(contents[slot]);
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
    
    public int getMaxStackSize()
    {
        return 64;
    }
    
    public void setMaxStackSize(int i)
    {
    }
    
    public boolean #FIELD_IINVENTORY_1#(EntityHuman entityhuman) // Derpnote
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
    
    public void #FIELD_IINVENTORY_2#() // Derpnote
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