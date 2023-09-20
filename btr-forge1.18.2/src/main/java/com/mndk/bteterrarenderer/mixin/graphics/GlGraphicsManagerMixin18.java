package com.mndk.bteterrarenderer.mixin.graphics;

import com.mndk.bteterrarenderer.core.graphics.GlGraphicsManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@UtilityClass
@Mixin(value = GlGraphicsManager.class, remap = false)
public class GlGraphicsManagerMixin18 {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glTranslate(Object poseStack, float x, float y, float z) {
        ((PoseStack) poseStack).translate(x, y, z);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPushMatrix(Object poseStack) {
        ((PoseStack) poseStack).pushPose();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glPopMatrix(Object poseStack) {
        ((PoseStack) poseStack).popPose();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public void glDisableScissorTest() {
        RenderSystem.disableScissor();
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean glEnableRelativeScissor(Object poseStack, int x, int y, int width, int height) {
        Window window = Minecraft.getInstance().getWindow();
        if(window.getScreenWidth() == 0 || window.getScreenHeight() == 0) return false; // Division by zero handling
        float scaleFactorX = (float) window.getScreenWidth() / window.getGuiScaledWidth();
        float scaleFactorY = (float) window.getScreenHeight() / window.getGuiScaledHeight();

        Matrix4f matrix = ((PoseStack) poseStack).last().pose();
        Vector4f start = new Vector4f(x, y, 0, 1);
        Vector4f end = new Vector4f(x+width, y+height, 0, 1);
        start.transform(matrix);
        end.transform(matrix);

        RenderSystem.enableScissor(
                (int) (scaleFactorX * Math.min(start.x(), end.x())),
                (int) (window.getScreenHeight() - scaleFactorY * Math.max(start.y(), end.y())),
                (int) (scaleFactorX * Math.abs(start.x() - end.x())),
                (int) (scaleFactorY * Math.abs(start.y() - end.y()))
        );
        return true;
    }

}