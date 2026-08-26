package vazkii.psi.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

/** Full-bright wisp with the grow/fade timing used by current Psi. */
public class FXWisp extends FXSparkle {

    private static final ResourceLocation TEXTURE = new ResourceLocation("psi", "textures/particle/wisp.png");
    private final float wispScale;
    private final int halfLife;

    public FXWisp(World world, double x, double y, double z, double mx, double my, double mz,
            float size, float red, float green, float blue, float maxAgeMultiplier) {
        super(world, x, y, z, size, red, green, blue, 1, mx, my, mz);
        particleAlpha = 0.375F;
        wispScale = (rand.nextFloat() * 0.5F + 0.5F) * 2F * size;
        particleScale = wispScale;
        particleMaxAge = (int) (28D / (Math.random() * 0.3D + 0.7D) * maxAgeMultiplier);
        halfLife = particleMaxAge / 2;
    }

    @Override
    public void onUpdate() {
        if (particleAge++ >= particleMaxAge) {
            setDead();
            return;
        }
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        moveEntity(motionX, motionY, motionZ);
        motionX *= 0.9800000190734863D;
        motionY *= 0.9800000190734863D;
        motionZ *= 0.9800000190734863D;
    }

    @Override
    public void renderParticle(Tessellator ignored, float partialTicks, float rotationX, float rotationXZ,
            float rotationZ, float rotationYZ, float rotationXY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);
        float ageScale = (float) particleAge / halfLife;
        if (ageScale > 1F) ageScale = 2F - ageScale;
        renderQuad(partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY, wispScale * ageScale * 0.5F);
        GL11.glDepthMask(true);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
}
