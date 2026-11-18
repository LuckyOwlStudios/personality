package net.luckyowlstudios.personality.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luckyowlstudios.personality.client.eyes.ClosedEyesLayer;
import net.luckyowlstudios.personality.config.PersonalityConfig;
import net.luckyowlstudios.personality.client.sleep.SleepPose;
import net.luckyowlstudios.personality.client.sleep.SleepRenderContext;
import net.luckyowlstudios.personality.sleep.SleepingMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow
    protected EntityModel<?> model;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "<init>", at = @At("TAIL"))
    private void personality$addClosedEyesLayer(EntityRendererProvider.Context context, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        LivingEntityRenderer renderer = (LivingEntityRenderer) (Object) this;
        ((LivingEntityRendererAccessor) renderer).personality$getLayers().add(new ClosedEyesLayer(renderer));
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void personality$beginSleepPose(LivingEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof SleepingMob sleeper) {
            float progress = sleeper.personality$getSleepProgress(partialTick);
            SleepRenderContext.push(progress, progress > 0.0F && PersonalityConfig.LIE_DOWN.get() && SleepPose.shouldFoldLegs(entity, this.model));
        } else {
            SleepRenderContext.push(0.0F, false);
        }
    }

    /** Lower the body so the folded legs rest on the ground. Runs after the renderer's own scale. */
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;scale(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", shift = At.Shift.AFTER))
    private void personality$lowerSleepingBody(LivingEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        SleepRenderContext.State state = SleepRenderContext.current();
        if (state.active() && state.foldLegs()) {
            float pixels = SleepPose.loweringPixels(entity, this.model) * SleepPose.ease(state.progress());
            // Model space is flipped (y points down) at this point, so +y lowers the model.
            poseStack.translate(0.0F, pixels / 16.0F, 0.0F);
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void personality$endSleepPose(LivingEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        SleepRenderContext.pop();
    }
}
