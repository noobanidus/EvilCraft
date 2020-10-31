package org.cyclops.evilcraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import org.cyclops.cyclopscore.config.ConfigurableProperty;
import org.cyclops.evilcraft.EvilCraft;
import org.cyclops.evilcraft.core.config.extendedconfig.UpgradableBlockContainerConfig;
import org.cyclops.evilcraft.core.item.ItemBlockFluidContainer;
import org.cyclops.evilcraft.core.tileentity.upgrade.Upgrades;
import org.cyclops.evilcraft.core.tileentity.TileWorking;

import java.util.List;
import java.util.Set;

/**
 * Config for the {@link BlockBloodInfuser}.
 * @author rubensworks
 *
 */
public class BlockBloodInfuserConfig extends UpgradableBlockContainerConfig {

    @ConfigurableProperty(category = "machine", comment = "The blacklisted Blood Chest items, by item name.")
    public static List<String> itemBlacklist = Lists.newArrayList(
            "minecraft:stick"
    );

    public BlockBloodInfuserConfig() {
        super(
            EvilCraft._instance,
            "blood_infuser",
                eConfig -> new BlockBloodInfuser(Block.Properties.create(Material.ROCK)
                        .sound(SoundType.STONE)),
                (eConfig, block) -> new ItemBlockFluidContainer(block, (new Item.Properties())
                        .group(EvilCraft._instance.getDefaultItemGroup()))
        );
    }

    @Override
    public Set<Upgrades.Upgrade> getUpgrades() {
        return Sets.newHashSet(
                TileWorking.UPGRADE_EFFICIENCY,
                TileWorking.UPGRADE_SPEED,
                TileWorking.UPGRADE_TIER1,
                TileWorking.UPGRADE_TIER2,
                TileWorking.UPGRADE_TIER3);
    }
    
}
