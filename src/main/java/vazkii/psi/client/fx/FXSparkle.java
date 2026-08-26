package vazkii.psi.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

/** Additive, full-bright spell particle matching Psi's modern sparkle. */
public class FXSparkle extends EntityFX {

    private static final ResourceLocation[] TEXTURES = {
        new ResourceLocation("psi", "textures/particle/sparkle_0.png"),
        new ResourceLocation("psi", "textures/particle/sparkle_1.png"),
        new ResourceLocation("psi", "textures/particle/sparkle_2.png"),
        new ResourceLocation("psi", "textures/particle/sparkle_3.png")
    };

    private final float sparkleScale;

    public FXSparkle(World world, double x, double y, double z, float size,
            float red, float green, float blue, int ageMultiplier, double mx, double my, double mz) {
        super(world, x, y, z);
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
        particleAlpha = 0.5F;
        particleGravity = 0F;
        motionX = mx;
        motionY = my;
        motionZ = mz;
        sparkleScale = particleScale * size;
        particleScale = sparkleScale;
        particleMaxAge = 3 * ageMultiplier;
        setSize(0.01F, 0.01F);
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
        motionX *= 0.9D;
        motionY *= 0.9D;
        motionZ *= 0.9D;
        if (onGround) {
            motionX *= 0.7D;
            motionZ *= 0.7D;
        }
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    @Override
    public void renderParticle(Tessellator ignored, float partialTicks, float rotationX, float rotationXZ,
            float rotationZ, float rotationYZ, float rotationXY) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURES[particleAge % TEXTURES.length]);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);
        renderQuad(partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY,
            sparkleScale * (particleMaxAge - particleAge + 1F) / particleMaxAge);
        GL11.glDepthMask(true);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    protected void renderQuad(float partialTicks, float rotationX, float rotationXZ, float rotationZ,
            float rotationYZ, float rotationXY, float scale) {
        float x = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float y = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float z = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);
        float quad = 0.1F * scale;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(0xF000F0);
        tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, particleAlpha);
        tessellator.addVertexWithUV(x - rotationX * quad - rotationYZ * quad, y - rotationXZ * quad, z - rotationZ * quad - rotationXY * quad, 1, 1);
        tessellator.addVertexWithUV(x - rotationX * quad + rotationYZ * quad, y + rotationXZ * quad, z - rotationZ * quad + rotationXY * quad, 1, 0);
        tessellator.addVertexWithUV(x + rotationX * quad + rotationYZ * quad, y + rotationXZ * quad, z + rotationZ * quad + rotationXY * quad, 0, 0);
        tessellator.addVertexWithUV(x + rotationX * quad - rotationYZ * quad, y - rotationXZ * quad, z + rotationZ * quad - rotationXY * quad, 0, 1);
        tessellator.draw();
    }
}
