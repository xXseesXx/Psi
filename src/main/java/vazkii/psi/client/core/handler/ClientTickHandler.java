package vazkii.psi.client.core.handler;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Client clock used by animated colorizers. */
public class ClientTickHandler {

    public static int ticksInGame = 0;
    public static float partialTicks = 0F;
    public static float total = 0F;

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) partialTicks = event.renderTickTime;
        else total = ticksInGame + partialTicks;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Minecraft.getMinecraft()
            .isGamePaused()) {
            ticksInGame++;
            partialTicks = 0F;
            total = ticksInGame;
        }
    }
}
