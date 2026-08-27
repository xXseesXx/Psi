package vazkii.psi.client.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import vazkii.psi.api.cad.ICAD;
import vazkii.psi.client.model.cad.CadBakedModel;
import vazkii.psi.client.model.cad.CadMaterial;
import vazkii.psi.client.model.cad.CadModels;
import vazkii.psi.client.model.cad.CadRenderContext;
import vazkii.psi.client.model.cad.CadRenderer;
import vazkii.psi.common.item.ItemCAD;

/**
 * Thin Forge adapter for the cached, baked CAD model pipeline.
 */
public class RenderItemCAD implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        if (type == ItemRenderType.ENTITY) {
            return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
        }

        return helper == ItemRendererHelper.EQUIPPED_BLOCK || helper == ItemRendererHelper.INVENTORY_BLOCK
            || helper == ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        String assembly = ItemCAD.getAssemblyId(item);

        CadBakedModel model;

        if ("cad_assembly_iron".equals(assembly)) {
            model = CadModels.iron();
        } else if ("cad_assembly_gold".equals(assembly)) {
            model = CadModels.gold();
        } else if ("cad_assembly_psimetal".equals(assembly)) {
            model = CadModels.psimetal();
        } else if ("cad_assembly_ebony_psimetal".equals(assembly)) {
            model = CadModels.ebonyPsimental();
        } else if ("cad_assembly_ivory_psimetal".equals(assembly)) {
            model = CadModels.ivoryPsimental();
        } else if ("cad_assembly_creative".equals(assembly)) {
            model = CadModels.creative();
        } else {
            model = CadModels.cad();
        }

        // item.getItem() is always an ICAD here - this renderer is only ever registered for CAD items.
        ICAD cad = (ICAD) item.getItem();

        CadRenderer.render(model, CadMaterial.forAssembly(assembly), CadRenderContext.of(type), cad.getSpellColor(item));
    }
}
