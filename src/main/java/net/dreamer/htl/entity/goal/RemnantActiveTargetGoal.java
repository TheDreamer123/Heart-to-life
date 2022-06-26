package net.dreamer.htl.entity.goal;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.dreamer.htl.util.HtlEntityTypeTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Predicate;

public class RemnantActiveTargetGoal<T extends LivingEntity> extends TrackTargetGoal {
    protected final Class<T> targetClass;
    protected final int reciprocalChance;
    @Nullable protected LivingEntity targetEntity;
    protected TargetPredicate targetPredicate;
    public ZombifiedRemnantEntity mob;

    public RemnantActiveTargetGoal(ZombifiedRemnantEntity mob,Class<T> targetClass,boolean checkVisibility) {
        this(mob, targetClass, 10, checkVisibility, false,null);
    }

    public RemnantActiveTargetGoal(ZombifiedRemnantEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
        super(mob, checkVisibility, checkCanNavigate);
        this.targetClass = targetClass;
        this.reciprocalChance = toGoalTicks(reciprocalChance);
        this.setControls(EnumSet.of(Control.TARGET));
        this.targetPredicate = TargetPredicate.createAttackable().setBaseMaxDistance(this.getFollowRange()).setPredicate(targetPredicate);
        this.mob = mob;
    }

    public boolean canStart() {
        if (this.reciprocalChance <= 0 || this.mob.getRandom().nextInt(this.reciprocalChance) == 0) {
            this.findClosestTarget();
            if (this.targetEntity != null) return (!Objects.equals(this.mob.getRemnantType(),"ghost") ? targetEntity instanceof PlayerEntity && !this.mob.isTamed() : targetEntity.getType().isIn(HtlEntityTypeTags.CONVERTIBLE_UNDEAD));

        }
        return false;
    }

    protected Box getSearchBox(double distance) {
        return this.mob.getBoundingBox().expand(distance, 4.0D, distance);
    }

    protected void findClosestTarget() {
        if (this.targetClass != PlayerEntity.class && this.targetClass != ServerPlayerEntity.class) {
            this.targetEntity = this.mob.world.getClosestEntity(this.mob.world.getEntitiesByClass(this.targetClass, this.getSearchBox(this.getFollowRange()), (livingEntity) -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        } else {
            this.targetEntity = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        }

    }

    public void start() {
        this.mob.setTarget(this.targetEntity);
        super.start();
    }
}
