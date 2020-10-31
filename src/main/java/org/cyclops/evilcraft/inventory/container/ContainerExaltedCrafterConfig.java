package org.cyclops.evilcraft.inventory.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.client.gui.ScreenFactorySafe;
import org.cyclops.cyclopscore.config.extendedconfig.GuiConfig;
import org.cyclops.cyclopscore.inventory.container.ContainerTypeData;
import org.cyclops.evilcraft.EvilCraft;
import org.cyclops.evilcraft.client.gui.container.ContainerScreenExaltedCrafter;

/**
 * Config for {@link ContainerExaltedCrafter}.
 * @author rubensworks
 */
public class ContainerExaltedCrafterConfig extends GuiConfig<ContainerExaltedCrafter> {

    public ContainerExaltedCrafterConfig() {
        super(EvilCraft._instance,
                "exalted_crafter",
                eConfig -> new ContainerTypeData<>(ContainerExaltedCrafter::new));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public <U extends Screen & IHasContainer<ContainerExaltedCrafter>> ScreenManager.IScreenFactory<ContainerExaltedCrafter, U> getScreenFactory() {
        return new ScreenFactorySafe<>(ContainerScreenExaltedCrafter::new);
    }

}
