package org.cyclops.evilcraft.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.cyclops.cyclopscore.block.BlockTile;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.TileHelpers;
import org.cyclops.cyclopscore.item.IInformationProvider;
import org.cyclops.evilcraft.tileentity.TileDisplayStand;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A stand for displaying items.
 * @author rubensworks
 *
 */
public class BlockDisplayStand extends BlockTile implements IInformationProvider {

    private static final String NBT_TYPE = "displayStandType";

    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty AXIS_X = BooleanProperty.create("axis_x");

    // Model Properties
    public static final ModelProperty<Direction.AxisDirection> DIRECTION = new ModelProperty<>();
    public static final ModelProperty<ItemStack> TYPE = new ModelProperty<>();

    public static final Map<Direction, VoxelShape> FACING_BOUNDS = ImmutableMap.<Direction, VoxelShape>builder()
            .put(Direction.DOWN, Block.makeCuboidShape(0.375F * 16F, 0.0F, 0.375F * 16F, 0.625F * 16F, 0.5F * 16F, 0.625F * 16F))
            .put(Direction.UP, Block.makeCuboidShape(0.375F * 16F, 0.5F * 16F, 0.375F * 16F, 0.625F * 16F, 1.0F * 16F, 0.625F * 16F))
            .put(Direction.WEST, Block.makeCuboidShape(0.0F, 0.375F * 16F, 0.375F * 16F, 0.5F * 16F, 0.625F * 16F, 0.625F * 16F))
            .put(Direction.EAST, Block.makeCuboidShape(0.5F * 16F, 0.375F * 16F, 0.375F * 16F, 1.0F * 16F, 0.625F * 16F, 0.625F * 16F))
            .put(Direction.NORTH, Block.makeCuboidShape(0.375F * 16F, 0.375F * 16F, 0.0F, 0.625F * 16F, 0.625F * 16F, 0.5F * 16F))
            .put(Direction.SOUTH, Block.makeCuboidShape(0.375F * 16F, 0.375F * 16F, 0.5F * 16F, 0.625F * 16F, 0.625F * 16F, 1.0F * 16F))
            .build();

    public BlockDisplayStand(Block.Properties properties) {
        super(properties, TileDisplayStand::new);
        MinecraftForge.EVENT_BUS.register(this);
    }

    /*
    @Override
    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState blockState = (IExtendedBlockState) state;
        TileDisplayStand tile = TileHelpers.getSafeTile(world, pos, TileDisplayStand.class);
        if (tile != null) {
            blockState = blockState.with(DIRECTION, tile.getDirection());
            if (tile.getDisplayStandType() != null) {
                blockState = blockState.with(TYPE, tile.getDisplayStandType());
            }
        }
        return blockState;
    }TODO: model*/

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return FACING_BOUNDS.get(BlockHelpers.getSafeBlockStateProperty(state, FACING, Direction.DOWN));
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos blockPos) {
        return TileHelpers.getSafeTile(world, blockPos, TileDisplayStand.class)
                .map(tile -> !tile.getInventory().getStackInSlot(0).isEmpty() ? 15 : 0)
                .orElse(0);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockState = super.getStateForPlacement(context);
        blockState = blockState.with(FACING, context.getFace().getOpposite());
        Direction playerFacing = context.getPlayer().getHorizontalFacing();
        boolean axisX;
        if (context.getFace().getOpposite() == Direction.DOWN || context.getFace().getOpposite() == Direction.UP) {
            axisX = playerFacing.getAxis() == Direction.Axis.X;
        } else {
            axisX = playerFacing.getAxis() != Direction.Axis.X && playerFacing.getAxis() != Direction.Axis.Z;
        }
        blockState = blockState.with(AXIS_X, axisX);
        return blockState;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos blockPos, BlockState blockState, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, blockPos, blockState, entity, stack);
        TileHelpers.getSafeTile(world, blockPos, TileDisplayStand.class)
                .ifPresent(tile -> tile.setDirection(entity.getHorizontalFacing().getAxisDirection()));
    }

    @Override
    public BlockState rotate(BlockState blockState, IWorld world, BlockPos pos, Rotation direction) {
        return TileHelpers.getSafeTile(world, pos, TileDisplayStand.class)
                .map(tile -> {
                    if (tile.getDirection() == Direction.AxisDirection.POSITIVE) {
                        if (blockState.get(AXIS_X)) {
                            tile.setDirection(Direction.AxisDirection.POSITIVE);
                            return blockState.with(AXIS_X, false);
                        } else {
                            tile.setDirection(Direction.AxisDirection.NEGATIVE);
                            return blockState.with(AXIS_X, true);
                        }
                    } else {
                        if (blockState.get(AXIS_X)) {
                            tile.setDirection(Direction.AxisDirection.NEGATIVE);
                            return blockState.with(AXIS_X, false);
                        } else {
                            tile.setDirection(Direction.AxisDirection.POSITIVE);
                            return blockState.with(AXIS_X, true);
                        }
                    }
                })
                .orElse(blockState);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> list) {
        for (Item item : ItemTags.PLANKS.getAllElements()) {
            if (item instanceof BlockItem) {
                BlockState plankWoodBlockState = BlockHelpers.getBlockStateFromItemStack(new ItemStack(item));
                list.add(getTypedDisplayStandItem(plankWoodBlockState));
            }
        }
    }

    public ItemStack getTypedDisplayStandItem(BlockState blockState) {
        INBT blockTag = BlockHelpers.serializeBlockState(blockState);
        ItemStack itemStack = new ItemStack(this);
        CompoundNBT tag = itemStack.getOrCreateTag();
        tag.put(NBT_TYPE, blockTag);
        return itemStack;
    }

    public ItemStack getDisplayStandType(ItemStack displayStandStack) {
        CompoundNBT tag = displayStandStack.getTag();
        if (tag != null && tag.contains(NBT_TYPE)) {
            BlockState blockState = BlockHelpers.deserializeBlockState(tag.get(NBT_TYPE));
            return BlockHelpers.getItemStackFromBlockState(blockState);
        }
        return null;
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        // Force allow right clicking with a fluid container passing through to this block
        if (!event.getItemStack().isEmpty()
                && event.getItemStack().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()
                && event.getWorld().getBlockState(event.getPos()).getBlock() == this) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult p_225533_6_) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote()) {
            return ActionResultType.SUCCESS;
        } else {
            TileDisplayStand tile = TileHelpers.getSafeTile(world, pos, TileDisplayStand.class).orElse(null);
            if (tile != null) {
                ItemStack tileStack = tile.getInventory().getStackInSlot(0);
                if ((itemStack.isEmpty() || (ItemStack.areItemsEqual(itemStack, tileStack) && ItemStack.areItemStackTagsEqual(itemStack, tileStack) && tileStack.getCount() < tileStack.getMaxStackSize())) && !tileStack.isEmpty()) {
                    if(!itemStack.isEmpty()) {
                        tileStack.grow(itemStack.getCount());
                    }
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, tileStack);
                    tile.getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
                    tile.sendUpdate();
                    return ActionResultType.SUCCESS;
                } else if (!itemStack.isEmpty() && tile.getInventory().getStackInSlot(0).isEmpty()) {
                    tile.getInventory().setInventorySlotContents(0, itemStack.split(1));
                    if (itemStack.getCount() <= 0)
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                    tile.sendUpdate();
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return ActionResultType.PASS;
    }

    /* TODO: model
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        for (Direction facing : DisplayStand.FACING.getAllowedValues()) {
            for (Boolean axisX : DisplayStand.AXIS_X.getAllowedValues()) {
                String blockVariant = String.format("%s=%s,%s=%s",
                        DisplayStand.AXIS_X.getName(), DisplayStand.AXIS_X.getName(axisX),
                        DisplayStand.FACING.getName(), DisplayStand.FACING.getName(facing));
                bakeVariantModel(event, blockVariant, !axisX);
            }
        }
        bakeVariantModel(event, "inventory", false);
    }

    protected void bakeVariantModel(ModelBakeEvent event, String variant, boolean rotated) {
        ModelResourceLocation modelVariantLocation = new ModelResourceLocation(Reference.MOD_ID + ":display_stand", variant);
        try {
            ResourceLocation modelLocation = new ResourceLocation(Reference.MOD_ID, "block/display_stand" + (rotated ? "_rotated" : ""));
            IBakedModel originalBakedModel = event.getModelRegistry().getObject(modelVariantLocation);
            // We actually need to retrieve modelVariantLocation, but that seems to make it a non-IRetexturableModel
            // So instead, we manually apply model rotation in ModelDisplayStand when baking.
            IModel model = ModelLoaderRegistry.getModel(modelLocation);
            event.getModelRegistry().putObject(modelVariantLocation,
                    new ModelDisplayStand(originalBakedModel, model));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public ITextComponent getInfo(ItemStack itemStack) {
        ItemStack blockType = getDisplayStandType(itemStack);
        if (blockType != null) {
            return blockType.getDisplayName();
        }
        return new StringTextComponent("");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void provideInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag iTooltipFlag) {

    }
}
