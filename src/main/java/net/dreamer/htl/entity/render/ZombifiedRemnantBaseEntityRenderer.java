package net.dreamer.htl.entity.render;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.dreamer.htl.entity.render.model.ZombifiedRemnantEntityModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class ZombifiedRemnantBaseEntityRenderer<T extends ZombifiedRemnantEntity, M extends ZombifiedRemnantEntityModel<T>> extends BipedEntityRenderer<T, M> {
    private static final Identifier TEXTURE = new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/sculk.png");

    protected ZombifiedRemnantBaseEntityRenderer(EntityRendererFactory.Context ctx,M bodyModel,M legsArmorModel,M bodyArmorModel) {
        super(ctx, bodyModel, 0.5F);
        this.addFeature(new ArmorFeatureRenderer<>(this, legsArmorModel, bodyArmorModel));
    }

    public Identifier getTexture(ZombifiedRemnantEntity zombieEntity) {
        return TEXTURE;
    }
}
