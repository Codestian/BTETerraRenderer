package com.mndk.bteterrarenderer.core.tile;

import com.mndk.bteterrarenderer.core.config.BTETerraRendererConfig;
import com.mndk.bteterrarenderer.core.graphics.baker.GraphicsModelTextureBaker;
import com.mndk.bteterrarenderer.core.projection.Projections;
import com.mndk.bteterrarenderer.mcconnector.graphics.GlGraphicsManager;

public class TileRenderer {

    public static void renderTiles(Object poseStack, double px, double py, double pz) {
        if(!BTETerraRendererConfig.HOLOGRAM.isDoRender()) return;

        BTETerraRendererConfig.GeneralConfig generalConfig = BTETerraRendererConfig.GENERAL;
        BTETerraRendererConfig.HologramConfig hologramConfig = BTETerraRendererConfig.HOLOGRAM;

        TileMapService<?> tms = BTETerraRendererConfig.getTileMapServiceWrapper().getItem();
        if(tms == null) return;
        if(Projections.getServerProjection() == null) return;

        double yDiff = hologramConfig.getFlatMapYAxis() - py;
        if(Math.abs(yDiff) >= hologramConfig.getYDiffLimit()) return;

        GlGraphicsManager.INSTANCE.glPushMatrix(poseStack);
        GlGraphicsManager.INSTANCE.glDisableCull();
        GlGraphicsManager.INSTANCE.glEnableBlend();
        GlGraphicsManager.INSTANCE.glSetAlphaBlendFunc();

        String tmsId = generalConfig.getMapServiceCategory() + "." + generalConfig.getMapServiceId();
        tms.render(poseStack, tmsId,
                px + hologramConfig.getXAlign(), py, pz + hologramConfig.getZAlign(),
                (float) hologramConfig.getOpacity());
        GraphicsModelTextureBaker.getInstance().cleanUp();

        GlGraphicsManager.INSTANCE.glDisableBlend();
        GlGraphicsManager.INSTANCE.glDefaultBlendFunc();
        GlGraphicsManager.INSTANCE.glEnableCull();
        GlGraphicsManager.INSTANCE.glPopMatrix(poseStack);
    }
}
