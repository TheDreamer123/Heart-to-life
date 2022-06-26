package net.dreamer.htl.entity.render.model;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;

@Environment(EnvType.CLIENT)
public abstract class AbstractZombifiedRemnantModel<T extends ZombifiedRemnantEntity> extends BipedEntityModel<T> {
    protected AbstractZombifiedRemnantModel(ModelPart modelPart) {
        super(modelPart);
    }

    public void setAngles(T hostileEntity, float f, float g, float h, float i, float j) {
        super.setAngles(hostileEntity, f, g, h, i, j);
        CrossbowPosing.meleeAttack(this.leftArm, this.rightArm, this.isAttacking(hostileEntity), this.handSwingProgress, h);
    }

    public abstract boolean isAttacking(T entity);
}
