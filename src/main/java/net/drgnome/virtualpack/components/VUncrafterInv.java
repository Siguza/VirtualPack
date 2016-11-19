// Bukkit Plugin "VirtualPack" by Siguza
// The license under which this software is released can be accessed at:
// http://creativecommons.org/licenses/by-nc-sa/3.0/

package net.drgnome.virtualpack.components;

import java.util.*;
import java.lang.reflect.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import net.minecraft.server.v#MC_VERSION#.*;
import org.bukkit.craftbukkit.v#MC_VERSION#.inventory.CraftItemStack;
import net.drgnome.virtualpack.util.*;
import static net.drgnome.virtualpack.util.Global.*;

public class VUncrafterInv extends VInv implements VProcessing
{
    private static final Field[] _fields = new Field[2];
    private static VRecipe[] _recipes;
    private static VRecipe _enchantedBook = new VRecipe(new ItemStack[]{new ItemStack(Item.#FIELD_ITEM_8#(Material.BOOK.getId()), 1, 0)}, new ItemStack(Item.#FIELD_ITEM_8#(Material.ENCHANTED_BOOK.getId()), 1, 0), false);;
    private final EntityPlayer _player;

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

    public static void init()
    {
        ArrayList<VRecipe> list = new ArrayList<VRecipe>();
        List<IRecipe> mcList = (List<IRecipe>)CraftingManager.getInstance().getRecipes();
        for(IRecipe recipe : mcList)
        {
            ItemStack result = recipe.#FIELD_IRECIPE_1#(); // Derpnote
            ---------- PRE 1.11 START ----------
            if(result == null || result.count < 1)
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            if(result == null || result.getCount() < 1)
            ---------- SINCE 1.11 END ----------
            {
                continue;
            }
            ItemStack[] ingredients;
            if(recipe instanceof ShapelessRecipes)
            {
                ingredients = ((List<ItemStack>)access(0, recipe)).toArray(new ItemStack[0]);
            }
            else if(recipe instanceof ShapedRecipes)
            {
                ingredients = (ItemStack[])access(1, recipe);
            }
            else
            {
                _log.warning("[VirtualPack] Uncrafter: Skipping unknown recipe type: " + recipe.getClass().getName());
                continue;
            }
            list.add(new VRecipe(ingredients, result));
        }
        _recipes = list.toArray(new VRecipe[0]);
        Arrays.sort(_recipes);
    }

    public VUncrafterInv(EntityPlayer player)
    {
        super(2);
        _player = player;
    }

    public void process()
    {
        for(int i = 0; i < 9; i++)
        {
            processSlot(i);
        }
    }

    private VRecipe match(ItemStack result)
    {
        for(int i = 0; i < _recipes.length; i++)
        {
            ItemStack re = _recipes[i].result;
            if((Item.#FIELD_ITEM_7#(re.getItem()) != Item.#FIELD_ITEM_7#(result.getItem())) || (re.getData() != result.getData()))
            {
                continue;
            }
            return _recipes[i];
        }
        return null;
    }

    private VRecipe exactMatch(ItemStack result)
    {
        for(int i = 0; i < _recipes.length; i++)
        {
            ItemStack re = _recipes[i].result;
            if((Item.#FIELD_ITEM_7#(re.getItem()) != Item.#FIELD_ITEM_7#(result.getItem())) || (re.getData() != result.getData()))
            {
                continue;
            }
            NBTTagCompound tag1 = re.getTag();
            NBTTagCompound tag2 = result.getTag();
            if((tag1 == null && tag2 == null) || (tag1 != null && tag2 != null && tag1.equals(tag2)))
            {
                return _recipes[i];
            }
        }
        return null;
    }

    private VRecipe disEntchant(ItemStack result)
    {
        if(Item.#FIELD_ITEM_7#(result.getItem()) == Material.ENCHANTED_BOOK.getId())
        {
            return _enchantedBook;
        }
        else if(Config.bool("uncraft-enchanted"))
        {
            NBTTagList ench = result.getEnchantments();
            if((ench == null) || (ench.size() == 0))
            {
                return null;
            }
            ItemStack re = Util.copy_old(result);
            ---------- PRE 1.11 START ----------
            re.count = 1;
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            re.setCount(1);
            ---------- SINCE 1.11 END ----------
            ItemStack[] back = new ItemStack[ench.size() + 1];
            back[0] = Util.copy_old(re);
            back[0].getTag().remove("ench");
            for(int i = 0; i < ench.size(); i++)
            {
                back[i + 1] = new ItemStack(Item.#FIELD_ITEM_8#(Material.ENCHANTED_BOOK.getId()), 1, 0);
                NBTTagCompound tag = new NBTTagCompound();
                NBTTagList list = new NBTTagList();
                list.add(ench.get(i));
                tag.set("StoredEnchantments", list);
                back[i + 1].setTag(tag);
            }
            return new VRecipe(back, re, false);
        }
        return null;
    }

    private void processSlot(int slot)
    {
        if(contents[slot] == null || (contents[slot].getItem().usesDurability() && (contents[slot].getData() != 0)) || Config.isBlacklisted(_player.world.getWorld().getName(), (Player)_player.getBukkitEntity(), "uncrafter", CraftItemStack.asBukkitCopy(contents[slot])))
        {
            return;
        }
        VRecipe re = exactMatch(contents[slot]);
        if(re == null)
        {
            re = disEntchant(contents[slot]);
            if(re == null)
            {
                NBTTagList ench = contents[slot].getEnchantments();
                if((ench != null) && (ench.size() > 0))
                {
                    return;
                }
                re = match(contents[slot]);
                if(re == null)
                {
                    return;
                }
            }
        }
        ItemStack[] copy = new ItemStack[9];
        for(int i = 0; i < 9; i++)
        {
            copy[i] = Util.copy_old(contents[i + 9]);
        }
        ---------- PRE 1.11 START ----------
        int amount = re.result.count;
        dance:
        for(int count = contents[slot].count - amount; count >= 0; count -= amount)
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        int amount = re.result.getCount();
        dance:
        for(int count = contents[slot].getCount() - amount; count >= 0; count -= amount)
        ---------- SINCE 1.11 END ----------
        {
            ItemStack[] sandbox = Util.copy_old(copy);
            derping:
            for(int i = 0; i < re.ingredients.length; i++)
            {
                for(int j = 0; j < 9; j++)
                {
                    if(sandbox[j] == null || sandbox[j] == #F_ITEMSTACK_NULL#)
                    {
                        sandbox[j] = Util.copy_old(re.ingredients[i]);
                        continue derping;
                    }
                    else
                    {
                        ---------- PRE 1.11 START ----------
                        if(sandbox[j].doMaterialsMatch(re.ingredients[i]) && sandbox[j].count < sandbox[j].getMaxStackSize())
                        ---------- PRE 1.11 END ----------
                        ---------- SINCE 1.11 START ----------
                        int c = sandbox[j].getCount();
                        if(sandbox[j].doMaterialsMatch(re.ingredients[i]) && c < sandbox[j].getMaxStackSize())
                        ---------- SINCE 1.11 END ----------
                        {
                            ---------- PRE 1.11 START ----------
                            sandbox[j].count++;
                            ---------- PRE 1.11 END ----------
                            ---------- SINCE 1.11 START ----------
                            sandbox[j].setCount(c + 1);
                            ---------- SINCE 1.11 END ----------
                            continue derping;
                        }
                    }
                }
                break dance;
            }
            copy = sandbox;
            ---------- PRE 1.11 START ----------
            contents[slot].count = count;
            ---------- PRE 1.11 END ----------
            ---------- SINCE 1.11 START ----------
            contents[slot].setCount(count);
            ---------- SINCE 1.11 END ----------
        }
        for(int i = 0; i < 9; i++)
        {
            contents[i + 9] = copy[i];
        }
        ---------- PRE 1.11 START ----------
        if(contents[slot].count == 0)
        ---------- PRE 1.11 END ----------
        ---------- SINCE 1.11 START ----------
        if(contents[slot].getCount() == 0)
        ---------- SINCE 1.11 END ----------
        {
            contents[slot] = null;
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

    private static class VRecipe implements Comparable<VRecipe>
    {
        public final ItemStack[] ingredients;
        public final ItemStack result;

        public VRecipe(ItemStack[] in, ItemStack re)
        {
            this(in, re, true);
        }

        public VRecipe(ItemStack[] in, ItemStack re, boolean copy)
        {
            if(copy)
            {
                in = Util.copy_old(in);
                result = Util.copy_old(re);
            }
            else
            {
                result = re;
            }
            ArrayList<ItemStack> list = new ArrayList<ItemStack>();
            for(int i = 0; i < in.length; i++)
            {
                if(in[i] != null)
                {
                    Item item = in[i].getItem();
                    if(item != null && !item.#FIELD_ITEM_1#())
                    {
                        list.add(in[i]);
                    }
                }
            }
            ingredients = list.toArray(new ItemStack[0]);
            for(int i = 0; i < ingredients.length; i++)
            {
                ---------- PRE 1.11 START ----------
                ingredients[i].count = 1;
                ---------- PRE 1.11 END ----------
                ---------- SINCE 1.11 START ----------
                ingredients[i].setCount(1);
                ---------- SINCE 1.11 END ----------
                int data = ingredients[i].getData();
                if((data < 0) || (data >= 0x7FFF))
                {
                    ingredients[i].setData(0);
                }
            }
        }

        // A recipe that can be guarateed to not be a superset of another recipe gets sorted to the back.
        // This way, specific recipes are checked before generic ones.
        // It should be fine to see non-comparable recipes as equal here.
        public int compareTo(VRecipe vr)
        {
            Item it = this.result.getItem();
            if((Item.#FIELD_ITEM_7#(it) != Item.#FIELD_ITEM_7#(vr.result.getItem())) || (it.#FIELD_ITEM_5#() && (this.result.getData() != vr.result.getData())))
            {
                return 0;
            }
            NBTTagCompound tag1 = this.result.getTag();
            NBTTagCompound tag2 = vr.result.getTag();
            if(tag1 == null && tag2 == null)
            {
                return 0;
            }
            if(tag1 == null && tag2 != null)
            {
                return 1;
            }
            if(tag1 != null && tag2 == null)
            {
                return -1;
            }
            return compareTags(tag1, tag2);
        }

        private static int compareTags(NBTBase nbt1, NBTBase nbt2)
        {
            int id = nbt1.getTypeId();
            if(id != nbt2.getTypeId())
            {
                return 0;
            }
            if(id == 10) // Compound
            {
                NBTTagCompound tag1 = (NBTTagCompound)nbt1;
                NBTTagCompound tag2 = (NBTTagCompound)nbt2;
                boolean hasMore = false;
                ArrayList key1 = new ArrayList(tag1.#FIELD_NBTTAGCOMPOUND_1#());
                ArrayList key2 = new ArrayList(tag2.#FIELD_NBTTAGCOMPOUND_1#());
                for(Object o : key1)
                {
                    if(!key2.remove(o))
                    {
                        hasMore = true;
                    }
                }
                if(key2.size() > 0)
                {
                    return hasMore ? 0 : 1;
                }
                else if(hasMore)
                {
                    return -1;
                }
                // At this point, key1 and key2 are bound to be equal
                for(Object o : key1)
                {
                    int ret = compareTags(tag1.get((String)o), tag2.get((String)o));
                    if(ret != 0)
                    {
                        return ret;
                    }
                }
            }
            else if(id == 9) // List
            {
                NBTTagList list1 = (NBTTagList)nbt1;
                NBTTagList list2 = (NBTTagList)nbt2;
                int size1 = list1.size();
                int size2 = list2.size();
                if(size1 > size2)
                {
                    return -1;
                }
                if(size1 < size2)
                {
                    return 1;
                }
                for(int i = 0; i < size1; i++)
                {
                    int ret = compareTags(list1.#FIELD_NBTTAGLIST_1#(i), list2.#FIELD_NBTTAGLIST_1#(i));
                    if(ret != 0)
                    {
                        return ret;
                    }
                }
            }
            // Plain values say nothing
            return 0;
        }
    }
}
