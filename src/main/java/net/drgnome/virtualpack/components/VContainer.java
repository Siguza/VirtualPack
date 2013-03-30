// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;

public abstract class VContainer extends ContainerChest implements VGUI
{
    protected EntityPlayer player;
    
    public VContainer(EntityPlayer player, IInventory data)
    {
        super(player.inventory, data);
        this.checkReachable = false;
        this.player = player;
    }
    
    public final ItemStack clickItem(int slot, int mouse, int shift, EntityHuman human)
    {
        ItemStack item;
        if(allowClick(slot, mouse, shift, human))
        {
            item = super.clickItem(slot, mouse, shift, human);
        }
        else
        {
            item = human.inventory.getCarried();
        }
        update();
        return item;
    }
    
    public boolean allowClick(int slot, int mouse, int shift, EntityHuman human)
    {
        return true;
    }
    
    protected int toInventorySlot(int slot)
    {
        return (slot >= 27) ? (slot - 27) : (slot + 9);
    }
    
    protected void update()
    {
        player.updateInventory(player.activeContainer);
    }
}