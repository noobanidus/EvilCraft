package org.cyclops.evilcraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.evilcraft.RegistryEntries;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * A blockState for the blood fluid.
 * @author rubensworks
 *
 */
public class BlockFluidBlood extends FlowingFluidBlock {
    
    private static final int CHANCE_HARDEN = 3;

    public BlockFluidBlood(Block.Properties builder) {
        super(() -> RegistryEntries.FLUID_BLOOD, builder);
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(LEVEL) == 0;
    }

    @Override
    public void randomTick(BlockState blockState, ServerWorld world, BlockPos blockPos, Random random) {
        if(random.nextInt(CHANCE_HARDEN) == 0 && blockState.get(LEVEL) == 0
                && (!(world.isRaining() && world.getBiome(blockPos).getDownfall() > 0) || !world.canBlockSeeSky(blockPos))
                && !isWaterInArea(world, blockPos)) {
            world.setBlockState(blockPos, RegistryEntries.BLOCK_HARDENED_BLOOD.getDefaultState(), MinecraftHelpers.BLOCK_NOTIFY_CLIENT);
        }
        super.randomTick(blockState, world, blockPos, random);
    }

    protected boolean isWaterInArea(World world, BlockPos blockPos) {
        return WorldHelpers.foldArea(world, 4, blockPos,
                (input, world1, blockPos1) -> input || world1.getBlockState(blockPos1).getBlock() == Blocks.WATER, false);
    }

}
