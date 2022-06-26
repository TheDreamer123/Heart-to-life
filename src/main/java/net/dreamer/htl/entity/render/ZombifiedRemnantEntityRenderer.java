package net.dreamer.htl.entity.render;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.dreamer.htl.entity.render.model.ZombifiedRemnantEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.CreeperChargeFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class ZombifiedRemnantEntityRenderer extends ZombifiedRemnantBaseEntityRenderer<ZombifiedRemnantEntity, ZombifiedRemnantEntityModel<ZombifiedRemnantEntity>> {
    private static final Identifier SCULK_TEXTURE = new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/sculk.png");
    private static final Identifier SOUL_SAND_TEXTURE = new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/soul_ground.png");
    private static final Identifier EMPTY = new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/empty.png");

    public ZombifiedRemnantEntityRenderer(EntityRendererFactory.Context context) {
        this(context, EntityModelLayers.ZOMBIE, EntityModelLayers.ZOMBIE_INNER_ARMOR, EntityModelLayers.ZOMBIE_OUTER_ARMOR);
    }

    public ZombifiedRemnantEntityRenderer(EntityRendererFactory.Context ctx,EntityModelLayer layer,EntityModelLayer legsArmorLayer,EntityModelLayer bodyArmorLayer) {
        super(ctx, new ZombifiedRemnantEntityModel<>(ctx.getPart(layer)), new ZombifiedRemnantEntityModel<>(ctx.getPart(legsArmorLayer)), new ZombifiedRemnantEntityModel<>(ctx.getPart(bodyArmorLayer)));
        this.addFeature(new ZombifiedRemnantGlowFeatureRenderer(this));
        this.addFeature(new ZombifiedRemnantGhostRenderer(this));
        this.addFeature(new ZombifiedRemnantSoulFeatureRenderer(this, ctx.getModelLoader()));
    }

    @Override
    public Identifier getTexture(ZombifiedRemnantEntity entity) {
        if (Objects.equals(entity.getRemnantType(),"sculk")) return SCULK_TEXTURE;
        else if(Objects.equals(entity.getRemnantType(),"soul_ground")) return SOUL_SAND_TEXTURE;
        else return EMPTY;
    }
}
