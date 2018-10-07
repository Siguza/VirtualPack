// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v#MC_VERSION#.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.util.*;

public class VInv implements VIInventory
{
    protected ItemStack[] contents;
    private long lastUpdate;
    private int _maxStack = 64;
    private String _name = "";
    ---------- SINCE 1.11 START ----------
    private ProxyList<ItemStack> proxy;
    ---------- SINCE 1.11 END ----------

    public VInv(int rows)
    {
        contents = new ItemStack[rows * 9];
        lastUpdate = System.currentTimeMillis();
        ---------- SINCE 1.11 START ----------
        proxy = new ProxyList<ItemStack>(contents, #F_ITEMSTACK_NULL#);
        ---------- SINCE 1.11 END ----------
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
        ---------- SINCE 1.11 START ----------
        proxy = new ProxyList<ItemStack>(contents, #F_ITEMSTACK_NULL#);
        ---------- SINCE 1.11 END ----------
    }

    public void resize(int size)
    {
        if(contents.length != size)
        {
            contents = Arrays.copyOf(contents, size);
            ---------- SINCE 1.11 START ----------
            proxy = new ProxyList<ItemStack>(contents, #F_ITEMSTACK_NULL#);
            ---------- SINCE 1.11 END ----------
        }
    }

    public int getSize()
    {
        return contents.length;
    }

    public ItemStack getItem(int slot)
    {
        ItemStack item = (slot < contents.length) && (slot >= 0) ? contents[slot] : null;
        return item == null ? #F_ITEMSTACK_NULL# : item;
    }

    public ItemStack splitStack(int slot, int size)
    {
        if((slot < contents.length) && (contents[slot] != null))
        {
            ---------- PRE 1.11 START ----------
            if(contents[slot].count <= size)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(contents[slot].getCount() <= size)
            ---------- SINCE 1.11 END ----------
            {
                ItemStack item = Util.copy_old(contents[slot]);
                setItem(slot, #F_ITEMSTACK_NULL#);
                return item;
            }
            ItemStack item = contents[slot].#FIELD_ITEMSTACK_3#(size); // Derpnote
            ---------- PRE 1.11 START ----------
            if(contents[slot].count <= 0)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(contents[slot].getCount() <= 0)
            ---------- SINCE 1.11 END ----------
            {
                setItem(slot, #F_ITEMSTACK_NULL#);
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
        if(item == #F_ITEMSTACK_NULL#)
        {
            item = null;
        }
        contents[slot] = item;
        int max = getMaxStackSize();
        ---------- PRE 1.11 START ----------
        if((item != null) && (item.count > max))
            item.count = max;
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        if((item != null) && (item.getCount() > max))
            item.setCount(max);
        ---------- SINCE 1.11 END ----------
        lastUpdate = System.currentTimeMillis();
    }

    public boolean isEmpty()
    {
        for(ItemStack i : contents)
        {
            if(i != null)
            {
                return false;
            }
        }
        return true;
    }

    public long getLastUpdate()
    {
        return lastUpdate;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public IChatBaseComponent getScoreboardDisplayName()
    {
        return new ChatComponentText(_name);
    }

    public boolean hasCustomName()
    {
        return true;
    }

    public int getMaxStackSize()
    {
        return _maxStack;
    }

    public void setMaxStackSize(int i)
    {
        _maxStack = i;
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

    public Location getLocation()
    {
        return null;
    }

    public ItemStack[] #F_GET_RAW_CONTENTS#()
    {
        return contents;
    }

    ---------- SINCE 1.11 START ----------
    public List<ItemStack> getContents()
    {
        return proxy;
    }
    ---------- SINCE 1.11 END ----------

    public boolean #FIELD_IINVENTORY_3#(int slot, ItemStack item)
    {
        return true;
    }

    public int getProperty(int i)
    {
        return 0;
    }

    public void #FIELD_IINVENTORY_5#(int i, int j)
    {
    }

    public int #FIELD_IINVENTORY_6#()
    {
        return 0;
    }

    ---------- PRE 1.11 START ----------
    public void #FIELD_IINVENTORY_7#()
    {
        clear();
    }
    ---------- PRE 1.11 END ----------

    public void update()
    {
    }

    public void closeContainer(EntityHuman human)
    {
    }

    public void startOpen(EntityHuman human)
    {
    }

    public void onOpen(CraftHumanEntity paramCraftHumanEntity)
    {
    }

    public void onClose(CraftHumanEntity paramCraftHumanEntity)
    {
    }

    ---------- SINCE 1.11 START ----------
    public boolean #F_INV_IS_EMPTY#()
    {
        return isEmpty();
    }
    ---------- SINCE 1.11 END ----------

    ---------- SINCE 1.13 START ----------
    public IChatBaseComponent getDisplayName()
    {
        return getScoreboardDisplayName();
    }

    public IChatBaseComponent getCustomName()
    {
        return getScoreboardDisplayName();
    }
    ---------- SINCE 1.13 END ----------
}
