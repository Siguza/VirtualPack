// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;

import #PACKAGE_CRAFTBUKKIT#.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.server.v#MC_VERSION#.*;

import static net.drgnome.virtualpack.Util.*;

public class VInv implements IInventory
{
    private ItemStack[] contents = new ItemStack[0];
    private long lastUpdate;
    
    public VInv(int rows)
    {
        contents = new ItemStack[rows * 9];
        lastUpdate = System.currentTimeMillis();
    }
    
    public VInv(int rows, String data[]) throws Throwable
    {
        this(rows, data, 0);
    }
    
    public VInv(int rows, String data[], int offset) throws Throwable
    {
        this(rows);
        int max = data.length - offset < (rows * 9) ? data.length - offset : (rows * 9);
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
                ItemStack item = copy(contents[slot]);
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
            ItemStack item = copy(contents[slot]);
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
    
    public void #FIELD_IINVENTORY_3#() // Derpnote
    {
    }
    
    public void onOpen(CraftHumanEntity paramCraftHumanEntity)
    {
    }
    
    public void onClose(CraftHumanEntity paramCraftHumanEntity)
    {
    }
}