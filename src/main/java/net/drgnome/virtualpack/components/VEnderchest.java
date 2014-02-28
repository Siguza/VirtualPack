// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.Config;

public class VEnderchest extends VChest
{
    private final org.bukkit.entity.Player _bukkitPlayer;
    
    public VEnderchest(EntityPlayer player, InventoryEnderChest ec, org.bukkit.entity.Player bukkitPlayer, boolean canEdit)
    {
        super(player, ec, canEdit);
        _bukkitPlayer = bukkitPlayer;
    }
    
    public void #FIELD_CONTAINER_5#(EntityHuman entityhuman) // Derpnote
    {
        _bukkitPlayer.saveData();
    }
}