package vazkii.psi.client.render.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/** Modern Psi projectiles are represented exclusively by their particle trail. */
public class RenderSpellProjectile extends Render {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        // Deliberately invisible: the emitted spell particles are the projectile visual.
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}
