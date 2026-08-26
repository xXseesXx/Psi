package vazkii.psi.client.model.cad;

import net.minecraft.util.ResourceLocation;

public final class CadModels {

    private static volatile CadBakedModel cad;
    private static volatile CadBakedModel iron;

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

    public static CadBakedModel iron() {
        CadBakedModel result = iron;
        if (result == null) {
            synchronized (CadModels.class) {
                if (iron == null) iron = CadModelBaker
                    .bake(CadModelLoader.load(new ResourceLocation("psi", "models/item/cad_iron.json")));
                result = iron;
            }
        }
        return result;
    }
}
