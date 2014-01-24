package evilcraft.entities.tileentities.tickaction;

import net.minecraft.item.ItemStack;
import evilcraft.api.entities.tileentitites.TickingTankInventoryTileEntity;
import evilcraft.api.entities.tileentitites.tickaction.ITickAction;

public abstract class EmptyInTankTickAction<T extends TickingTankInventoryTileEntity> implements ITickAction<T> {
    
    protected final static int MB_PER_TICK = 100;
    
    @Override
    public boolean canTick(T tile, ItemStack itemStack, int slot, int tick) {
        return !tile.getTank().isFull()
                && itemStack != null
                && itemStack.stackSize == 1;
    }
    
}
