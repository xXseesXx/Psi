package vazkii.psi.client.model.cad;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

/** The only CAD-model class that touches the 1.7.10 OpenGL/Tessellator backend. */
public final class CadRenderer {
    // LWJGL's generic glGetFloat validation requires room for a 4x4 matrix even
    // when querying GL_CURRENT_COLOR. Reuse this client-thread buffer per render.
    private static final FloatBuffer CURRENT_COLOR = BufferUtils.createFloatBuffer(16);
    private CadRenderer() {}
    public static void render(CadBakedModel model,CadMaterial material,CadRenderContext context){
        boolean texture=GL11.glIsEnabled(GL11.GL_TEXTURE_2D); int bound=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D); CURRENT_COLOR.clear(); GL11.glGetFloat(GL11.GL_CURRENT_COLOR,CURRENT_COLOR);
        GL11.glPushMatrix();
        try { if(!texture)GL11.glEnable(GL11.GL_TEXTURE_2D); apply(model.transforms.get(context.transform)); GL11.glTranslatef(-0.28F,-0.25F,-0.50F); GL11.glScalef(context.scaleCompensation,context.scaleCompensation,context.scaleCompensation); renderLayer(model,material,"layer0",material.shell,0xFFFFFF); renderLayer(model,material,"-1",material.shell,0xFFFFFF); renderLayer(model,material,"layer1",CadMaterial.COLOR,material.tint); }
        finally { GL11.glBindTexture(GL11.GL_TEXTURE_2D,bound); if(!texture)GL11.glDisable(GL11.GL_TEXTURE_2D); GL11.glColor4f(CURRENT_COLOR.get(0),CURRENT_COLOR.get(1),CURRENT_COLOR.get(2),CURRENT_COLOR.get(3)); GL11.glPopMatrix(); }
    }
    private static void apply(CadModel.Transform t){if(t==null)return;GL11.glTranslatef(t.translation[0]/16F,t.translation[1]/16F,t.translation[2]/16F);GL11.glRotatef(t.rotation[0],1,0,0);GL11.glRotatef(t.rotation[1],0,1,0);GL11.glRotatef(t.rotation[2],0,0,1);GL11.glScalef(t.scale[0],t.scale[1],t.scale[2]);}
    private static void renderLayer(CadBakedModel model,CadMaterial material,String layer,net.minecraft.util.ResourceLocation texture,int colour){
        boolean found=false;for(CadBakedModel.Quad q:model.quads)if(layer.equals(q.layer)){found=true;break;}if(!found)return; Minecraft.getMinecraft().getTextureManager().bindTexture(texture); Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setColorOpaque_I(colour);
        for(CadBakedModel.Quad q:model.quads)if(layer.equals(q.layer)){t.setNormal(q.normal[0],q.normal[1],q.normal[2]);for(int i=0;i<4;i++)t.addVertexWithUV(q.v[i*3],q.v[i*3+1],q.v[i*3+2],q.uv[i*2],q.uv[i*2+1]);}t.draw();
    }
}
