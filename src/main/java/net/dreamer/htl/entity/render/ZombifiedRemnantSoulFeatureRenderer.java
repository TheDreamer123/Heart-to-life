package net.dreamer.htl.entity.render;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.dreamer.htl.entity.render.model.ZombifiedRemnantEntityModel;
import net.minecraft.client.render.entity.feature.EnergySwirlOverlayFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.util.Identifier;

public class ZombifiedRemnantSoulFeatureRenderer extends EnergySwirlOverlayFeatureRenderer<ZombifiedRemnantEntity, ZombifiedRemnantEntityModel<ZombifiedRemnantEntity>> {
    private static final Identifier SKIN = new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/souls.png");
    private final ZombifiedRemnantEntityModel<ZombifiedRemnantEntity> model;

    public ZombifiedRemnantSoulFeatureRenderer(FeatureRendererContext<ZombifiedRemnantEntity, ZombifiedRemnantEntityModel<ZombifiedRemnantEntity>> context,EntityModelLoader loader) {
        super(context);
        this.model = new ZombifiedRemnantEntityModel<>(loader.getModelPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR));
    }

    protected float getEnergySwirlX(float partialAge) {
        return partialAge * 0.01F;
    }

    protected Identifier getEnergySwirlTexture() {
        return SKIN;
    }

    protected EntityModel<ZombifiedRemnantEntity> getEnergySwirlModel() {
        return this.model;
    }
}
