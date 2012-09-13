// Bukkit Plugin "VirtualPack" by Siguza
// This software is distributed under the following license:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack;

import java.util.*;
import java.lang.reflect.*;

import net.minecraft.server.*;

import static net.drgnome.virtualpack.Util.*;

public class VUncrafterInv extends VInv
{
    
    public VUncrafterInv()
    {
        super(2);
    }
    
    public void setItem(int slot, ItemStack item)
    {
        if((item != null) && (slot < 9))
        {
            List list = CraftingManager.getInstance().getRecipes();
            Object tmp;
            Object tmp2;
            IRecipe recipe;
            ItemStack result;
            for(int i = 0; i < list.size(); i++)
            {
                tmp = list.get(i);
                if(!(tmp instanceof IRecipe))
                {
                    continue;
                }
                recipe = (IRecipe)tmp;
                if(recipe == null)
                {
                    continue;
                }
                result = copy(recipe.b()); // Derpnote
                if((result == null) || (result.id != item.id) || (result.getData() != item.getData()))
                {
                    continue;
                }
                ItemStack back[] = new ItemStack[0];
                if(recipe instanceof ShapedRecipes)
                {
                    try
                    {
                        Field field = net.minecraft.server.ShapedRecipes.class.getDeclaredField("items");
                        field.setAccessible(true);
                        tmp2 = field.get(recipe);
                        if(!(tmp2 instanceof ItemStack[]))
                        {
                            break;
                        }
                        ItemStack[] tmp3 = (ItemStack[])tmp2;
                        back = new ItemStack[tmp3.length];
                        for(int j = 0; j < tmp3.length; j++)
                        {
                            back[j] = copy(tmp3[j]);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                else if(recipe instanceof ShapelessRecipes)
                {
                    try
                    {
                        Field field = net.minecraft.server.ShapelessRecipes.class.getDeclaredField("ingredients");
                        field.setAccessible(true);
                        tmp2 = field.get(recipe);
                        if(!(tmp2 instanceof List))
                        {
                            break;
                        }
                        Object obj[] = ((List)tmp2).toArray();
                        back = new ItemStack[obj.length];
                        for(int j = 0; j < obj.length; j++)
                        {
                            back[j] = copy((ItemStack)obj[j]);
                        }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
                for(int j = 0; j < back.length; j++)
                {
                    if((back[j] != null) && (back[j].getData() < 0))
                    {
                        back[j].setData(0);
                    }
                }
                ItemStack abc;
                ItemStack test[] = new ItemStack[9];
                for(int j = 0; j < test.length; j++)
                {
                    abc = getItem(j + 9);
                    test[j] = copy(abc);
                }
                boolean success;
                ItemStack test1[] = copy(test);
                for(; item.count >= result.count; item.count -= result.count)
                {
                    success = true;
                    for(int j = 0; j < back.length; j++)
                    {
                        if((back[j] == null) || (Item.byId[back[j].id].r())) // Derpnote
                        {
                            continue;
                        }
                        success = false;
                        for(int k = 0; k < test1.length; k++)
                        {
                            
                            if(test1[k] == null)
                            {
                                test1[k] = copy(back[j]);
                                test1[k].count = 1;
                                success = true;
                                break;
                            }
                            else if((test1[k].id == back[j].id) && (test1[k].getData() == back[j].getData()) && (test1[k].count < Item.byId[test1[k].id].getMaxStackSize()))
                            {
                                test1[k].count += 1;
                                success = true;
                                break;
                            }
                        }
                        if(!success)
                        {
                            break;
                        }
                    }
                    test = copy(test1);
                }
                for(int j = 0; j < test.length; j++)
                {
                    super.setItem(j + 9, test[j]);
                }
                break;
            }
        }
        if((item != null) && (item.count <= 0))
        {
            item = null;
        }
        if(item == null)
        {
            super.setItem(slot, null);
        }
        else
        {
            super.setItem(slot, copy(item));
        }
    }
}