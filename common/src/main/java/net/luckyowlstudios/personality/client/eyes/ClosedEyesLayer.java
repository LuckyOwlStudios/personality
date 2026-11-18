package net.luckyowlstudios.personality.client.eyes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Draws eyelids over a sleeping mob's eyes. Added to every living entity renderer; it only draws for
 * mobs that have an eyelid definition (see {@link EyelidManager}).
 */
public class ClosedEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    /** Eyes shut once the mob is this far into lying down. */
    private static final float EYES_CLOSED_AT = 0.6F;

    public ClosedEyesLayer(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof SleepingMob sleeper) || entity.isInvisible()) {
            return;
        }
        if (sleeper.personality$getSleepProgress(partialTick) < EYES_CLOSED_AT) {
            return;
        }
        ResourceLocation eyelids = EyelidManager.getEyelidTexture(entity.getType(), this.getTextureLocation(entity));
        if (eyelids == null) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(eyelids));
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), -1);
    }
}
