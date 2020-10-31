package org.cyclops.evilcraft.core.entity.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;

/**
 * An extended version of the entity item.
 * @author rubensworks
 *
 */
public abstract class EntityItemExtended extends ItemEntity {

    public EntityItemExtended(EntityType<? extends EntityItemExtended> type, World world) {
        super(type, world);
        this.setPickupDelay(40);
    }

	public EntityItemExtended(EntityType<? extends EntityItemExtended> type, World world, ItemEntity original) {
        super(type, world);
        this.setPickupDelay(40);
        this.setMotion(original.getMotion());
        this.setPosition(original.getPosX(), original.getPosY(), original.getPosZ());
        this.setItem(original.getItem());
    }
	
}
