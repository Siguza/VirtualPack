// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.lang.reflect.Method;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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
        updateInv();
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
    
    public final boolean allowClick(int slot, boolean right, boolean shift)
    {
        boolean flag = _inv.allowClick(_player, slot, right, shift);
        updateInv();
        return flag;
    }
    
    public void updateInv()
    {
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
}