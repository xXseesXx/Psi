package vazkii.psi.client.core.handler;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import vazkii.psi.client.render.entity.RenderSpellCircle;

/** Client-side tracking and rendering for the circle at a loopcaster's feet. */
public class LoopcastRenderHandler {

    private static final Map<Integer, Long> LOOPCASTERS = new HashMap<Integer, Long>();
    private static final Map<Integer, Long> FADING_LOOPCASTERS = new HashMap<Integer, Long>();

    public static void setLoopcasting(int entityId, boolean loopcasting) {
        long time = currentTime();
        if (loopcasting) {
            LOOPCASTERS.put(entityId, time);
            FADING_LOOPCASTERS.remove(entityId);
        } else if (LOOPCASTERS.remove(entityId) != null) {
            FADING_LOOPCASTERS.put(entityId, time);
        }
    }

    @SubscribeEvent
    public void renderPlayer(RenderLivingEvent.Post event) {
        if (!(event.entity instanceof EntityPlayer)) return;
        if (event.entity == Minecraft.getMinecraft().thePlayer && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
            return;
        renderCircle((EntityPlayer) event.entity, event.x, event.y + 0.15D, event.z);
    }

    @SubscribeEvent
    public void renderFirstPersonLoopcast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = minecraft.thePlayer;
        if (player == null || minecraft.gameSettings.thirdPersonView != 0) return;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks - RenderManager.renderPosX;
        // RenderWorldLast retains the first-person eye transform, unlike a player render event.
        // Convert the local player's origin back down from the camera to the feet.
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks - RenderManager.renderPosY
            - player.getEyeHeight() - 1.4D;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks - RenderManager.renderPosZ;
        renderCircle(player, x, y, z);
    }

    private void renderCircle(EntityPlayer player, double x, double y, double z) {
        int entityId = player.getEntityId();
        long time = currentTime();
        float multiplier;
        Long start = LOOPCASTERS.get(entityId);
        if (start != null) {
            multiplier = Math.min(5F, time - start) / 5F;
        } else {
            Long fadeStart = FADING_LOOPCASTERS.get(entityId);
            if (fadeStart == null) return;
            multiplier = Math.max(0F, 1F - (time - fadeStart) / 5F);
            if (multiplier <= 0F) {
                FADING_LOOPCASTERS.remove(entityId);
                return;
            }
        }
        RenderSpellCircle.renderCircle(x, y, z, player.ticksExisted, 0.75F * multiplier);
    }

    private static long currentTime() {
        return Minecraft.getMinecraft().theWorld == null ? 0L : Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }
}
