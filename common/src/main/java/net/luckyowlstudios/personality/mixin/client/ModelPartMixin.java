package net.luckyowlstudios.personality.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.luckyowlstudios.personality.client.sleep.NamedModelPart;
import net.luckyowlstudios.personality.client.sleep.SleepPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements NamedModelPart {

    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Shadow
    @Final
    private Map<String, ModelPart> children;

    @Shadow
    public abstract void loadPose(PartPose pose);

    @Unique
    private String personality$name = "";

    @Unique
    private PartPose personality$savedPose;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void personality$nameChildren(List<ModelPart.Cube> cubes, Map<String, ModelPart> children, CallbackInfo ci) {
        children.forEach((name, child) -> NamedModelPart.of(child).personality$setName(name));
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"))
    private void personality$applySleepPose(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        this.personality$savedPose = SleepPose.apply((ModelPart) (Object) this);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("RETURN"))
    private void personality$restorePose(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        if (this.personality$savedPose != null) {
            this.loadPose(this.personality$savedPose);
            this.personality$savedPose = null;
        }
    }

    @Override
    public String personality$getName() {
        return this.personality$name;
    }

    @Override
    public void personality$setName(String name) {
        this.personality$name = name;
    }

    @Override
    public Map<String, ModelPart> personality$getChildren() {
        return this.children;
    }

    @Override
    public List<ModelPart.Cube> personality$getCubes() {
        return this.cubes;
    }
}
