package net.dreamer.htl.entity.goal;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class RemnantAttackGoal extends MeleeAttackGoal {
    private final ZombifiedRemnantEntity remnant;
    private int ticks;

    public RemnantAttackGoal(ZombifiedRemnantEntity remnant,double speed,boolean pauseWhenMobIdle) {
        super(remnant, speed, pauseWhenMobIdle);
        this.remnant = remnant;
    }

    public void start() {
        super.start();
        this.ticks = 0;
    }

    public void stop() {
        super.stop();
        this.remnant.setAttacking(false);
    }

    public void tick() {
        super.tick();
        ++this.ticks;
        this.remnant.setAttacking(this.ticks >= 5 && this.getCooldown() < this.getMaxCooldown() / 2);

    }
}
