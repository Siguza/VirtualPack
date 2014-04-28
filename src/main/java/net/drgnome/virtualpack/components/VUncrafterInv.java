// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import org.bukkit.Material;
import net.minecraft.server.v#MC_VERSION#.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class VUncrafterInv extends VInv implements VProcessing
{
    private static final List _recipes = CraftingManager.getInstance().getRecipes();
    private static final Field[] _fields = new Field[2];
    private final EntityPlayer _player;
    private ArrayList<Integer> _slotUpdate = new ArrayList<Integer>();
    
    static
    {
        try
        {
            _fields[0] = ShapelessRecipes.class.getDeclaredField("ingredients");
            _fields[0].setAccessible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            _fields[1] = ShapedRecipes.class.getDeclaredField("items");
            _fields[1].setAccessible(true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public VUncrafterInv(EntityPlayer player)
    {
        super(2);
        _player = player;
    }
    
    public void setItem(int slot, ItemStack item)
    {
        if((slot >= 0) && (slot < 9))
        {
            _slotUpdate.add(slot);
        }
        super.setItem(slot, item);
    }
    
    public void process()
    {
        for(Integer i : _slotUpdate)
        {
            processSlot(i);
        }
        _slotUpdate.clear();
    }
    
    private void processSlot(int slot)
    {
        ItemStack old;
        ItemStack result;
        Object tmp;
        IRecipe recipe;
        if(contents[slot] != null)
        {
            old = Util.copy_old(contents[slot]);
            for(int i = -1; i < _recipes.size(); i++)
            {
                ItemStack[] back = null;
                if(i == -1)
                {
                    if(Item.#FIELD_ITEM_7#(old.getItem()) == Material.ENCHANTED_BOOK.getId())
                    {
                        back = new ItemStack[]{new ItemStack(Item.#FIELD_ITEM_8#(Material.BOOK.getId()), old.count, old.getData())};
                    }
                    else
                    {
                        NBTTagList ench = old.getEnchantments();
                        if((ench == null) || (ench.size() == 0))
                        {
                            continue;
                        }
                        else if((!old.getItem().#FIELD_ITEM_5#() && (old.getData() != 0)) || !Config.bool("uncraft-enchanted"))
                        {
                            break;
                        }
                        back = new ItemStack[ench.size() + 1];
                        back[0] = Util.copy_old(old);
                        back[0].getTag().remove("ench");
                        for(int j = 0; j < ench.size(); j++)
                        {
                            back[j + 1] = new ItemStack(Item.#FIELD_ITEM_8#(Material.ENCHANTED_BOOK.getId()), 1, 0);
                            NBTTagCompound tag = new NBTTagCompound();
                            NBTTagList elist = new NBTTagList();
                            elist.add(ench.get(j));
                            tag.set("StoredEnchantments", elist);
                            back[j + 1].setTag(tag);
                        }
                    }
                    result = Util.copy_old(old);
                }
                else
                {
                    tmp = _recipes.get(i);
                    if((tmp == null) || !(tmp instanceof IRecipe))
                    {
                        continue;
                    }
                    recipe = (IRecipe)tmp;
                    result = Util.copy_old(recipe.#FIELD_IRECIPE_1#()); // Derpnote
                    if((result == null) || (Item.#FIELD_ITEM_7#(result.getItem()) != Item.#FIELD_ITEM_7#(old.getItem())) || (result.getData() != old.getData()))
                    {
                        continue;
                    }
                    if(recipe instanceof ShapelessRecipes)
                    {
                        back = Util.copy_old(((List<ItemStack>)access(0, recipe)).toArray(new ItemStack[0]));
                    }
                    else if(recipe instanceof ShapedRecipes)
                    {
                        back = Util.copy_old((ItemStack[])access(1, recipe));
                    }
                    else
                    {
                        _log.warning("[VirtualPack] Uncrafter: Skipping unknown recipe type: " + recipe.getClass().getName());
                        continue;
                    }
                }
                for(int j = 0; j < back.length; j++)
                {
                    if((back[j] != null) && ((back[j].getData() < 0) || (back[j].getData() >= 0x7FFF)))
                    {
                        back[j].setData(0);
                    }
                }
                ItemStack[] test = new ItemStack[9];
                for(int j = 0; j < test.length; j++)
                {
                    test[j] = Util.copy_old(contents[j + 9]);
                }
                ItemStack[] test1 = Util.copy_old(test);
                boolean success;
                for(; old.count >= result.count; old.count -= result.count)
                {
                    success = true;
                    outside:
                    for(int j = 0; j < back.length; j++)
                    {
                        if((back[j] == null) || (back[j].getItem().#FIELD_ITEM_1#())) // Derpnote
                        {
                            continue;
                        }
                        for(int k = 0; k < test1.length; k++)
                        {
                            if(test1[k] == null)
                            {
                                test1[k] = Util.copy_old(back[j]);
                                test1[k].count = 1;
                                continue outside;
                            }
                            else if((Item.#FIELD_ITEM_7#(test1[k].getItem()) == Item.#FIELD_ITEM_7#(back[j].getItem())) && (test1[k].getData() == back[j].getData()) && (test1[k].count < test1[k].getItem().getMaxStackSize()))
                            {
                                test1[k].count += 1;
                                continue outside;
                            }
                        }
                        success = false;
                        break;
                    }
                    if(success)
                    {
                        test = Util.copy_old(test1);
                    }
                    else
                    {
                        break;
                    }
                }
                for(int j = 0; j < test.length; j++)
                {
                    contents[j + 9] = test[j];
                }
                if(old.count == 0)
                {
                    contents[slot] = null;
                }
                else
                {
                    contents[slot].count = old.count;
                }
                break;
            }
        }
    }
    
    private static Object access(int i, Object handle)
    {
        try
        {
            return _fields[i].get(handle);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
}