package vazkii.psi.client.model.cad;

import net.minecraft.util.ResourceLocation;

/** Thread-safe lazy cache for the CAD's immutable baked model. */
public final class CadModels {

    private static volatile CadBakedModel cad;

    private CadModels() {}

    public static CadBakedModel cad() {
        CadBakedModel result = cad;
        if (result == null) {
            synchronized (CadModels.class) {
                if (cad == null)
                    cad = CadModelBaker.bake(CadModelLoader.load(new ResourceLocation("psi", "models/item/cad.json")));
                result = cad;
            }
        }
        return result;
    }
}
