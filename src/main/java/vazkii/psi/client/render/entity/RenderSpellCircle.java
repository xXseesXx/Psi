package vazkii.psi.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vazkii.psi.common.entity.EntitySpellCircle;

/** Three rotating, additive layers used by a Psi circle. */
public class RenderSpellCircle extends Render {

    private static final ResourceLocation[] LAYERS = { new ResourceLocation("psi", "textures/misc/spell_circle0.png"),
        new ResourceLocation("psi", "textures/misc/spell_circle1.png"),
        new ResourceLocation("psi", "textures/misc/spell_circle2.png") };

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntitySpellCircle circle = (EntitySpellCircle) entity;
        float alive = circle.getAge() + partialTicks;
        float scale = Math.min(1F, alive / EntitySpellCircle.CAST_DELAY);
        float end = (EntitySpellCircle.CAST_TIMES + 2) * EntitySpellCircle.CAST_DELAY;
        if (alive > end - EntitySpellCircle.CAST_DELAY) {
            scale = Math.max(0F, 1F - (alive - (end - EntitySpellCircle.CAST_DELAY)) / EntitySpellCircle.CAST_DELAY);
        }

        renderCircle(x, y, z, alive, scale, circle.getSpellColor());
    }

    /** Renders the same circle around a loopcasting player. Coordinates are camera-relative. */
    public static void renderCircle(double x, double y, double z, float alive, float scale) {
        renderCircle(x, y, z, alive, scale, 0x13C5FF);
    }

    /** Renders a circle with its colorizer-derived spell colour. */
    public static void renderCircle(double x, double y, double z, float alive, float scale, int spellColor) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y + 0.02D, z);
        GL11.glRotatef(90F, 1F, 0F, 0F);
        GL11.glScalef(scale, scale, scale);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);

        for (int i = 0; i < LAYERS.length; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i == 0 ? -alive : alive, 0F, 0F, 1F);
            Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(LAYERS[i]);
            int color = i == 1 ? 0xFFFFFF : i == 2 ? brighten(spellColor) : spellColor;
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setBrightness(0xF000F0);
            tessellator
                .setColorRGBA_F(((color >> 16) & 255) / 255F, ((color >> 8) & 255) / 255F, (color & 255) / 255F, 1F);
            tessellator.addVertexWithUV(-2D, 2D, 0D, 0D, 1D);
            tessellator.addVertexWithUV(2D, 2D, 0D, 1D, 1D);
            tessellator.addVertexWithUV(2D, -2D, 0D, 1D, 0D);
            tessellator.addVertexWithUV(-2D, -2D, 0D, 0D, 0D);
            tessellator.draw();
            GL11.glPopMatrix();
            GL11.glTranslatef(0F, 0F, -0.01F);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private static int brighten(int color) {
        int r = Math.min(255, (int) (((color >> 16) & 255) / 0.7F));
        int g = Math.min(255, (int) (((color >> 8) & 255) / 0.7F));
        int b = Math.min(255, (int) ((color & 255) / 0.7F));
        return r << 16 | g << 8 | b;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return LAYERS[0];
    }
}
