// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.inject;

import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v#MC_VERSION#.event.CraftEventFactory;
import net.drgnome.virtualpack.components.VAnvil;
import net.drgnome.virtualpack.util.Util;

public class BlockVAnvil extends BlockAnvil
{
    public static final int _id = Material.ANVIL.getId();
    public static Method _addBlock;

    static
    {
        try
        {
            _addBlock = Block.class.getDeclaredMethod("#FIELD_BLOCK_6#", int.class, String.class, Block.class);
            _addBlock.setAccessible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void inject()
    {
        new BlockVAnvil();
    }

    public BlockVAnvil()
    {
        super();
        #FIELD_BLOCK_1#(5.0F);
        #FIELD_BLOCK_2#(Block.#FIELD_BLOCK_5#);
        #FIELD_BLOCK_3#(2000.0F);
        #FIELD_BLOCK_4#("anvil");
        //Block.REGISTRY.#FIELD_REGISTRYMATERIALS_1#(145, "anvil", this);
        try
        {
            _addBlock.invoke(null, 145, "anvil", this);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean interact(World world, int x, int y, int z, EntityHuman human, int i1, float f1, float f2, float f3)
    {
        if((human instanceof EntityPlayer) && !world.#FIELD_WORLD_1#)
        {
            EntityPlayer player = (EntityPlayer)human;
            Container container = CraftEventFactory.callInventoryOpenEvent(player, new VAnvil(player, x, y, z));
            if(container == null)
            {
                return true;
            }
            Util.openWindow(player, container, "Repairing", "minecraft:anvil", 9);
        }
        return true;
    }
}
