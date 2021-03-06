package org.cyclops.evilcraft.core.recipe.type;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.cyclops.evilcraft.RegistryEntries;
import org.cyclops.evilcraft.core.weather.WeatherType;
import org.cyclops.evilcraft.item.ItemBiomeExtractConfig;

/**
 * Environmental Accumulator recipe
 * @author rubensworks
 */
public class RecipeEnvironmentalAccumulatorBiomeExtract extends RecipeEnvironmentalAccumulator {

    public RecipeEnvironmentalAccumulatorBiomeExtract(ResourceLocation id, Ingredient inputIngredient, WeatherType inputWeather, ItemStack outputItem, WeatherType outputWeather, int duration, int cooldownTime, float processingSpeed) {
        super(id, inputIngredient, inputWeather, outputItem, outputWeather, duration, cooldownTime, processingSpeed);
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return RegistryEntries.RECIPESERIALIZER_BIOME_EXTRACT;
    }

    @Override
    public ItemStack getCraftingResult(Inventory inventory) {
        Biome biome = inventory.getWorld().getBiome(inventory.getPos());
        if (ItemBiomeExtractConfig.isCraftingBlacklisted(biome)) {
            return RegistryEntries.ITEM_BIOME_EXTRACT.createItemStack(null, 1);
        } else {
            return RegistryEntries.ITEM_BIOME_EXTRACT.createItemStack(biome, 1);
        }
    }
}
