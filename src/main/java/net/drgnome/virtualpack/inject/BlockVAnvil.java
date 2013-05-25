// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.inject;

import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.event.CraftEventFactory;
import net.drgnome.virtualpack.components.VAnvil;
import net.drgnome.virtualpack.util.Util;

public class BlockVAnvil extends BlockAnvil
{
    public static final int _id = 145;
    
    public static void inject()
    {
        Block.byId[_id] = null;
        new BlockVAnvil();
    }
    
    public BlockVAnvil()
    {
        super(_id);
        #FIELD_BLOCK_1#(5.0F);
        #FIELD_BLOCK_2#(Block.#FIELD_BLOCK_5#);
        #FIELD_BLOCK_3#(2000.0F);
        #FIELD_BLOCK_4#("anvil");
    }
    
    public boolean interact(World world, int x, int y, int z, EntityHuman human, int i1, float f1, float f2, float f3)
    {
        if((human instanceof EntityPlayer) && !world.isStatic)
        {
            EntityPlayer player = (EntityPlayer)human;
            Container container = CraftEventFactory.callInventoryOpenEvent(player, new VAnvil(player, x, y, z));
            if(container == null)
            {
                return true;
            }
            Util.openWindow(player, container, "Repairing", 8, 9);
        }
        return true;
    }
}