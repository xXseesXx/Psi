package vazkii.psi.client.model.cad;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

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

    public boolean hasLayer(String layer) {
        for (Quad quad : quads) {
            if (layer.equals(quad.layer)) return true;
        }
        return false;
    }

    private static volatile CadBakedModel creative;

    public static CadBakedModel creative() {
        CadBakedModel result = creative;
        if (result == null) {
            synchronized (CadModels.class) {
                if (creative == null) {
                    creative = CadModelBaker
                        .bake(CadModelLoader.load(new ResourceLocation("psi", "models/item/cad_creative.json")));
                }
                result = creative;
            }
        }
        return result;
    }

}
