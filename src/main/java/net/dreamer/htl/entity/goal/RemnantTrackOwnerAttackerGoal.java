package net.dreamer.htl.entity.goal;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;

import java.util.EnumSet;
import java.util.Objects;

public class RemnantTrackOwnerAttackerGoal extends TrackTargetGoal {
    private final ZombifiedRemnantEntity remnant;
    private LivingEntity attacker;
    private int lastAttackedTime;

    public RemnantTrackOwnerAttackerGoal(ZombifiedRemnantEntity remnant) {
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
                this.attacker = livingEntity.getAttacker();
                int i = livingEntity.getLastAttackedTime();
                return i != this.lastAttackedTime && this.canTrack(this.attacker, TargetPredicate.DEFAULT) && this.remnant.canAttackWithOwner(this.attacker, livingEntity) && this.attacker.getType() != EntityType.WARDEN && !this.remnant.getStayingStill();
            }
        } else {
            return false;
        }
    }

    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity livingEntity = this.remnant.getOwner();
        if (livingEntity != null) {
            this.lastAttackedTime = livingEntity.getLastAttackedTime();
        }

        super.start();
    }
}
