package org.cyclops.evilcraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.cyclopscore.config.extendedconfig.BlockConfig;
import org.cyclops.evilcraft.EvilCraft;

/**
 * Config for the {@link BlockDarkOre}.
 * @author rubensworks
 *
 */
public class BlockDarkOreConfig extends BlockConfig {

    @ConfigurableProperty(category = "worldgeneration", comment = "How much ores per vein.")
    public static int blocksPerVein = 4;

    @ConfigurableProperty(category = "worldgeneration", comment = "How many veins per chunk.")
    public static int veinsPerChunk = 7;

    @ConfigurableProperty(category = "worldgeneration", comment = "Generation ends of this level.")
    public static int endY = 66;

    public ConfiguredFeature<?, ?> worldFeature;

    public BlockDarkOreConfig() {
        super(
                EvilCraft._instance,
            "dark_ore",
                eConfig -> new BlockDarkOre(Block.Properties.create(Material.ROCK)
                        .hardnessAndResistance(3.0F)
                        .sound(SoundType.STONE)
                        .harvestTool(ToolType.PICKAXE)
                        .harvestLevel(2)),
                getDefaultItemConstructor(EvilCraft._instance)
        );
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onBiomeLoadingEvent);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(getInstance(), RenderType.getCutout());
    }

    public void onBiomeLoadingEvent(BiomeLoadingEvent event) {
        if (event.getCategory() != Biome.Category.THEEND && event.getCategory() != Biome.Category.NETHER) {
            event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> Feature.ORE
                    .withConfiguration(new OreFeatureConfig(OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD, getInstance().getDefaultState(), blocksPerVein))
                    .range(endY).square().func_242731_b(veinsPerChunk));
        }
    }
    
}
