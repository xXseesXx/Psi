package vazkii.psi.client.model.cad;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class CadRenderer {

    private static final FloatBuffer CURRENT_COLOR = BufferUtils.createFloatBuffer(16);

    private CadRenderer() {}

    public static void render(CadBakedModel model, CadMaterial material, CadRenderContext context) {
        render(model, material, context, material.tint);
    }

    public static void render(CadBakedModel model, CadMaterial material, CadRenderContext context, int spellColor) {
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        CURRENT_COLOR.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, CURRENT_COLOR);

        GL11.glPushMatrix();

        try {
            if (!textureEnabled) {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }

            apply(model.transforms.get(context.transform));

            if ("gui".equals(context.transform)) {
                GL11.glRotatef(45F, 0F, 1F, 0F);
                GL11.glRotatef(-35F, 0F, 0F, 1F);
                GL11.glRotatef(-5F, 0F, 1F, 0F);

                GL11.glTranslatef(-1F, -0.35F, 0.0F);
                GL11.glScalef(0.9F, 0.9F, 0.9F);

            } else if ("thirdperson_righthand".equals(context.transform)) {
                GL11.glRotatef(45F, 0F, 1F, 0F);
                GL11.glRotatef(-70F, 1F, 0F, 0F);
                GL11.glTranslatef(-0.8F, -2.1F, 0.38F);
                GL11.glScalef(2F, 2F, 2F);

            } else if ("firstperson_righthand".equals(context.transform)) {
                GL11.glRotatef(-45F, 0F, 1F, 0F);
                GL11.glTranslatef(-0.13F, 1.1F, 1.1F);
                GL11.glScalef(0.9F, 0.9F, 0.9F);

            } else {
                GL11.glRotatef(90F, 0F, -1F, 0F);
                GL11.glRotatef(90F, -1F, 0F, 0F);
                GL11.glScalef(2F, 2F, 2F);
                GL11.glTranslatef(-0.2F, -0.51F, -0.1F);
            }

            GL11.glTranslatef(-0.28F, -0.25F, -0.50F);
            GL11.glScalef(context.scaleCompensation, context.scaleCompensation, context.scaleCompensation);

            renderLayer(model, material.shell, "layer0", 0xFFFFFF, 1F, 1F, 0);
            renderLayer(model, material.shell, "-1", 0xFFFFFF, 1F, 1F, 0);
            renderLayer(model, CadMaterial.COLOR, "layer1", spellColor & 0xFFFFFF, 1F, 1F, 0);

            renderLayer(model, material.shell, "2", 0xFFFFFF, 1F, 1F, 0);
            renderLayer(model, material.shell, "3", 0xFFFFFF, 1F, 1F, 0);
            renderLayer(model, CadMaterial.COLOR, "1", spellColor & 0xFFFFFF, 1F, 1F, 0);

            if (model == CadModels.creative()) {
                renderLayer(
                    model,
                    new ResourceLocation("psi", "textures/items/cad_creative_gizmo.png"),
                    "4",
                    0xFFFFFF,
                    1F,
                    0.25F,
                    getCreativeGizmoFrame());
            }

        } finally {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundTexture);

            if (!textureEnabled) {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }

            GL11.glColor4f(CURRENT_COLOR.get(0), CURRENT_COLOR.get(1), CURRENT_COLOR.get(2), CURRENT_COLOR.get(3));

            GL11.glPopMatrix();
        }
    }

    private static int getCreativeGizmoFrame() {
        long ticks = Minecraft.getSystemTime() / 50L;
        return (int) ((ticks / 10L) % 4L);
    }

    private static void apply(CadModel.Transform t) {
        if (t == null) {
            return;
        }

        GL11.glTranslatef(t.translation[0] / 16F, t.translation[1] / 16F, t.translation[2] / 16F);

        GL11.glRotatef(t.rotation[0], 1, 0, 0);
        GL11.glRotatef(t.rotation[1], 0, 1, 0);
        GL11.glRotatef(t.rotation[2], 0, 0, 1);

        GL11.glScalef(t.scale[0], t.scale[1], t.scale[2]);
    }

    private static void renderLayer(CadBakedModel model, ResourceLocation texture, String layer, int tint, float uScale,
        float vScale, int frame) {
        if (!model.hasLayer(layer)) {
            return;
        }

        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(texture);

        Tessellator t = Tessellator.instance;

        t.startDrawingQuads();
        t.setColorOpaque_I(tint);

        float frameOffset = frame * 0.25F;

        for (CadBakedModel.Quad q : model.quads) {
            if (!layer.equals(q.layer)) {
                continue;
            }

            t.setNormal(q.normal[0], q.normal[1], q.normal[2]);

            for (int i = 0; i < 4; i++) {
                float u = q.uv[i * 2] * uScale;
                float v = q.uv[i * 2 + 1] * vScale;

                if (frame > 0) {
                    v += frameOffset;
                }

                t.addVertexWithUV(q.v[i * 3], q.v[i * 3 + 1], q.v[i * 3 + 2], u, v);
            }
        }

        t.draw();
    }

}
