package org.cyclops.evilcraft.entity.monster;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.experimental.Delegate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.cyclops.cyclopscore.client.particle.ParticleBlurData;
import org.cyclops.cyclopscore.helper.BlockHelpers;
import org.cyclops.cyclopscore.helper.WorldHelpers;
import org.cyclops.evilcraft.EvilCraft;
import org.cyclops.evilcraft.EvilCraftSoundEvents;
import org.cyclops.evilcraft.ExtendedDamageSource;
import org.cyclops.evilcraft.Reference;
import org.cyclops.evilcraft.RegistryEntries;
import org.cyclops.evilcraft.block.BlockGemStoneTorchConfig;
import org.cyclops.evilcraft.client.particle.ParticleDarkSmokeData;
import org.cyclops.evilcraft.core.monster.EntityNoMob;
import org.cyclops.evilcraft.item.ItemBurningGemStone;
import org.cyclops.evilcraft.item.ItemSpectralGlasses;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * A silverfish for the nether.
 * @author rubensworks
 *
 */
public class EntityVengeanceSpirit extends EntityNoMob {

    private static final Set<String> IMC_BLACKLIST = Sets.newHashSet();
    
    /**
     * The minimum life duration in ticks the spirits should have.
     */
    public static final int REMAININGLIFE_MIN = 250;
    /**
     * The maximum life duration in ticks the spirits should have.
     */
    public static final int REMAININGLIFE_MAX = 1000;

    public static final DataParameter<String> WATCHERID_INNER = EntityDataManager.<String>createKey(EntityVengeanceSpirit.class, DataSerializers.STRING);
    public static final DataParameter<Integer> WATCHERID_REMAININGLIFE = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> WATCHERID_FROZENDURATION = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> WATCHERID_GLOBALVENGEANCE = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<String> WATCHERID_VENGEANCEPLAYERS = EntityDataManager.<String>createKey(EntityVengeanceSpirit.class, DataSerializers.STRING);
    public static final DataParameter<Integer> WATCHERID_ISSWARM = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> WATCHERID_SWARMTIER = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> WATCHERID_BUILDUP = EntityDataManager.<Integer>createKey(EntityVengeanceSpirit.class, DataSerializers.VARINT);
    public static final DataParameter<String> WATCHERID_PLAYERID = EntityDataManager.<String>createKey(EntityVengeanceSpirit.class, DataSerializers.STRING);
    public static final DataParameter<String> WATCHERID_PLAYERNAME = EntityDataManager.<String>createKey(EntityVengeanceSpirit.class, DataSerializers.STRING);

    @Getter
    @Delegate
    private EntityVengeanceSpiritSyncedData data;

	private MobEntity innerEntity = null;

    private EntityType<?> preferredInnerEntity;

    private final Set<ServerPlayerEntity> entanglingPlayers = Sets.newHashSet();

    public EntityVengeanceSpirit(EntityType<? extends EntityVengeanceSpirit> type, World world) {
        super(type, world);
    }

    public EntityVengeanceSpirit(World world) {
        this(world, null);
    }

    public EntityVengeanceSpirit(World world, EntityType<?> preferredInnerEntity) {
        this(RegistryEntries.ENTITY_VENGEANCE_SPIRIT, world);
        this.preferredInnerEntity = preferredInnerEntity;

        this.stepHeight = 5.0F;
        this.preventEntitySpawning = false;

        double speed = 0.25D;
        float damage = 0.5F;
        int remainingLife = MathHelper.nextInt(world.rand, REMAININGLIFE_MIN, REMAININGLIFE_MAX);
        if(isSwarm()) {
        	speed += 0.125D * getSwarmTier();
        	damage += 0.5D * getSwarmTier();
        	remainingLife += (REMAININGLIFE_MAX - REMAININGLIFE_MIN) * getSwarmTier();
        }

        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);

        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new RandomWalkingGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, damage));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, true));

        setRemainingLife(remainingLife);
        setFrozenDuration(0);
    }
    
    @Override
	public void registerData() {
        super.registerData();
        if (preferredInnerEntity == null)
            data = new EntityVengeanceSpiritSyncedData(this.dataManager, EntityVengeanceSpiritData.getRandomInnerEntity(this.rand));
        else
            data = new EntityVengeanceSpiritSyncedData(this.dataManager, preferredInnerEntity);
    }

    @Override
	public void writeAdditional(CompoundNBT tag) {
    	super.writeAdditional(tag);
    	data.writeNBT(tag);
    }
    
    @Override
	public void readAdditional(CompoundNBT tag) {
    	super.readAdditional(tag);
        data.readNBT(tag);
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3125D);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    public ResourceLocation getLootTable() {
        return new ResourceLocation(Reference.MOD_ID, "entities/" + getType().getRegistryName().getPath());
    }
    
    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() / 3.0F;
    }

    @Override
    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !(damageSource instanceof ExtendedDamageSource.VengeanceBeamDamageSource || damageSource == DamageSource.OUT_OF_WORLD);
    }
    
    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if(getBuildupDuration() > 0) return false; // Don't attack anything when still building up.

        this.remove();
    	if(entity instanceof PlayerEntity) {
    		PlayerEntity player = (PlayerEntity) entity;

    		if(ItemBurningGemStone.damageForPlayer(player, isSwarm() ? getSwarmTier() : 0, false)) {
    			entity.addVelocity(
    					(double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * 0.01F),
    					0.025D,
    					(double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * 0.01F));
    			entity.attackEntityFrom(DamageSource.causeMobDamage(this), 0.1F);
    			return false;
    		}
    	}
        return super.attackEntityAsMob(entity);
    }

    @Override
    protected void dropLoot(DamageSource damageSource, boolean fromPlayer) {
        super.dropLoot(damageSource, fromPlayer);

        // Also drop loot from inner entity!
        LivingEntity innerEntity = getInnerEntity();
        if (innerEntity instanceof MobEntity && damageSource != DamageSource.OUT_OF_WORLD) {
            ResourceLocation deathLootTable = ((MobEntity) innerEntity).getLootTable();
            if (deathLootTable != null) {
                LootTable loottable = getEntityWorld().getServer().getLootTableManager().getLootTableFromLocation(deathLootTable);
                LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) getEntityWorld()))
                        .withRandom(this.rand)
                        .withParameter(LootParameters.THIS_ENTITY, innerEntity)
                        .withParameter(LootParameters.POSITION, new BlockPos(this))
                        .withParameter(LootParameters.DAMAGE_SOURCE, DamageSource.GENERIC)
                        .withNullableParameter(LootParameters.KILLER_ENTITY, null)
                        .withNullableParameter(LootParameters.DIRECT_KILLER_ENTITY, null);

                if (fromPlayer && this.attackingPlayer != null) {
                    lootcontext$builder = lootcontext$builder
                            .withParameter(LootParameters.LAST_DAMAGE_PLAYER, this.attackingPlayer)
                            .withLuck(this.attackingPlayer.getLuck());
                }

                for (ItemStack itemstack : loottable.generate(lootcontext$builder.build(LootParameterSets.ENTITY))) {
                    this.entityDropItem(itemstack, 0.0F);
                }
            }
        }
    }

    @Override
    public void remove() {
    	super.remove();
    	if(world.isRemote() && isVisible()) {
    		spawnSmoke();
    		playSound(getDeathSound(), 0.1F + world.rand.nextFloat() * 0.9F,
    				0.1F + world.rand.nextFloat() * 0.9F);
    	}
    }
    
    @Override
    public boolean isMovementBlocked() {
    	return isFrozen() || getBuildupDuration() > 0;
    }
    
    @Override
    public void livingTick() {
    	super.livingTick();

        if(isVisible()) {
        	if(innerEntity != null) {
	        	innerEntity.deathTime = deathTime;
                innerEntity.setRevengeTarget(getAttackTarget());
	        	//innerEntity.lastAttackerTime = lastAttackerTime;
	        	innerEntity.hurtTime = hurtTime;
	        	innerEntity.rotationPitch = rotationPitch;
	        	innerEntity.rotationYaw = rotationYaw;
	        	innerEntity.rotationYawHead = rotationYawHead;
	        	innerEntity.renderYawOffset = renderYawOffset;
	        	innerEntity.prevRotationPitch = prevRotationPitch;
	        	innerEntity.prevRenderYawOffset = prevRenderYawOffset;
	        	innerEntity.prevRotationYaw = prevRotationYaw;
	        	innerEntity.prevRotationYawHead = prevRotationYawHead;
        	}
        	
        	if(world.isRemote()) {
        		spawnSmoke();
        		if(isSwarm()) {
        			spawnSwarmParticles();
        		}
        	}
        }

        int buildupDuration = getBuildupDuration();
        if(buildupDuration > 0) setBuildupDuration(buildupDuration - 1);

        if(isFrozen()) {
            this.setMotion(0, 0, 0);
        	addFrozenDuration(-1);
        	// TODO: render entangled particles
        } else {
            setRemainingLife(getRemainingLife() - 1);
	        if(getRemainingLife() <= 0) {
	        	this.remove();
	        }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void spawnSmoke() {
        EntitySize size = getSize(getPose());
    	int numParticles = rand.nextInt(5);
    	if(!this.isAlive())
    		numParticles *= 10;
    	float clearRange = size.width; // Particles can't spawn within this X and Z distance
    	for (int i=0; i < numParticles; i++) {            
            double particleX = getPosX() - size.width /2 + size.width * rand.nextFloat();
            if(particleX < 0.7F && particleX >= 0) particleX += size.width /2;
            if(particleX > -0.7F && particleX <= 0) particleX -= size.width /2;
            double particleY = getPosY() + size.height * rand.nextFloat();
            double particleZ = getPosZ() - size.width / 2 + size.width * rand.nextFloat();
            if(particleZ < clearRange && particleZ >= 0) particleZ += size.width /2;
            if(particleZ > -clearRange && particleZ <= 0) particleZ -= size.width /2;
            
            float particleMotionX = (-0.5F + rand.nextFloat()) * 0.05F;
            float particleMotionY = (-0.5F + rand.nextFloat()) * 0.05F;
            float particleMotionZ = (-0.5F + rand.nextFloat()) * 0.05F;

            Minecraft.getInstance().worldRenderer.addParticle(
                    new ParticleDarkSmokeData(!this.isAlive()), false,
                    particleX, particleY, particleZ, particleMotionX, particleMotionY, particleMotionZ);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    private void spawnSwarmParticles() {
        EntitySize size = getSize(getPose());
    	int numParticles = 5 * (rand.nextInt((getSwarmTier() << 1) + 1) + 1);
    	for (int i=0; i < numParticles; i++) {            
            double particleX = getPosX() - size.width /2 + size.width * rand.nextFloat();
            if(particleX < 0.7F && particleX >= 0) particleX += size.width /2;
            if(particleX > -0.7F && particleX <= 0) particleX -= size.width /2;
            double particleY = getPosY() + size.height * rand.nextFloat();
            double particleZ = getPosZ() - size.width / 2 + size.width * rand.nextFloat();
            
            float particleMotionX = (-0.5F + rand.nextFloat()) * 0.05F;
            float particleMotionY = (-0.5F + rand.nextFloat()) * 0.05F;
            float particleMotionZ = (-0.5F + rand.nextFloat()) * 0.05F;

            Minecraft.getInstance().worldRenderer.addParticle(
                    RegistryEntries.PARTICLE_DEGRADE, false,
                    particleX, particleY, particleZ, particleMotionX, particleMotionY, particleMotionZ);
        }
    }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {
        if (soundIn != null && isVisible() && !this.isSilent()) {
            this.world.playSound(this.getPosX(), this.getPosY(), this.getPosZ(), soundIn, this.getSoundCategory(), volume, pitch, true);
        }
    }

    /**
     * If this entity is visible to the current player
     * @return If it is visible
     */
    public boolean isVisible() {
        return world.isRemote() &&
    			(isAlternativelyVisible() || isClientVisible());
    }
    
    @OnlyIn(Dist.CLIENT)
    private boolean isClientVisible() {
    	if (isEnabledVengeance(Minecraft.getInstance().player)) {
            return true;
        }
        for (ItemStack itemStack : Minecraft.getInstance().player.getArmorInventoryList()) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof ItemSpectralGlasses) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isAlternativelyVisible() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
		return EntityVengeanceSpiritConfig.alwaysVisibleInCreative && player != null && player.isCreative();
	}

    @Override
    protected void collideWithNearbyEntities() {
        // Do nothing
    }

    @Override
    public boolean canBePushed() {
        return false;
    }
    
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
    
    @Override
    public void onStruckByLightning(LightningBoltEntity lightning) {
    	setGlobalVengeance(true);
    }
    
    @Override
    public boolean canEntityBeSeen(Entity entity) {
    	if(entity instanceof PlayerEntity)
    		return isEnabledVengeance((PlayerEntity) entity);
    	else
    		return super.canEntityBeSeen(entity);
    }
    
    /**
     * If the given player is vengeanced by this spirit
     * @param player the player.
     * @return If it should be visible.
     */
    public boolean isEnabledVengeance(PlayerEntity player) {
        return isGlobalVengeance() || (player != null && ArrayUtils.contains(getVengeancePlayers(), player.getName()));
	}
    
    /**
     * Enable vengeance of this spirit for the given player.
     * @param player This player will be added to the target list.
     * @param enabled If vengeance should be enabled
     */
    public void setEnabledVengeance(PlayerEntity player, boolean enabled) {
    	String[] players = getVengeancePlayers();
        int index = ArrayUtils.indexOf(players, player.getDisplayName().getString());
    	if(enabled && index == ArrayUtils.INDEX_NOT_FOUND)
    		players = ArrayUtils.add(players, player.getName().getString());
    	else if(!enabled && index != ArrayUtils.INDEX_NOT_FOUND)
    		players = ArrayUtils.remove(players, index);
    	setVengeancePlayers(players);
    }

    public boolean isPlayer() {
        return containsPlayer();
    }

    @Override
    public EntitySize getSize(Pose poseIn) {
        if(isSwarm()) {
            return EntitySize.flexible(getSwarmTier() / 3 + 1, getSwarmTier() / 2 + 1);
        }
        MobEntity innerEntity = getInnerEntity();
        if (innerEntity != null) {
            return innerEntity.getSize(poseIn);
        }
        return super.getSize(poseIn);
    }

    /**
     * Get the inner entity.
     * @return inner entity
     */
    @Nullable
	public MobEntity getInnerEntity() {
    	if(isSwarm()) {
    		return null;
    	}
    	if(innerEntity != null)
    		return innerEntity;
    	try {
            EntityType<?> entityType = data.getInnerEntityType();
            if (entityType != RegistryEntries.ENTITY_VENGEANCE_SPIRIT) {
                Entity entity = entityType.create(world);
                if (canSustain((MobEntity) entity)) {
                    return (MobEntity) entity;
                }
            }
		} catch (NullPointerException | ClassCastException e) {
			EvilCraft.clog("Tried to spirit invalid entity, removing it now.", Level.ERROR);
 		}
        if(!this.world.isRemote()) {
            this.remove();
        }
    	return null;
    }
    
    /**
     * Set the inner entity;
     * @param innerEntity inner entity
     */
    public void setInnerEntity(LivingEntity innerEntity) {
        if(innerEntity instanceof PlayerEntity) {
            setPlayerId(((PlayerEntity) innerEntity).getGameProfile().getId().toString());
            setPlayerName(((PlayerEntity) innerEntity).getGameProfile().getName());
            this.data.setInnerEntityType(EntityType.ZOMBIE);
        } else {
            this.data.setInnerEntityType(innerEntity.getType());
        }
	}

	/**
     * If the given entity can be 'spiritted'
     * @param entityLiving The entity to check.
     * @return If it can become a spirit.
     */
	public static boolean canSustain(LivingEntity entityLiving) {
        String entityName = entityLiving.getType().getRegistryName().toString();
        for (String blacklistedRegex : EntityVengeanceSpiritConfig.entityBlacklist) {
            if (entityName.matches(blacklistedRegex)) {
                return false;
            }
        }
        for (String blacklistedRegex : IMC_BLACKLIST) {
            if (entityName.matches(blacklistedRegex)) {
                return false;
            }
        }
		return true;
	}
	
	/**
     * Check if we can spawn a new vengeance spirit in the given location.
     * It will check if the amount of spirits in an area is below a certain threshold and if there aren't any gemstone
     * torches in the area
	 * @param world The world.
	 * @param blockPos The position.
     * @return If we are allowed to spawn a spirit.
     */
	@SuppressWarnings("unchecked")
	public static boolean canSpawnNew(World world, BlockPos blockPos) {
		int area = EntityVengeanceSpiritConfig.spawnLimitArea;
		int threshold = EntityVengeanceSpiritConfig.spawnLimit;
		AxisAlignedBB box = new AxisAlignedBB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX(), blockPos.getY(), blockPos.getZ()).grow(area, area, area);
    	List<EntityVengeanceSpirit> spirits = world.getEntitiesWithinAABB(EntityVengeanceSpirit.class, box);
		if(spirits.size() >= threshold) {
            return false;
        }

        return WorldHelpers.foldArea(world, BlockGemStoneTorchConfig.area, blockPos,
                new WorldHelpers.WorldFoldingFunction<Boolean, Boolean>() {

            @Nullable
            @Override
            public Boolean apply(@Nullable Boolean input, World world, BlockPos blockPos) {
                // TODO: use block tags
                return (input == null ||input)
                        && world.getBlockState(blockPos).getBlock() != RegistryEntries.BLOCK_GEM_STONE_TORCH
                        && world.getBlockState(blockPos).getBlock() != RegistryEntries.BLOCK_GEM_STONE_TORCH_WALL;
            }

        }, true);
	}
	
	/**
	 * When this spirit is hit by a neutron.
	 * @param hitX Hit X.
	 * @param hitY Hit Y.
	 * @param hitZ Hit Z.
	 * @param impactMotionX The motion speed for X.
	 * @param impactMotionY The motion speed for Y.
	 * @param impactMotionZ The motion speed for Z.
	 */
	public void onHit(double hitX, double hitY, double hitZ,
			double impactMotionX, double impactMotionY, double impactMotionZ) {
		addFrozenDuration(world.rand.nextInt(4) + 3);
		if(world.isRemote()) {
			showBurstParticles(hitX, hitY, hitZ, impactMotionX, impactMotionY, impactMotionZ);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void showBurstParticles(double hitX, double hitY, double hitZ, 
			double impactMotionX, double impactMotionY, double impactMotionZ) {
		for(int i = 0; i < world.rand.nextInt(5); i++) {
			float scale = 0.04F - rand.nextFloat() * 0.02F;
	    	float red = rand.nextFloat() * 0.2F + 0.3F;
	        float green = rand.nextFloat() * 0.2F + 0.3F;
	        float blue = rand.nextFloat() * 0.01F;
	        float ageMultiplier = (float) (rand.nextDouble() * 0.5D + 3D);
	        
	        double dx = 0.1D - rand.nextDouble() * 0.2D - impactMotionX * 0.1D;
	        double dy = 0.1D - rand.nextDouble() * 0.2D - impactMotionY * 0.1D;
	        double dz = 0.1D - rand.nextDouble() * 0.2D - impactMotionZ * 0.1D;

            Minecraft.getInstance().worldRenderer.addParticle(
                    new ParticleBlurData(red, green, blue, scale, ageMultiplier), false,
                    hitX, hitY, hitZ, dx, dy, dz);
		}
	}

	/**
	 * Spawn a random vengeance spirit in the given area.
	 * @param world The world.
	 * @param blockPos The position.
	 * @param area The radius in which the spawn can occur.
	 * @return The spawned spirit, could be null.
	 */
	public static EntityVengeanceSpirit spawnRandom(World world, BlockPos blockPos, int area) {
		EntityVengeanceSpirit spirit = new EntityVengeanceSpirit(world);
		int attempts = 50;
        int baseDistance = 5;
		while(canSpawnNew(world, blockPos) && attempts > 0) {
            BlockPos spawnPos = blockPos.add(
                    MathHelper.nextInt(world.rand, baseDistance, baseDistance + area) * MathHelper.nextInt(world.rand, -1, 1),
                    MathHelper.nextInt(world.rand, 0, 3) * MathHelper.nextInt(world.rand, -1, 1),
                    MathHelper.nextInt(world.rand, baseDistance, baseDistance + area) * MathHelper.nextInt(world.rand, -1, 1)
            );
            
            if(BlockHelpers.doesBlockHaveSolidTopSurface(world, spawnPos.add(0, -1, 0))) {
                spirit.setPosition((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 0.5, (double) spawnPos.getZ() + 0.5);
                if(!world.checkBlockCollision(spirit.getBoundingBox())
                		&& spirit.isNotColliding(world)
                		&& !world.containsAnyLiquid(spirit.getBoundingBox())) {
                    world.addEntity(spirit);
                    return spirit;
                }
            }
			attempts--;
		}
		return null;
	}

    @Override
    public SoundEvent getDeathSound() {
		if(getInnerEntity() != null) {
            return getInnerEntity().getDeathSound();
		}
		return EvilCraftSoundEvents.mob_vengeancespirit_death;
	}
	
	@Override
    public SoundEvent getAmbientSound() {
		LivingEntity entity = getInnerEntity();
		if(entity != null && entity instanceof MobEntity) {
            return ((MobEntity) getInnerEntity()).getAmbientSound();
		}
		return EvilCraftSoundEvents.mob_vengeancespirit_ambient;
	}

    /**
     * Add an entity name to the blacklist, every subinstance of this class will not
     * be spirited anymore.
     * This should only be called by IMC message handlers.
     * @param entityName The entity name or regexthat will be blocked from spiritation.
     */
    public static void addToBlacklistIMC(String entityName) {
        IMC_BLACKLIST.add(entityName);
        EvilCraft.clog("Added entity name " + entityName + " to the spirit blacklist.", Level.TRACE);
    }

    @Override
    public boolean handleWaterMovement() {
        // Ignore water movement and particles
        return this.inWater;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    public static EntityVengeanceSpirit fromNBT(World world, CompoundNBT spiritTag) {
        EntityVengeanceSpirit spirit = new EntityVengeanceSpirit(world);
        spirit.readAdditional(spiritTag);
        return spirit;
    }

    public void addEntanglingPlayer(ServerPlayerEntity player) {
        this.entanglingPlayers.add(player);
    }

    public Set<ServerPlayerEntity> getEntanglingPlayers() {
        return entanglingPlayers;
    }
}
