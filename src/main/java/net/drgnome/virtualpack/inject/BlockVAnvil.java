// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.inject;

import java.lang.reflect.*;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.event.CraftEventFactory;
import net.drgnome.virtualpack.components.VAnvil;
import net.drgnome.virtualpack.util.Util;

public class BlockVAnvil extends BlockAnvil
{
    public static Method _addBlock;
    ---------- SINCE 1.13 START ----------
    public static Method _setStepSound;
    ---------- SINCE 1.13 END ----------

    static
    {
        try
        {
            _addBlock = Block.class.getDeclaredMethod("#FIELD_BLOCK_6#",
            ---------- PRE 1.13 START ----------
            int.class,
            ---------- PRE 1.13 END ----------
            String.class, Block.class);
            _addBlock.setAccessible(true);

            ---------- SINCE 1.13 START ----------
            _setStepSound = Block.Info.class.getDeclaredMethod("#F_BLOCKMATERIAL_SETSTEPSOUND#", SoundEffectType.class);
            _setStepSound.setAccessible(true);
            ---------- SINCE 1.13 END ----------
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

    ---------- SINCE 1.13 START ----------
    private static Info buildInfo()
    {
        Info info = Info.#F_BLOCKMATERIAL_NEW#(Material.HEAVY, MaterialMapColor.#F_MATERIALMAPCOLOR_ANVIL#).#F_BLOCKMATERIAL_SETWHATEVER#(5.0f, 1200.0f);
        try
        {
            _setStepSound.invoke(info, #F_ANVIL_STEPSOUND#);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return info;
    }
    ---------- SINCE 1.13 END ----------

    public BlockVAnvil()
    {
        // Note to self: this comes from Block.class static code
        ---------- PRE 1.13 START ----------
        super();
        #FIELD_BLOCK_1#(5.0F);
        #FIELD_BLOCK_2#(#F_ANVIL_STEPSOUND#);
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
        ---------- PRE 1.13 END ----------
        ---------- SINCE 1.13 START ----------
        // Yes, this really changed to 1200 from 2000.
        super(buildInfo());
        try
        {
            _addBlock.invoke(null, "anvil", this);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        ---------- SINCE 1.13 END ----------
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
