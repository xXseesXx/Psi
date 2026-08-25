package vazkii.psi.client.model.cad;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
/** Selects the JSON display transform for a Forge item render context. */
public final class CadRenderContext {
    public final String transform; public final float scaleCompensation;
    private CadRenderContext(String t,float s){transform=t;scaleCompensation=s;}
    public static CadRenderContext of(ItemRenderType type){switch(type){case INVENTORY:return new CadRenderContext("gui",1.75F);case ENTITY:return new CadRenderContext("ground",1.25F);case EQUIPPED:return new CadRenderContext("thirdperson_righthand",1.25F);case EQUIPPED_FIRST_PERSON:return new CadRenderContext("firstperson_righthand",1.25F);default:return new CadRenderContext("fixed",1F);}}
}
