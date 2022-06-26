package net.dreamer.htl.entity.render;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class ZombifiedRemnantGlowFeatureRenderer<T extends ZombifiedRemnantEntity> extends EyesFeatureRenderer<T, BipedEntityModel<T>> {
    private static final RenderLayer EYES = RenderLayer.getEyes(new Identifier(HeartToLife.MOD_ID, "textures/entity/remnant/remnant_glow.png"));

    public ZombifiedRemnantGlowFeatureRenderer(FeatureRendererContext<T, BipedEntityModel<T>> featureRendererContext) {
        super(featureRendererContext);
    }

    public void render(MatrixStack matrices,VertexConsumerProvider vertexConsumers,int light,T entity,float limbAngle,float limbDistance,float tickDelta,float animationProgress,float headYaw,float headPitch) {
        if (!entity.isInvisible() && !Objects.equals(entity.getRemnantType(),"ghost") && !entity.getStayingStill()) {
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.getEyesTexture());
            this.getContextModel().render(matrices,vertexConsumer,15728640,OverlayTexture.DEFAULT_UV,1.0F,1.0F,1.0F,1.0F);
        }
    }

    public RenderLayer getEyesTexture() {
        return EYES;
    }
}
