package net.dreamer.htl.entity.render.model;

import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;

@Environment(EnvType.CLIENT)
public class ZombifiedRemnantEntityModel<T extends ZombifiedRemnantEntity> extends AbstractZombifiedRemnantModel<T> {
    public ZombifiedRemnantEntityModel(ModelPart modelPart) {
        super(modelPart);
    }

    public boolean isAttacking(T zombieEntity) {
        return zombieEntity.isAttacking();
    }
}
