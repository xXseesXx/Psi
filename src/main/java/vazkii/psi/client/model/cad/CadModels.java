package vazkii.psi.client.model.cad;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

public final class CadModels {

    private static final Map<String, CadBakedModel> MODELS = new HashMap<String, CadBakedModel>();

    private CadModels() {}

    public static CadBakedModel cad() {
        return get("cad");
    }

    public static CadBakedModel iron() {
        return get("cad_iron");
    }

    public static CadBakedModel gold() {
        return get("cad_gold");
    }

    public static CadBakedModel psimetal() {
        return get("cad_psimetal");
    }

    public static CadBakedModel ebonyPsimental() {
        return get("cad_ebony_psimetal");
    }

    public static CadBakedModel ivoryPsimental() {
        return get("cad_ivory_psimetal");
    }

    public static CadBakedModel forAssembly(String assembly) {
        if ("cad_assembly_iron".equals(assembly)) return iron();
        if ("cad_assembly_gold".equals(assembly)) return gold();
        if ("cad_assembly_psimetal".equals(assembly)) return psimetal();
        if ("cad_assembly_ebony_psimetal".equals(assembly)) return ebonyPsimental();
        if ("cad_assembly_ivory_psimetal".equals(assembly)) return ivoryPsimental();

        return cad();
    }

    private static CadBakedModel get(String name) {
        synchronized (MODELS) {
            CadBakedModel model = MODELS.get(name);

            if (model == null) {
                model = CadModelBaker
                    .bake(CadModelLoader.load(new ResourceLocation("psi", "models/item/" + name + ".json")));
                MODELS.put(name, model);
            }

            return model;
        }
    }

    private static volatile CadBakedModel creative;

    public static CadBakedModel creative() {
        CadBakedModel result = creative;

        if (result == null) {
            synchronized (CadModels.class) {
                result = creative;

                if (result == null) {
                    result = CadModelBaker
                        .bake(CadModelLoader.load(new ResourceLocation("psi", "models/item/cad_creative.json")));

                    creative = result;
                }
            }
        }

        return result;
    }
}
