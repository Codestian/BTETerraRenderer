package com.mndk.bteterrarenderer.tile;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

public class TileQuad<T extends TileQuad.VertexInfo> extends ArrayList<T> {

    public final int glId;

    public TileQuad(int glId) {
        super();
        this.glId = glId;
    }

    @RequiredArgsConstructor
    public static class PosTexColor extends VertexInfo {
        public final float x, y, z;
        public final float u, v, r, g, b, a;
    }

    public static class VertexInfo {}
}
