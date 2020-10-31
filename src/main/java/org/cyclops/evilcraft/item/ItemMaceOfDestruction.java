package org.cyclops.evilcraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.helper.FluidHelpers;

/**
 * A mace that produces explosions around the player, without damaging that player.
 * @author rubensworks
 *
 */
public class ItemMaceOfDestruction extends ItemMace {

    /**
     * The amount of ticks that should go between each update of the area of effect particles.
     */
    public static final int AOE_TICK_UPDATE = 20;

    private static final int MAXIMUM_CHARGE = 100;
    private static final float MELEE_DAMAGE = 10.0F;
    private static final int CONTAINER_SIZE = FluidHelpers.BUCKET_VOLUME * 4;
    private static final int HIT_USAGE = 6;
    private static final int POWER_LEVELS = 5;

    public ItemMaceOfDestruction(Item.Properties properties) {
        super(properties, CONTAINER_SIZE, HIT_USAGE, MAXIMUM_CHARGE, POWER_LEVELS, MELEE_DAMAGE);
    }

    @Override
    protected void use(World world, LivingEntity entity, int itemUsedCount, int power) {
        if(!world.isRemote()) {
            Vec3d v = entity.getLookVec();
            world.createExplosion(entity, entity.getPosX() + v.x * 2, entity.getPosY() + entity.getEyeHeight() + v.y * 2, entity.getPosZ() + v.z * 2, ((float) itemUsedCount) / 20 + power, Explosion.Mode.DESTROY);
        }
    }
}
