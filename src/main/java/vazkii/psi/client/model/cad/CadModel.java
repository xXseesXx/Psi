package vazkii.psi.client.model.cad;

import java.util.List;
import java.util.Map;

/** Parsed, renderer-independent representation of Psi's CAD JSON model. */
public final class CadModel {
    final List<Element> elements;
    final Map<String, String> textures;
    final Map<String, Transform> transforms;
    final float textureWidth;
    final float textureHeight;

    CadModel(List<Element> elements, Map<String, String> textures, Map<String, Transform> transforms, float textureWidth, float textureHeight) {
        this.elements = elements; this.textures = textures; this.transforms = transforms;
        this.textureWidth = textureWidth; this.textureHeight = textureHeight;
    }
    static final class Element { final String name; final float[] from, to, origin; final String axis; final float angle; final Map<String, Face> faces;
        Element(String n,float[] f, float[] t, float[] o, String a, float an, Map<String, Face> fs) { name=n; from=f; to=t; origin=o; axis=a; angle=an; faces=fs; } }
    static final class Face { final float[] uv; final String texture, cullface; final int rotation;
        Face(float[] u, String t, int r, String c) { uv=u; texture=t; rotation=r; cullface=c; } }
    public static final class Transform { public final float[] rotation, translation, scale;
        Transform(float[] r, float[] t, float[] s) { rotation=r; translation=t; scale=s; } }
}
