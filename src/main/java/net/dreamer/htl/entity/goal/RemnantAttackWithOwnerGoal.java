package net.dreamer.htl.entity.goal;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

import java.util.EnumSet;
import java.util.Objects;

public class RemnantAttackWithOwnerGoal extends TrackTargetGoal {
    private final ZombifiedRemnantEntity remnant;
    private LivingEntity attacking;
    private int lastAttackTime;

    public RemnantAttackWithOwnerGoal(ZombifiedRemnantEntity remnant) {
        super(remnant, false);
        this.remnant = remnant;
        this.setControls(EnumSet.of(Control.TARGET));
    }

    public boolean canStart() {
        if (this.remnant.isTamed() && !Objects.equals(this.remnant.getRemnantType(),"ghost")) {
            LivingEntity livingEntity = this.remnant.getOwner();
            if (livingEntity == null) {
                return false;
            } else {
                this.attacking = livingEntity.getAttacking();
                int i = livingEntity.getLastAttackTime();
                return i != this.lastAttackTime && this.canTrack(this.attacking, TargetPredicate.DEFAULT) && this.remnant.canAttackWithOwner(this.attacking, livingEntity) && this.attacking.getType() != EntityType.WARDEN && !this.remnant.getStayingStill();
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.attacking);
        LivingEntity livingEntity = this.remnant.getOwner();
        if (livingEntity != null) {
            this.lastAttackTime = livingEntity.getLastAttackTime();
        }

        super.start();
    }
}
