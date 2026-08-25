package vazkii.psi.client.model.cad;
import net.minecraft.util.ResourceLocation;
/** Assembly variant data, intentionally separate from baked geometry. */
public final class CadMaterial {
    public static final ResourceLocation ASSEMBLY_0=new ResourceLocation("psi","textures/items/cad_assembly_0.png");
    public static final ResourceLocation ASSEMBLY_1=new ResourceLocation("psi","textures/items/cad_assembly_1.png");
    public static final ResourceLocation COLOR=new ResourceLocation("psi","textures/items/cad_color.png");
    public final ResourceLocation shell; public final int tint;
    private CadMaterial(ResourceLocation shell,int tint){this.shell=shell;this.tint=tint;}
    public static CadMaterial forAssembly(String assembly){ if("cad_assembly_ebony_psimetal".equals(assembly))return new CadMaterial(ASSEMBLY_1,0x382653); if("cad_assembly_ivory_psimetal".equals(assembly))return new CadMaterial(ASSEMBLY_1,0xF1E2C7); if("cad_assembly_creative".equals(assembly)){float h=(System.currentTimeMillis()%5000L)/5000F;return new CadMaterial(ASSEMBLY_1,java.awt.Color.HSBtoRGB(h,.75F,1F)&0xFFFFFF);} if("cad_assembly_gold".equals(assembly))return new CadMaterial(ASSEMBLY_0,0xE8B543); if("cad_assembly_psimetal".equals(assembly))return new CadMaterial(ASSEMBLY_0,0x42C5C9); return new CadMaterial(ASSEMBLY_0,0xFFFFFF); }
}
