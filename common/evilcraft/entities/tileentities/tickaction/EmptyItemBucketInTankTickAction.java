package evilcraft.entities.tileentities.tickaction;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import evilcraft.api.entities.tileentitites.TickingTankInventoryTileEntity;
import evilcraft.api.fluids.SingleUseTank;

public class EmptyItemBucketInTankTickAction<T extends TickingTankInventoryTileEntity> extends EmptyInTankTickAction<T> {

    @Override
    public void onTick(T tile, ItemStack itemStack, int slot, int tick) {
        if(tick >= getRequiredTicks(tile, slot)) {
            ItemStack infuseStack = itemStack;
            FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(infuseStack);
            SingleUseTank tank = tile.getTank();
            if(tank.getCapacity() - tank.getFluidAmount() >= FluidContainerRegistry.BUCKET_VOLUME
                    && fluidStack != null && tank.getAcceptedFluid().equals(fluidStack.getFluid())) {
                tank.fill(fluidStack, true);
                tile.getInventory().setInventorySlotContents(slot, FluidContainerRegistry.EMPTY_BUCKET.copy());
            }
        }
    }
    
    @Override
    public boolean canTick(T tile, ItemStack itemStack, int slot, int tick) {
        boolean emptyContainer = false;
        ItemStack containerStack = tile.getInventory().getStackInSlot(slot);
        return super.canTick(tile, itemStack, slot, tick) && containerStack.itemID != Item.bucketEmpty.itemID;
    }
    
    @Override
    public int getRequiredTicks(T tile, int slot) {
        return FluidContainerRegistry.BUCKET_VOLUME / MB_PER_TICK;
    }

}
