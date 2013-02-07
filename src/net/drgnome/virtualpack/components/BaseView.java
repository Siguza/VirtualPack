// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.lang.reflect.*;
import org.bukkit.inventory.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.*;
import static net.drgnome.virtualpack.util.Global.*;

public class BaseView extends InventoryView
{
    protected Player _player;
    protected BaseInv _inv;
    
    public BaseView(Player player, BaseInv inv)
    {
        _player = player;
        _inv = inv;
    }
    
    public void setItem(int slot, ItemStack item)
    {
        super.setItem(slot, item);
        try
        {
            Method m = _player.getClass().getMethod("updateInventory");
            m.setAccessible(true);
            m.invoke(_player);
        }
        catch(Throwable t)
        {
            warn();
            t.printStackTrace();
        }
    }
    
    public Inventory getTopInventory()
    {
        return _inv;
    }
    
    public Inventory getBottomInventory()
    {
        return _player.getInventory();
    }
    
    public HumanEntity getPlayer()
    {
        return _player;
    }
    
    public InventoryType getType()
    {
        return _inv.getType();
    }
    
    public boolean allowClick(int slot, boolean right, boolean shift)
    {
        return true;
    }
}