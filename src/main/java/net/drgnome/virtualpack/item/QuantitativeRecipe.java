// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.item;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class QuantitativeRecipe implements Recipe
{
    private ItemStack _result;
    private HashMap<ComparativeItemStack, Integer> _map;
    
    public QuantitativeRecipe(ItemStack result, HashMap<ComparativeItemStack, Integer> map)
    {
        _result = result;
        _map = map;
    }
    
    public ItemStack getResult()
    {
        return _result;
    }
    
    public HashMap<ComparativeItemStack, Integer> getIngredients()
    {
        return _map;
    }
}