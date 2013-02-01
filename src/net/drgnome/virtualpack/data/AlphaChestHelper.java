// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.*;
import net.drgnome.virtualpack.components.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class AlphaChestHelper
{
    public static void check()
    {
        File chests = new File(_plugin.getDataFolder(), "chests");
        if(chests.exists() && chests.isDirectory())
        {
            load(chests);
        }
    }
    
    private static void load(File dataFolder)
    {
        _log.info("[VirtualPack] Converting AlphaChest database...");
        String world = Config.string("import-world").length() > 0 ? Config.string("import-world") : "*";
		for(File chestFile : dataFolder.listFiles())
        {
			String chestFileName = chestFile.getName();
			try
            {
                String playerName = "";
                VInv inv;
				if(chestFileName.endsWith(".chest.nbt"))
                {
					playerName = chestFileName.substring(0, chestFile.getName().length() - 10);
                    inv = loadFromNBT(chestFile, _plugin.getPack(world, playerName).getChestSize());
				}
                else if(chestFileName.endsWith(".chest"))
                {
					playerName = chestFileName.substring(0, chestFile.getName().length() - 6);
                    inv = loadFromTextfile(chestFile, _plugin.getPack(world, playerName).getChestSize());
				}
                else
                {
                    continue;
                }
                _plugin.getPack(world, playerName).addInv(inv);
                _log.info("[VirtualPack] (AlphaChest) Loaded " + playerName + "'s chest");
			}
            catch(IOException e)
            {
				_log.warning("[VirtualPack] Couldn't load AlphaChest chest file: " + chestFileName);
			}
		}
        try
        {
            dataFolder.renameTo(new File(dataFolder.getParentFile(), "chests_old"));
        }
        catch(Throwable t)
        {
            _log.warning("[VirtualPack] Couldn't rename AlphaChest data folder!");
        }
        _log.info("[VirtualPack] AlphaChest data loaded.");
	}
    
	private static VInv loadFromTextfile(File chestFile, int rows) throws IOException
    {
        VInv inv = new VInv(rows);
        BufferedReader in = new BufferedReader(new FileReader(chestFile));
		String line;
		int slot = 0;
		while((line = in.readLine()) != null)
        {
            if(slot >= (rows * 9))
            {
                break;
            }
			if(!line.equals(""))
            {
				final String[] parts = line.split(":");
				try
                {
					int type = Integer.parseInt(parts[0]);
					int amount = Integer.parseInt(parts[1]);
					short damage = Short.parseShort(parts[2]);
					if((type > 0) && (amount > 0))
                    {
						inv.setItem(slot, new ItemStack(type, amount, damage));
					}
				}
                catch(NumberFormatException e)
                {
				}
				slot++;
			}
		}
		in.close();
        return inv;
	}
    
	private static VInv loadFromNBT(File chestFile, int rows) throws IOException
    {
        VInv inv = new VInv(rows);
        DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(chestFile)));
        NBTTagCompound nbt = (NBTTagCompound)NBTBase.#FIELD_NBTBASE_1#(in);
        in.close();
        NBTTagList items = nbt.getList("Items");
        for(int i = 0; i < items.size(); i++)
        {
			NBTTagCompound item = (NBTTagCompound)items.get(i);
			byte slot = item.getByte("Slot");
			if(slot >= 0 && slot < (rows * 9))
            {
                inv.setItem(slot, ItemStack.#FIELD_ITEMSTACK_1#(item));
			}
		}
        return inv;
	}
}