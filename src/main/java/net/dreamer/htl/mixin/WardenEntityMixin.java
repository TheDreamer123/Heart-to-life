package net.dreamer.htl.mixin;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WardenEntity.class)
public abstract class WardenEntityMixin extends HostileEntity {
    private int wasHeartDropped = 0;

    protected WardenEntityMixin(EntityType<? extends HostileEntity> entityType,World world) {
        super(entityType,world);
    }

    @Inject(at = @At("HEAD"), method = "isValidTarget", cancellable = true)
    public void isValidTargetInject(Entity entity,CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ZombifiedRemnantEntity) cir.setReturnValue(false);
    }

    @Inject(at = @At("HEAD"), method = "writeCustomDataToNbt")
    public void writeCustomDataToNbtInject(NbtCompound nbt,CallbackInfo info) {
        nbt.putInt("wasHeartDropped", this.wasHeartDropped);
    }

    @Inject(at = @At("HEAD"), method = "readCustomDataFromNbt")
    public void readCustomDataFromNbtInject(NbtCompound nbt,CallbackInfo info) {
        this.wasHeartDropped = nbt.getInt("wasHeartDropped");
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void tickInject(CallbackInfo info) {
        if((isDead() && hurtTime == 1) || (this.getHealth() <= (this.getMaxHealth() - (this.getMaxHealth() / 10))) && this.getPose() == EntityPose.DIGGING && wasHeartDropped == 0) {
            dropStack(new ItemStack(HeartToLife.WARDEN_HEART));
            wasHeartDropped = 1;
        }
    }
}
