package vazkii.psi.client.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import vazkii.psi.client.model.cad.*;
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
        CadRenderer.render(CadModels.cad(), CadMaterial.forAssembly(ItemCAD.getAssemblyId(item)), CadRenderContext.of(type));
    }
}
