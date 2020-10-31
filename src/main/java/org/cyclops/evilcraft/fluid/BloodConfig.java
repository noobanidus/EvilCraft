package org.cyclops.evilcraft.fluid;

import net.minecraft.item.Rarity;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.cyclops.cyclopscore.config.extendedconfig.FluidConfig;
import org.cyclops.evilcraft.EvilCraft;
import org.cyclops.evilcraft.RegistryEntries;

/**
 * Config for blood.
 * @author rubensworks
 *
 */
public class BloodConfig extends FluidConfig {

    public BloodConfig() {
        super(
                EvilCraft._instance,
                "blood",
                fluidConfig -> new ForgeFlowingFluid.Source(
                        getDefaultFluidProperties(EvilCraft._instance,
                                "block/blood",
                                builder -> builder
                                        .density(1500)
                                        .viscosity(3000)
                                        .temperature(309)
                                        .rarity(Rarity.COMMON)
                                        .translationKey("block.evilcraft.blood"))
                                .bucket(() -> RegistryEntries.ITEM_BUCKET_BLOOD)
                                .block(() -> RegistryEntries.BLOCK_BLOOD))
        );
    }
    
}
