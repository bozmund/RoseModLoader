package rose.era.v1_20_1.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;

/**
 * Vertices an old renderer wrote, kept until 26.3 draws the frame. Positions and normals were already transformed by
 * the renderer's pose stack (as 1.20.1 drew them), so {@link #replay} writes them unchanged.
 */
final class RecordedVertices implements VertexConsumer {
    private static final int COLOR = 1, UV = 2, UV1 = 4, UV2 = 8, UV3 = 16, NORMAL = 32, LINE_WIDTH = 64;

    private static final class Vertex {
        float x, y, z;
        int set;
        int r, g, b, a;
        float u, v, u3, v3;
        int u1, v1, u2, v2;
        float nx, ny, nz;
        float lineWidth;
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private Vertex current;

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        current = new Vertex();
        current.x = x;
        current.y = y;
        current.z = z;
        vertices.add(current);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        current.r = r;
        current.g = g;
        current.b = b;
        current.a = a;
        current.set |= COLOR;
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        return setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        current.u = u;
        current.v = v;
        current.set |= UV;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        current.u1 = u;
        current.v1 = v;
        current.set |= UV1;
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        current.u2 = u;
        current.v2 = v;
        current.set |= UV2;
        return this;
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        current.u3 = u;
        current.v3 = v;
        current.set |= UV3;
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        current.nx = x;
        current.ny = y;
        current.nz = z;
        current.set |= NORMAL;
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        current.lineWidth = width;
        current.set |= LINE_WIDTH;
        return this;
    }

    void replay(VertexConsumer out) {
        for (Vertex v : vertices) {
            out.addVertex(v.x, v.y, v.z);
            if ((v.set & COLOR) != 0) out.setColor(v.r, v.g, v.b, v.a);
            if ((v.set & UV) != 0) out.setUv(v.u, v.v);
            if ((v.set & UV1) != 0) out.setUv1(v.u1, v.v1);
            if ((v.set & UV2) != 0) out.setUv2(v.u2, v.v2);
            if ((v.set & UV3) != 0) out.setUv3(v.u3, v.v3);
            if ((v.set & NORMAL) != 0) out.setNormal(v.nx, v.ny, v.nz);
            if ((v.set & LINE_WIDTH) != 0) out.setLineWidth(v.lineWidth);
        }
    }
}
