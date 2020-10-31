package org.cyclops.evilcraft.core.client.gui.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.cyclops.cyclopscore.inventory.container.ContainerExtended;
import org.cyclops.evilcraft.client.gui.container.WidgetUpgradeTab;
import org.cyclops.evilcraft.core.inventory.container.ContainerInventoryTickingTank;
import org.cyclops.evilcraft.core.inventory.container.ContainerWorking;
import org.cyclops.evilcraft.core.tileentity.WorkingTileEntity;
import org.cyclops.evilcraft.core.tileentity.TileWorking;

import java.util.List;
import java.util.function.Supplier;

/**
 * A GUI container that has support for the display of {@link WorkingTileEntity}.
 * @author rubensworks
 *
 * @param <T> The {@link TileWorking} class, mostly just the extension class.
 */
public abstract class ContainerScreenWorking<C extends ContainerWorking<T>, T extends TileWorking<T, ?>>
        extends ContainerScreenContainerTankInventory<C, T>
        implements WidgetUpgradeTab.SlotEnabledCallback {

    public static final int UPGRADES_OFFSET_X = 28;

    private WidgetUpgradeTab upgrades;

    public ContainerScreenWorking(C container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.upgrades = new WidgetUpgradeTab(this, this);
        this.offsetX = UPGRADES_OFFSET_X;
	}

    @Override
    public boolean isSlotEnabled(int upgradeSlotId) {
        return container.isUpgradeSlotEnabled(upgradeSlotId);
    }
	
	@Override
    protected boolean isShowProgress() {
        return getContainer().getMaxProgress(0) > 0;
    }
    
    @Override
    protected int getProgressXScaled(int width) {
        return (int) Math.ceil((float)(getContainer().getProgress(0)) / (float)getContainer().getMaxProgress(0) * (float)width);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
        super.drawGuiContainerBackgroundLayer(f, x, y);
        upgrades.drawBackground(guiLeft, guiTop);
    }

}
