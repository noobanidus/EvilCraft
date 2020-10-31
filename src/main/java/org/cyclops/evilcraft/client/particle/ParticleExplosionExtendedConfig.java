package org.cyclops.evilcraft.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.cyclops.cyclopscore.config.extendedconfig.ParticleConfig;
import org.cyclops.evilcraft.EvilCraft;

import javax.annotation.Nullable;

/**
 * Config for {@link ParticleExplosionExtended}.
 * @author rubensworks
 */
public class ParticleExplosionExtendedConfig extends ParticleConfig<ParticleExplosionExtendedData> {

    public ParticleExplosionExtendedConfig() {
        super(EvilCraft._instance, "explosion_extended", eConfig -> new ParticleType<>(false, ParticleExplosionExtendedData.DESERIALIZER));
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public IParticleFactory<ParticleExplosionExtendedData> getParticleFactory() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Override
    public ParticleManager.IParticleMetaFactory<ParticleExplosionExtendedData> getParticleMetaFactory() {
        return sprite -> (IParticleFactory<ParticleExplosionExtendedData>) (particleData, worldIn, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            ParticleExplosionExtended particle = new ParticleExplosionExtended(worldIn, x, y, z, xSpeed, ySpeed, zSpeed,
                    particleData.getR(), particleData.getG(), particleData.getB(), particleData.getScale(), sprite);
            particle.selectSpriteRandomly(sprite);
            return particle;
        };
    }

}
