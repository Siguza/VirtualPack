// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Bukkit;
import static net.drgnome.virtualpack.util.Global._plugin;

public abstract class VContainer extends ContainerChest
{
    protected EntityPlayer player;

    public VContainer(EntityPlayer player, IInventory data)
    {
        super(player.inventory, data, player);
        this.checkReachable = false;
        this.player = player;
    }

    public final ItemStack #FIELD_CONTAINER_11#(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        ItemStack item;
        if(allowClick(slot, mouse, meta, human))
        {
            item = super.#FIELD_CONTAINER_11#(slot, mouse, meta, human);
            IInventory container = this.#FIELD_CONTAINERCHEST_1#();
            if(container instanceof VProcessing)
            {
                ((VProcessing)container).process();
            }
        }
        else
        {
            item = human.inventory.getCarried();
        }
        update();
        return item;
    }

    public boolean allowClick(int slot, int mouse, #F_INVCLICK_META# meta, EntityHuman human)
    {
        return true;
    }

    protected int toInventorySlot(int slot)
    {
        return (slot >= 27) ? (slot - 27) : (slot + 9);
    }

    protected final void update()
    {
        update(this.player);
    }

    public static void update(final EntityPlayer player)
    {
        Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, new Runnable()
        {
            public void run()
            {
                player.updateInventory(player.activeContainer);
            }
        });
    }
}
