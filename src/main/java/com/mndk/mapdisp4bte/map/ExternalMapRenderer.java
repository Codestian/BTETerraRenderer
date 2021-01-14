package com.mndk.mapdisp4bte.map;

import copy.io.github.terra121.projection.OutOfProjectionBoundsException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ExternalMapRenderer {

    public static final Map<String, ResourceLocation> resourceLocations = new HashMap<>();
    private static final List<Map.Entry<String, BufferedImage>> renderList = new ArrayList<>();

    private final RenderMapSource source;
    private final ExecutorService donwloadExecutor;

    public ExternalMapRenderer(RenderMapSource source, int maximumDownloadThreads) {
        this.source = source;
        this.donwloadExecutor = Executors.newFixedThreadPool(maximumDownloadThreads);
    }



    public void initializeMapImageByPlayerCoordinate(
            double playerX, double playerZ,
            int tileDeltaX, int tileDeltaY,
            int zoom, RenderMapType type
    ) throws OutOfProjectionBoundsException {

        int[] tileCoord = this.playerPositionToTileCoord(playerX, playerZ, zoom);

        String tileID = genTileID(tileCoord[0]+tileDeltaX, tileCoord[1]+tileDeltaY, zoom, type, this.source);

        BufferedImage image = this.fetchMapSync(playerX, playerZ, tileDeltaX, tileDeltaY, zoom, type);

        renderList.add(new AbstractMap.SimpleEntry<>(tileID, image));
    }



    public abstract int[] playerPositionToTileCoord(double playerX, double playerZ, int zoom) throws OutOfProjectionBoundsException;

    public abstract double[] tileCoordToPlayerPosition(int tileX, int tileY, int zoom) throws OutOfProjectionBoundsException;

    /**
     * This should return: [tileDeltaX, tileDeltaY, u, v]
     */
    protected abstract int[] getCornerMatrix(int i);

    protected abstract int getZoomFromLevel(int level); // TODO do this



    public URLConnection getTileUrlConnection(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom, RenderMapType type) {
        try {
            int[] tilePos = this.playerPositionToTileCoord(playerX, playerZ, zoom);

            String url = this.getUrlTemplate(tilePos[0] + tileDeltaX, tilePos[1] + tileDeltaY, zoom, type);

            // System.out.println(url);

            return new URL(url).openConnection();
        }catch(OutOfProjectionBoundsException | IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public BufferedImage fetchMapSync(double playerX, double playerZ, int tileDeltaX, int tileDeltaY, int zoom, RenderMapType type) {
        try {
            URLConnection connection = this.getTileUrlConnection(playerX, playerZ, tileDeltaX, tileDeltaY, zoom, type);
            if(connection == null) return null;
            connection.connect();
            return ImageIO.read(connection.getInputStream());
        } catch(IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }



    public abstract String getUrlTemplate(int tileX, int tileY, int level, RenderMapType type);



    public void renderTile(
            Tessellator t, BufferBuilder builder,
            int level, RenderMapType type,
            double y, float opacity,
            double px, double py, double pz,
            int tileDeltaX, int tileDeltaY
    ) {
        try {
            int zoom = this.getZoomFromLevel(level);

            int[] tilePos = this.playerPositionToTileCoord(px, pz, zoom);

            String tileID = genTileID(tilePos[0]+tileDeltaX, tilePos[1]+tileDeltaY, zoom, type, source);

            if(!renderList.isEmpty()) {
                // Cannot set all images to resource locations at once, because it would cause ConcurrentModificationException.
                // So instead, it's setting one image by frame.

                // TODO The code is messy, so re-categorize this

                Map.Entry<String, BufferedImage> entry = renderList.get(0);
                renderList.remove(0);
                if(entry != null) if(entry.getValue() != null) {
                    DynamicTexture texture = new DynamicTexture(entry.getValue());
                    ResourceLocation reloc =
                            Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(tileID, texture);
                    resourceLocations.put(entry.getKey(), reloc);
                }
            }

            if(!resourceLocations.containsKey(tileID)) {
                // If the tile is not loaded, load it in new thread
                resourceLocations.put(tileID, null);
                this.donwloadExecutor.execute(() -> {
                    try {
                        initializeMapImageByPlayerCoordinate(px, pz, tileDeltaX, tileDeltaY, zoom, type);
                    } catch (OutOfProjectionBoundsException exception) {
                        exception.printStackTrace();
                    }
                });
            }
            else if(resourceLocations.get(tileID) != null) {
                ResourceLocation resourceLocation = resourceLocations.get(tileID);

                FMLClientHandler.instance().getClient().renderEngine.bindTexture(resourceLocation);

                // begin vertex
                builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

                double[] temp;

                // Convert boundaries
                for (int i = 0; i < 4; i++) {

                    int[] mat = this.getCornerMatrix(i);
                    temp = tileCoordToPlayerPosition(tilePos[0] + mat[0] + tileDeltaX, tilePos[1] + mat[1] + tileDeltaY, zoom);

                    builder.pos(temp[0] - px, y - py, temp[1] - pz)
                            .tex(mat[2], mat[3])
                            .color(1.f, 1.f, 1.f, opacity)
                            .endVertex();
                }

                t.draw();
            }

        } catch(OutOfProjectionBoundsException ignored) {}
    }



    public static String genTileID(int tileX, int tileY, int zoom, RenderMapType type, RenderMapSource source) {
        return "tilemap_" + source + "_" + tileX + "_" + tileY + "_" + zoom + "_" + type;
    }

}
