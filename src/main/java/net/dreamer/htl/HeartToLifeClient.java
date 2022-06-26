package net.dreamer.htl;

import net.dreamer.htl.entity.render.ZombifiedRemnantEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class HeartToLifeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(HeartToLife.SOUL_CLUSTER, RenderLayer.getTranslucent());

        EntityRendererRegistry.register(HeartToLife.ZOMBIFIED_REMNANT, ZombifiedRemnantEntityRenderer::new);
    }
}
