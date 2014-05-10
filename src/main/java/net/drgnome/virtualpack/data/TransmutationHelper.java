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

    public static void init()
    {
        _list = new ArrayList<ValuedItemStack>();
        try
        {
            for(String[] array : Util.readIni(new File(_plugin.getDataFolder(), "transmutation.ini")))
            {
                double value;
                try
                {
                    value = Util.parseBigDouble(array[1]);
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
            QuantitativeRecipe[] all = recipes.toArray(new QuantitativeRecipe[0]);
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
                }
                if(recipes.size() == size)
                {
                    break;
                }
            }
            if(Config.bool("transmutation.notify-mismatch"))
            {
                for(QuantitativeRecipe recipe : all)
                {
                    double val1 = getValue(recipe.getResult()) * (double)recipe.getResult().getAmount();
                    double val2 = calculateValue(recipe.getIngredients());
                    if((val1 != val2) && (val1 > 0) && (val2 > 0))
                    {
                        short meta = recipe.getResult().getDurability();
                        _log.warning(Lang.get(null, "matter.mismatch", recipe.getResult().getTypeId() + (meta > 0 ? ":" + meta : ""), Util.printDouble(val1), Util.printDouble(val2)));
                    }
                }
            }
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
            if(recipe.getResult().getAmount() <= 0)
            {
                continue;
            }
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
            else if(recipe instanceof FurnaceRecipe)
            {
                addToMap(ingredients, ((FurnaceRecipe)recipe).getInput());
            }
            else
            {
                short meta = recipe.getResult().getDurability();
                _log.info(Lang.get(null, "matter.customIngredient", recipe.getResult().getTypeId() + (meta > 0 ? ":" + meta : ""), recipe.getClass().getName()));
                continue;
            }
            recipes.add(new QuantitativeRecipe(recipe.getResult(), ingredients));
        }
        return recipes;
    }

    private static void addToMap(Map<ComparativeItemStack, Integer> map, ItemStack item)
    {
        if(item == null)
        {
            return;
        }
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
        ItemStack result = recipe.getResult();
        if(isDefined(result))
        {
            return true;
        }
        double value = calculateValue(recipe.getIngredients());
        if(value < 0)
        {
            return false;
        }
        _list.add(new ValuedItemStack(result, value / (double)result.getAmount()));
        return true;
    }

    private static double calculateValue(HashMap<ComparativeItemStack, Integer> map)
    {
        double value = 0D;
        for(Map.Entry<ComparativeItemStack, Integer> entry : map.entrySet())
        {
            if(!isMapped(entry.getKey()))
            {
                return -1D;
            }
            if(isMapped(new ComparativeItemStack(325, (short)0)) && ((entry.getKey().getId() == 326) || (entry.getKey().getId() == 327) || (entry.getKey().getId() == 335))) // I hate buckets -.-
            {
                value += (getValue(entry.getKey()) - getValue(new ComparativeItemStack(325, (short)0))) * entry.getValue().doubleValue();
            }
            else
            {
                value += getValue(entry.getKey()) * entry.getValue().doubleValue();
            }
        }
        return value;
    }

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

    private static boolean isDefined(ItemStack item)
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

    private static boolean isDefined(ComparativeItemStack item)
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

    private static boolean isMapped(ItemStack item)
    {
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return stack.getValue() > 0;
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
                return stack.getValue() > 0;
            }
        }
        return false;
    }

    public static double getValue(ItemStack item)
    {
        if(item == null)
        {
            return 0D;
        }
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
        if(item == null)
        {
            return 0D;
        }
        for(ValuedItemStack stack : _list.toArray(new ValuedItemStack[0]))
        {
            if(stack.matches(item))
            {
                return stack.getValue();
            }
        }
        return 0D;
    }

    public static ValuedItemStack[] getAll()
    {
        return _list.toArray(new ValuedItemStack[0]);
    }

    public static ValuedItemStack[] getAll(int start)
    {
        start *= 20;
        if(start >= _list.size())
        {
            return new ValuedItemStack[0];
        }
        ListIterator<ValuedItemStack> it = _list.listIterator(start);
        ArrayList<ValuedItemStack> list = new ArrayList<ValuedItemStack>();
        for(int i = 0; i < 20; i++)
        {
            if(!it.hasNext())
            {
                break;
            }
            list.add(it.next());
        }
        return list.toArray(new ValuedItemStack[0]);
    }

    public static ValuedItemStack[] getAllFiltered(String world, UUID player)
    {
        return filter(world, player, _list);
    }

    public static ValuedItemStack[] getAllFiltered(String world, UUID player, ComparativeItemStack[] unlocked)
    {
        ArrayList<ValuedItemStack> list = new ArrayList<ValuedItemStack>();
        for(ValuedItemStack stack : _list)
        {
            for(ComparativeItemStack item : unlocked)
            {
                if(stack.matches(item))
                {
                    list.add(stack);
                    break;
                }
            }
        }
        return filter(world, player, list);
    }

    private static ValuedItemStack[] filter(String world, UUID player, ArrayList<ValuedItemStack> list)
    {
        for(ValuedItemStack stack : list.toArray(new ValuedItemStack[0]))
        {
            if(Config.isBlacklisted(world, player, "materializer", stack))
            {
                list.remove(stack);
            }
        }
        return list.toArray(new ValuedItemStack[0]);
    }
}
