// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import net.minecraft.server.*;

import static net.drgnome.virtualpack.Lang.*;
import static net.drgnome.virtualpack.Util.*;

public class AlphaChestHelper
{
    public static void load(VPluginBase plugin, File dataFolder)
    {
        log.info("[VirtualPack] Converting AlphaChest database...");
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
                    inv = loadChestFromNBT(chestFile, plugin.getPack(playerName).getChestSize());
				}
                else if(chestFileName.endsWith(".chest"))
                {
					playerName = chestFileName.substring(0, chestFile.getName().length() - 6);
                    inv = loadChestFromTextfile(chestFile, plugin.getPack(playerName).getChestSize());
				}
                else
                {
                    continue;
                }
                plugin.getPack(playerName).alphaChest(inv);
                log.info("[VirtualPack] (AlphaChest) Loaded " + playerName + "'s chest");
			}
            catch(IOException e)
            {
				log.warning("[VirtualPack] Couldn't load AlphaChest chest file: " + chestFileName);
			}
		}
        try
        {
            dataFolder.renameTo(new File(dataFolder.getParentFile(), "chests_old"));
        }
        catch(Throwable t)
        {
            log.warning("[VirtualPack] Couldn't rename AlphaChest data folder!");
        }
        log.info("[VirtualPack] AlphaChest data loaded.");
	}
    
	private static VInv loadChestFromTextfile(File chestFile, int rows) throws IOException
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
    
	private static VInv loadChestFromNBT(File chestFile, int rows) throws IOException
    {
        VInv inv = new VInv(rows);
        DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(chestFile)));
        NBTTagCompound nbt = (NBTTagCompound)NBTBase.b(in); // Derpnote
        in.close();
        NBTTagList items = nbt.getList("Items");
        for(int i = 0; i < items.size(); i++)
        {
			NBTTagCompound item = (NBTTagCompound)items.get(i);
			byte slot = item.getByte("Slot");
			if(slot >= 0 && slot < (rows * 9))
            {
                inv.setItem(slot, ItemStack.a(item)); // Derpnote
			}
		}
        return inv;
	}
}