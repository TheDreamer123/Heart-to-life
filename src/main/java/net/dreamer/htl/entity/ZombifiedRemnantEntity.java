package net.dreamer.htl.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.goal.*;
import net.dreamer.htl.util.HtlEntityTypeTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.client.render.entity.feature.SkinOverlayOwner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class ZombifiedRemnantEntity extends TameableEntity implements SkinOverlayOwner {
    private static final TrackedData<String> REMNANT_TYPE;
    private static final TrackedData<String> PREVIOUS_TYPE;
    protected static final TrackedData<Byte> TAMEABLE_FLAGS;
    protected static final TrackedData<Optional<UUID>> OWNER_UUID;
    public boolean notStable;
    private int maximumStabilityTime;
    private int stabilityTime;
    private int usedSouls;
    protected boolean isStayingStill;
    private static final UUID SOUL_SPEED_BOOST_ID = UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038");
    private static final ObjectArrayList OFFSETS = Util.make(new ObjectArrayList(18), (objectArrayList) -> {
        Stream var10000 = BlockPos.stream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter((pos) -> (pos.getX() == 0 || pos.getY() == 0 || pos.getZ() == 0) && !pos.equals(BlockPos.ORIGIN)).map(BlockPos::toImmutable);
        Objects.requireNonNull(objectArrayList);
        var10000.forEach(objectArrayList::add);
    });

    public ZombifiedRemnantEntity(World world) {
        super(HeartToLife.ZOMBIFIED_REMNANT,world);
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return HeartToLife.REMNANT_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return HeartToLife.REMNANT_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return HeartToLife.REMNANT_TALK;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(HeartToLife.REMNANT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new FleeEntityGoal<>(this, WardenEntity.class, 8.0F, 1.6D, 1.4D));
        this.targetSelector.add(1,new RemnantTrackOwnerAttackerGoal(this));
        this.goalSelector.add(1, new SwimGoal(this));
        this.targetSelector.add(2, new RemnantActiveTargetGoal<>(this, LivingEntity.class, true));
        this.goalSelector.add(2, new RemnantAttackGoal(this,1.0D, false));
        this.targetSelector.add(2,new RemnantAttackWithOwnerGoal(this));
        this.goalSelector.add(2, new SitGoal(this));
        this.targetSelector.add(3, new RevengeGoal(this));
        this.goalSelector.add(5, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.add(6, new RemnantFollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.add(7, new RemnantWanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(7, new RemnantWanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(10, new LookAroundGoal(this));
        this.goalSelector.add(10, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    }

    public boolean isTamed() {
        return (this.dataTracker.get(TAMEABLE_FLAGS) & 4) != 0;
    }

    public void setTamed(boolean tamed) {
        byte b = this.dataTracker.get(TAMEABLE_FLAGS);
        if (tamed) this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b | 4));
        else this.dataTracker.set(TAMEABLE_FLAGS, (byte)(b & -5));

        this.onTamedChanged();
    }

    @Nullable
    public UUID getOwnerUuid() {
        return (this.dataTracker.get(OWNER_UUID)).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(PlayerEntity player) {
        this.setTamed(true);
        this.setOwnerUuid(player.getUuid());
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.world.getPlayerByUuid(uUID);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    public boolean canTarget(LivingEntity target) {
        return !this.isOwner(target) && super.canTarget(target);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (!(target instanceof CreeperEntity) && !(target instanceof GhastEntity)) {
            if (target instanceof ZombifiedRemnantEntity tameableZombieEntity) return !tameableZombieEntity.isTamed() || tameableZombieEntity.getOwner() != owner;
            else if (target instanceof PlayerEntity && owner instanceof PlayerEntity && !((PlayerEntity)owner).shouldDamagePlayer((PlayerEntity)target)) return false;
            else if (target instanceof HorseEntity && ((HorseEntity)target).isTame()) return false;
            else return !(target instanceof TameableEntity) || !((TameableEntity)target).isTamed();
        } else return false;
    }

    public AbstractTeam getScoreboardTeam() {
        if (this.isTamed()) {
            LivingEntity livingEntity = this.getOwner();
            if (livingEntity != null) return livingEntity.getScoreboardTeam();
        }

        return super.getScoreboardTeam();
    }

    public boolean isTeammate(Entity other) {
        if (this.isTamed()) {
            LivingEntity livingEntity = this.getOwner();
            if (other == livingEntity) return true;

            if (livingEntity != null) return livingEntity.isTeammate(other);
        }

        return super.isTeammate(other);
    }

    @Override
    public boolean damage(DamageSource source,float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else {
            Entity entity = source.getAttacker();
            if (!this.world.isClient) {
                this.setStayingStill(false);
            }

            if (entity != null && !(entity instanceof PlayerEntity) && !(entity instanceof PersistentProjectileEntity)) {
                amount = (amount + 1.0F) / 2.0F;
            }

            return super.damage(source, amount);
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);

        if(!isTamed()) {
            if (itemStack.isOf(Items.SCULK) && Objects.equals(this.dataTracker.get(REMNANT_TYPE),"sculk")) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.world.sendEntityStatus(this,(byte) 7);
                } else {
                    this.world.sendEntityStatus(this,(byte) 6);
                }

                return ActionResult.SUCCESS;
            } else if (itemStack.isIn(ItemTags.SOUL_FIRE_BASE_BLOCKS) && Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground")) {
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if (this.random.nextInt(3) == 0) {
                    this.setOwner(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.world.sendEntityStatus(this,(byte) 7);
                } else {
                    this.world.sendEntityStatus(this,(byte) 6);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            if(player == this.getOwner()) {
                if(Objects.equals(this.dataTracker.get(REMNANT_TYPE),"sculk") && this.getHealth() < this.getMaxHealth() && itemStack.isOf(Items.SCULK)) {
                    this.heal(3.0F);
                    itemStack.decrement(1);
                    return ActionResult.SUCCESS;
                } else if(Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground") && this.getHealth() < this.getMaxHealth() && itemStack.isIn(ItemTags.SOUL_FIRE_BASE_BLOCKS)) {
                    this.heal(3.0F);
                    itemStack.decrement(1);
                    return ActionResult.SUCCESS;
                } else if(!Objects.equals(this.dataTracker.get(REMNANT_TYPE),"ghost")) {
                    this.setStayingStill(!this.getStayingStill());
                    return ActionResult.SUCCESS;
                }
            }
        }

        return super.interactAt(player,hitPos,hand);
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return !isTamed();
    }

    public String getRemnantType() {
        return this.dataTracker.get(REMNANT_TYPE);
    }

    public void setRemnantType(String type) {
        this.dataTracker.set(REMNANT_TYPE, type);
    }

    private boolean getNotStable() {
        return notStable;
    }

    private void setNotStable() {
        this.notStable = true;
    }

    private void setMaximumStabilityTime(int stabilityTime) {
        this.maximumStabilityTime = stabilityTime;
    }

    private void setUsedSouls(int getUsedSouls) {
        this.usedSouls = getUsedSouls;
    }

    private void setPreviousType(String type) {
        this.dataTracker.set(PREVIOUS_TYPE, type);
    }

    public boolean getStayingStill() {
        return isStayingStill;
    }

    public void setStayingStill(boolean newValue) {
        this.isStayingStill = newValue;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world,PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean shouldRenderOverlay() {
        return this.getStayingStill();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(REMNANT_TYPE, "sculk");
        this.dataTracker.startTracking(PREVIOUS_TYPE, "sculk");
        this.dataTracker.startTracking(TAMEABLE_FLAGS, (byte)0);
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("remnantType", this.dataTracker.get(REMNANT_TYPE));
        nbt.putBoolean("notStable", this.notStable);
        nbt.putInt("maximumStabilityTime", this.maximumStabilityTime);
        nbt.putInt("stabilityTime", this.stabilityTime);
        nbt.putInt("usedSouls", this.usedSouls);
        nbt.putBoolean("isStayingStill", this.isStayingStill);
        nbt.putString("previousType", this.dataTracker.get(PREVIOUS_TYPE));
        if (this.getOwnerUuid() != null) nbt.putUuid("Owner", this.getOwnerUuid());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (Objects.equals(nbt.getString("remnantType"), "sculk") || Objects.equals(nbt.getString("remnantType"), "soul_ground") || Objects.equals(nbt.getString("remnantType"), "ghost")) this.dataTracker.set(REMNANT_TYPE,nbt.getString("remnantType"));
        this.notStable = nbt.getBoolean("notStable");
        this.maximumStabilityTime = nbt.getInt("maximumStabilityTime");
        this.stabilityTime = nbt.getInt("stabilityTime");
        this.usedSouls = nbt.getInt("usedSouls");
        this.dataTracker.set(PREVIOUS_TYPE, nbt.getString("previousType"));
        this.isStayingStill = nbt.getBoolean("isStayingStill");
        UUID uUID;
        if (nbt.containsUuid("Owner")) uUID = nbt.getUuid("Owner");
        else {
            String string = nbt.getString("Owner");
            uUID = ServerConfigHandler.getPlayerUuidByName(this.getServer(),string);
        }

        if (uUID != null)
            try {
                this.setOwnerUuid(uUID);
                this.setTamed(true);
            } catch (Throwable var4) {
                this.setTamed(false);
            }
    }

    private static List<Vec3i> shuffleOffsets(Random random) {
        return Util.copyShuffled(OFFSETS, random);
    }

    @Override
    public void remove(RemovalReason reason) {
        PlayerEntity owner = (PlayerEntity) this.getOwner();
        if (!this.world.isClient && !Objects.equals(this.dataTracker.get(REMNANT_TYPE),"ghost") && this.isDead() && !this.getNotStable()) {
            if (this.usedSouls < (Objects.equals(this.dataTracker.get(REMNANT_TYPE),"sculk") ? 3 : 7)) {
                Text text = this.getCustomName();
                boolean bl = this.isAiDisabled();
                int getUsedSouls = this.usedSouls;
                String type = this.getRemnantType();
                ZombifiedRemnantEntity zombifiedRemnantEntity = HeartToLife.ZOMBIFIED_REMNANT.create(world);
                if (zombifiedRemnantEntity != null) {
                    if (this.isPersistent()) zombifiedRemnantEntity.setPersistent();
                    zombifiedRemnantEntity.setCustomName(text);
                    zombifiedRemnantEntity.setAiDisabled(bl);
                    zombifiedRemnantEntity.setInvulnerable(this.isInvulnerable());
                    zombifiedRemnantEntity.refreshPositionAndAngles(this.getX(),this.getY(),this.getZ(),this.random.nextFloat() * 360.0F,0.0F);
                    zombifiedRemnantEntity.setRemnantType("ghost");
                    zombifiedRemnantEntity.setNotStable();
                    zombifiedRemnantEntity.setMaximumStabilityTime(1200);
                    if (owner != null) zombifiedRemnantEntity.setOwner(owner);
                    zombifiedRemnantEntity.setUsedSouls(getUsedSouls + 1);
                    zombifiedRemnantEntity.setPreviousType(type);
                }
                world.spawnEntity(zombifiedRemnantEntity);
            } else {
                Text text = this.getCustomName();
                boolean bl = this.isAiDisabled();
                HuskEntity huskEntity = EntityType.HUSK.create(world);
                if (huskEntity != null) {
                    if (this.isPersistent()) huskEntity.setPersistent();
                    huskEntity.setCustomName(text);
                    huskEntity.setAiDisabled(bl);
                    huskEntity.setInvulnerable(this.isInvulnerable());
                    huskEntity.refreshPositionAndAngles(this.getX(),this.getY(),this.getZ(),this.random.nextFloat() * 360.0F,0.0F);
                }
                world.spawnEntity(huskEntity);
            }
        }

        if(!this.world.isClient && Objects.equals(this.dataTracker.get(REMNANT_TYPE),"sculk") && this.isDead()) {
            SculkSpreadManager spreadManager = SculkSpreadManager.create();
            for(int i = 0; i < 30; i++) {
                spreadManager.spread(new BlockPos(this.getPos().withBias(Direction.UP,0.5D)),1);
                spreadManager.tick(world,this.getBlockPos(),world.getRandom(),true);
            }

            for(int i = 0; i < world.random.nextInt(4) && !this.getNotStable(); i++) {
                BlockPos.Mutable mutable = null;
                while (mutable == null || world.getBlockState(mutable).isSolidBlock(world, mutable) && world.getBlockState(mutable.setY(mutable.getY() + 1)).isSolidBlock(world, mutable.setY(mutable.getY() + 1))) {
                    mutable = getBlockPos().mutableCopy();

                    for (Vec3i vec3i : shuffleOffsets(random)) mutable.set(getBlockPos(),vec3i);
                }
                ZombifiedRemnantEntity zombifiedRemnantEntity = HeartToLife.ZOMBIFIED_REMNANT.create(world);
                if(zombifiedRemnantEntity != null) {
                    zombifiedRemnantEntity.refreshPositionAndAngles(mutable.getX(), mutable.getY(), mutable.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
                    zombifiedRemnantEntity.setRemnantType("sculk");
                    zombifiedRemnantEntity.setNotStable();
                    zombifiedRemnantEntity.setMaximumStabilityTime(600);
                    if(owner != null) zombifiedRemnantEntity.setOwner(owner);
                }

                world.spawnEntity(zombifiedRemnantEntity);
            }
        }

        super.remove(reason);
    }

    @Override
    public boolean onKilledOther(ServerWorld world,LivingEntity other) {
        if (Objects.equals(this.getRemnantType(),"sculk") && other.getType().isIn(HtlEntityTypeTags.CONVERTIBLE_UNDEAD) && !(other instanceof ZombifiedRemnantEntity)) {
            MobEntity mobEntity = (MobEntity) other;
            ZombifiedRemnantEntity zombifiedRemnantEntity = mobEntity.convertTo(HeartToLife.ZOMBIFIED_REMNANT,false);
            if (zombifiedRemnantEntity != null) {
                zombifiedRemnantEntity.setRemnantType("sculk");
                zombifiedRemnantEntity.setMaximumStabilityTime(1200);
                zombifiedRemnantEntity.setNotStable();
                if(this.getOwner() != null) zombifiedRemnantEntity.setOwner((PlayerEntity) this.getOwner());
            }
            if (!this.isSilent()) {
                world.syncWorldEvent(null,1026,this.getBlockPos(),0);
            }
            return super.onKilledOther(world, other);
        }

        return super.onKilledOther(world, other);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (Objects.equals(getRemnantType(),"sculk")) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(2.0D);
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.33000000417232513D);
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(16.0D);
        } else if (Objects.equals(getRemnantType(),"soul_ground")) {
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)).setBaseValue(4.0D);
            Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.16000000417232513D);
        }

        if(this.getNotStable()) {
            if (this.stabilityTime < this.maximumStabilityTime) this.stabilityTime++;
            if (this.stabilityTime > this.maximumStabilityTime - 1) {
                this.remove(RemovalReason.DISCARDED);
                if(Objects.equals(this.dataTracker.get(REMNANT_TYPE), "sculk")) {
                    SculkSpreadManager spreadManager = SculkSpreadManager.create();
                    for (int i = 0; i < 30; i++) {
                        spreadManager.spread(new BlockPos(this.getPos().withBias(Direction.UP,0.5D)),1);
                        spreadManager.tick(world,this.getBlockPos(),world.getRandom(),true);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldDisplaySoulSpeedEffects() {
        return Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground") ? this.age % 5 == 0 && this.getVelocity().x != 0.0D && this.getVelocity().z != 0.0D && !this.isSpectator() && this.isOnGround() : super.shouldDisplaySoulSpeedEffects();
    }

    @Override
    protected float getVelocityMultiplier() {
        return Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground") ? 1.0F : super.getVelocityMultiplier();
    }

    @Override
    protected void addSoulSpeedBoostIfNeeded() {
        if(Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground")) {
            if (!this.getLandingBlockState().isAir()) {
                EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                if (entityAttributeInstance == null) {
                    return;
                }

                entityAttributeInstance.addTemporaryModifier(new EntityAttributeModifier(SOUL_SPEED_BOOST_ID, "Soul speed boost",0.03F * (1.0F + (float)3 * 0.35F), EntityAttributeModifier.Operation.ADDITION));
            }
        } else super.addSoulSpeedBoostIfNeeded();
    }

    public boolean tryAttack(Entity target) {
        if (!super.tryAttack(target)) return false;
        if (Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground")) {
            if (target instanceof LivingEntity) {
                ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS,100,1),this);
                ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS,40),this);
            }
            return true;
        } else if (Objects.equals(this.dataTracker.get(REMNANT_TYPE),"ghost") && target instanceof MobEntity mobEntity) {
            ZombifiedRemnantEntity zombifiedRemnantEntity = mobEntity.convertTo(HeartToLife.ZOMBIFIED_REMNANT, false);
            if(zombifiedRemnantEntity != null) {
                zombifiedRemnantEntity.setRemnantType(this.dataTracker.get(PREVIOUS_TYPE));
                zombifiedRemnantEntity.setUsedSouls(this.usedSouls);
                if(this.getOwner() != null) zombifiedRemnantEntity.setOwner((PlayerEntity) this.getOwner());
            }
            this.remove(RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground");
    }

    @Override
    public boolean canWalkOnFluid(FluidState state) {
        return Objects.equals(this.dataTracker.get(REMNANT_TYPE),"soul_ground") ? state.isIn(FluidTags.LAVA) : super.canWalkOnFluid(state);
    }

    @Override
    public boolean isPushedByFluids() {
        return !Objects.equals(this.dataTracker.get(REMNANT_TYPE), "ghost");
    }

    public boolean isPushable() {
        return !Objects.equals(this.dataTracker.get(REMNANT_TYPE), "ghost");
    }

    protected void pushAway(Entity entity) {
        if (!Objects.equals(this.dataTracker.get(REMNANT_TYPE), "ghost")) super.pushAway(entity);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return Objects.equals(this.dataTracker.get(REMNANT_TYPE), "ghost") ? damageSource != DamageSource.OUT_OF_WORLD : super.isInvulnerableTo(damageSource);
    }

    @Override
    public boolean isAttackable() {
        return !Objects.equals(this.dataTracker.get(REMNANT_TYPE), "ghost");
    }

    static {
        REMNANT_TYPE = DataTracker.registerData(ZombifiedRemnantEntity.class, TrackedDataHandlerRegistry.STRING);
        TAMEABLE_FLAGS = DataTracker.registerData(ZombifiedRemnantEntity.class, TrackedDataHandlerRegistry.BYTE);
        OWNER_UUID = DataTracker.registerData(ZombifiedRemnantEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
        PREVIOUS_TYPE = DataTracker.registerData(ZombifiedRemnantEntity.class, TrackedDataHandlerRegistry.STRING);
    }
}
