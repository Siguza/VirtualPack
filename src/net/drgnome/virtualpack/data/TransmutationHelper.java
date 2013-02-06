// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.data;

import java.io.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.inventory.*;
import net.drgnome.virtualpack.item.*;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class TransmutationHelper
{
    private static ArrayList<ValuedItemStack> _list;
    private static boolean _notify;
    private static boolean _override;
    
    public static void init()
    {
        _notify = Config.bool("transmutation.notify-mismatch");
        _override = Config.bool("transmutation.override-mismatch");
        _list = new ArrayList<ValuedItemStack>();
        try
        {
            for(String[] array : Util.readIni(new File(_plugin.getDataFolder(), "transmutation.ini")))
            {
                double value;
                try
                {
                    value = Double.parseDouble(array[1]);
                    if(value < 0)
                    {
                        value = 0;
                    }
                }
                catch(Throwable t)
                {
                    value = 0D;
                }
                _list.add(new ValuedItemStack(array[0], value));
            }
            ArrayList<QuantitativeRecipe> recipes = getAllRecipes();
            while(true)
            {
                int size = recipes.size();
                for(int i = 0; i < recipes.size(); i++)
                {
                    QuantitativeRecipe rec = recipes.get(i);
                    if(addMapping(rec))
                    {
                        recipes.remove(rec);
                        i--;
                    }
                    /*switch(addMapping(rec))
                     {
                     case 2:
                     failed.add(rec); // No break!
                     case 1:
                     recipes.remove(rec);
                     i--;
                     }*/
                }
                if(recipes.size() == size)
                {
                    break;
                }
            }
            // Some more stuff
            sort();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    private static ArrayList<QuantitativeRecipe> getAllRecipes()
    {
        ArrayList<Recipe> base = new ArrayList<Recipe>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while(iterator.hasNext())
        {
            base.add(iterator.next());
        }
        iterator = null; // Don't wanna block it
        ArrayList<QuantitativeRecipe> recipes = new ArrayList<QuantitativeRecipe>();
        for(Recipe recipe : base)
        {
            HashMap<ComparativeItemStack, Integer> ingredients = new HashMap<ComparativeItemStack, Integer>();
            if(recipe instanceof ShapelessRecipe)
            {
                for(ItemStack in : ((ShapelessRecipe)recipe).getIngredientList().toArray(new ItemStack[0]))
                {
                    addToMap(ingredients, in);
                }
            }
            else if(recipe instanceof ShapedRecipe)
            {
                ShapedRecipe re = (ShapedRecipe)recipe;
                Map<Character, ItemStack> map = re.getIngredientMap();
                for(String shape : re.getShape())
                {
                    for(char c : shape.toCharArray())
                    {
                        addToMap(ingredients, map.get(Character.valueOf(c)));
                    }
                }
            }
            else
            {
                // Notify human
                continue;
            }
            recipes.add(new QuantitativeRecipe(recipe.getResult(), ingredients));
        }
        return recipes;
    }
    
    private static void addToMap(Map<ComparativeItemStack, Integer> map, ItemStack item)
    {
        ComparativeItemStack key = null;
        for(ComparativeItemStack stack : map.keySet().toArray(new ComparativeItemStack[0]))
        {
            if(stack.matches(item))
            {
                key = stack;
                break;
            }
        }
        if(key == null)
        {
            map.put(new ComparativeItemStack(item), 1);
        }
        else
        {
            map.put(key, map.get(key) + 1);
        }
    }
    
    private static boolean addMapping(QuantitativeRecipe recipe)
    {
        boolean mapped = isMapped(recipe.getResult());
        if(mapped && !Config.bool("notify-mismatch") && !Config.bool("override-mismatch"))
        {
            return true;
        }
        double value = 0D;
        for(ComparativeItemStack stack : recipe.getIngredients().keySet())
        {
            
        }
        return false; // TODO
    }
    
    /*private int addMapping(Recipe recipe)
    {
        ItemStack item = recipe.getResult();
        if(isMapped(item))
        {
            return 1;
        }
        HashMap<ComparativeItemStack, Integer> ingredients = new HashMap<ItemStack, Integer>();
        if(recipe instanceof ShapelessRecipe)
        {
            for(ItemStack in : ((ShapelessRecipe)recipe).getIngredientList().toArray(new ItemStack[0]))
            {
                addToMap(ingredients, in);
            }
        }
        else if(recipe instanceof ShapedRecipe)
        {
            ShapedRecipe re = (ShapedRecipe)recipe;
            Map<Character, ItemStack> map = re.getIngredientMap();
            for(String shape : re.getShape())
            {
                for(char c = shape.toCharArray())
                {
                    addToMap(ingredients, map.get(Character.valueOf(c)));
                }
            }
        }
        else
        {
            return 2;
        }
        for(ComparativeItemStack stack : ingredients.keySet().toArray(new ComparativeItemStack[0]))
        {
            if()
        }
    }
    
    private static double initValue(ItemStack item)
    {
        return initValue(new ComparativeItemStack(item));
    }
    
    private static double initValue(ComparativeItemStack item)
    {
        _defining.add(item);
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                _defining.remove(stack);
                return stack.getValue();
            }
        }
        for(Recipe recipe : Bukkit.getRecipesFor().toArray(new Recipe[0]))
        {
            HashMap<ComparativeItemStack, Integer> ingredients = new HashMap<ItemStack, Integer>();
            if(recipe instanceof ShapelessRecipe)
            {
                for(ItemStack ing : ((ShapelessRecipe)recipe).getIngredientList().toArray(new ItemStack[0]))
                {
                    if(ingredients.containsKey(ing))
                    {
                        
                    }
                    else
                    {
                        ingredients.put(ing);
                    }
                }
            }
            else if(recipe instanceof ShapedRecipe)
            {
                ShapedRecipe re = (ShapedRecipe)recipe;
                Map<Character, ItemStack> map = re.getIngredientMap();
                for(String shape : re.getShape())
                {
                    for(char c = shape.toCharArray())
                    {
                        
                    }
                }
            }
            else
            {
                short meta = item.getDurability();
                _log.info(Lang.get("matter.customIngredient", item.getTypeId() + (meta > 0 ? ":" + meta : ""), recipe.getClass().getName()));
                continue;
            }
        }
        _defining.remove(item);
    }*/
    
    private static void sort()
    {
        ArrayList<ValuedItemStack> tmpList = _list;
        _list = new ArrayList<ValuedItemStack>();
        while(tmpList.size() > 0)
        {
            ValuedItemStack tmp = null;
            for(ValuedItemStack stack : tmpList.toArray(new ValuedItemStack[0]))
            {
                if((tmp == null) || (stack.getValue() > tmp.getValue()))
                {
                    tmp = stack;
                }
            }
            if(tmp != null)
            {
                _list.add(tmp);
                tmpList.remove(tmp);
            }
        }
    }
    
    private static boolean isMapped(ItemStack item)
    {
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return true;
            }
        }
        return false;
    }
    
    private static boolean isMapped(ComparativeItemStack item)
    {
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return true;
            }
        }
        return false;
    }
    
    public static double getValue(ItemStack item)
    {
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return stack.getValue();
            }
        }
        return 0D;
    }
    
    public static double getValue(ComparativeItemStack item)
    {
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return stack.getValue();
            }
        }
        return 0D;
    }
}