package net.dreamer.htl.entity.goal;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class RemnantWanderAroundFarGoal extends WanderAroundGoal {
    public static final float CHANCE = 0.001F;
    protected final float probability;

    public RemnantWanderAroundFarGoal(PathAwareEntity pathAwareEntity,double d) {
        this(pathAwareEntity, d, CHANCE);
    }

    public RemnantWanderAroundFarGoal(PathAwareEntity mob, double speed, float probability) {
        super(mob, speed);
        this.probability = probability;
    }

    @Override
    public boolean canStart() {
        if(this.mob instanceof ZombifiedRemnantEntity zombifiedRemnantEntity && zombifiedRemnantEntity.getStayingStill()) return false;

        return super.canStart();
    }

    @Nullable
    protected Vec3d getWanderTarget() {
        if (this.mob.isInsideWaterOrBubbleColumn()) {
            Vec3d vec3d = FuzzyTargeting.find(this.mob, 15, 7);
            return vec3d == null ? super.getWanderTarget() : vec3d;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? FuzzyTargeting.find(this.mob, 10, 7) : super.getWanderTarget();
        }
    }
}
