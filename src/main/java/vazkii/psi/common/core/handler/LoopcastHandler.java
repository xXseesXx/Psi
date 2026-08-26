package vazkii.psi.common.core.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;
import vazkii.psi.common.network.PacketHandler;
import vazkii.psi.common.network.PacketLoopcastSync;

/** Server-side 4 Hz loopcast scheduler. */
public class LoopcastHandler {

    private static final Map<UUID, Loopcast> ACTIVE = new HashMap<UUID, Loopcast>();

    public static void start(EntityPlayer player, ItemStack bullet) {
        ACTIVE.put(player.getUniqueID(), new Loopcast(bullet.copy()));
        sync(player, true);
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        Loopcast cast = ACTIVE.get(event.player.getUniqueID());
        if (cast == null) return;
        ItemStack held = event.player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemCAD || held.getItem() instanceof ItemCreativeCAD)) {
            stop(event.player);
            return;
        }
        ItemStack selected = held.getItem() instanceof ItemCAD ? ItemCAD.getBullet(held, ItemCAD.getSelectedSlot(held))
            : ItemCreativeCAD.getBullet(held, ItemCreativeCAD.getSelectedSlot(held));
        if (selected == null || !ItemStack.areItemStacksEqual(selected, cast.bullet)) {
            stop(event.player);
            return;
        }
        if (++cast.ticks % 5 != 0) return;
        try {
            if (!new ItemLoopcastSpellBullet().castSpellNow(cast.bullet, event.player, cast.iterations++, false))
                stop(event.player);
        } catch (Exception ignored) {
            stop(event.player);
        }
    }

    private static void stop(EntityPlayer player) {
        if (ACTIVE.remove(player.getUniqueID()) != null) sync(player, false);
    }

    private static void sync(EntityPlayer player, boolean loopcasting) {
        if (PacketHandler.INSTANCE == null) return;
        PacketHandler.INSTANCE.sendToAllAround(
            new PacketLoopcastSync(player.getEntityId(), loopcasting),
            new NetworkRegistry.TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 64D));
    }

    private static class Loopcast {

        final ItemStack bullet;
        int ticks;
        int iterations = 1;

        Loopcast(ItemStack bullet) {
            this.bullet = bullet;
        }
    }
}
