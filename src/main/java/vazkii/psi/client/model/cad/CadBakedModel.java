package vazkii.psi.client.model.cad;

import java.util.*;

/** Immutable, render-ready CAD geometry. */
public final class CadBakedModel {

    public final List<Quad> quads;
    public final Map<String, CadModel.Transform> transforms;

    CadBakedModel(List<Quad> q, Map<String, CadModel.Transform> t) {
        quads = Collections.unmodifiableList(q);
        transforms = Collections.unmodifiableMap(t);
    }

    public static final class Quad {

        public final String layer;
        public final float[] v, uv, normal;

        Quad(String l, float[] p, float[] u, float[] n) {
            layer = l;
            v = p;
            uv = u;
            normal = n;
        }
    }
}
