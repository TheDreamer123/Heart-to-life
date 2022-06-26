package net.dreamer.htl.mixin;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected int playerHitTimer;
    @Shadow @Nullable protected PlayerEntity attackingPlayer;

    public LivingEntityMixin(EntityType<?> type,World world) {
        super(type,world);
    }

    @Inject(at = @At("HEAD"), method = "damage")
    public void damageInject(DamageSource source,float amount,CallbackInfoReturnable<Boolean> cir) {
        Entity htlEntity = source.getAttacker();
        if (htlEntity != null) {
            if (htlEntity instanceof ZombifiedRemnantEntity zombifiedRemnantEntity) {
                if (zombifiedRemnantEntity.isTamed()) {
                    this.playerHitTimer = 100;
                    LivingEntity livingEntity = zombifiedRemnantEntity.getOwner();
                    if (livingEntity != null && livingEntity.getType() == EntityType.PLAYER) {
                        this.attackingPlayer = (PlayerEntity) livingEntity;
                    } else {
                        this.attackingPlayer = null;
                    }
                }
            }
        }
    }
}
