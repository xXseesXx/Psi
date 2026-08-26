package vazkii.psi.common.core.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import vazkii.psi.common.item.ItemCAD;
import vazkii.psi.common.item.ItemCreativeCAD;
import vazkii.psi.common.item.ItemLoopcastSpellBullet;

/** Server-side 4 Hz loopcast scheduler. */
public class LoopcastHandler {

    private static final Map<UUID, Loopcast> ACTIVE = new HashMap<UUID, Loopcast>();

    public static void start(EntityPlayer player, ItemStack bullet) {
        ACTIVE.put(player.getUniqueID(), new Loopcast(bullet.copy()));
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        Loopcast cast = ACTIVE.get(event.player.getUniqueID());
        if (cast == null) return;
        ItemStack held = event.player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemCAD || held.getItem() instanceof ItemCreativeCAD)) {
            ACTIVE.remove(event.player.getUniqueID());
            return;
        }
        ItemStack selected = held.getItem() instanceof ItemCAD ? ItemCAD.getBullet(held, ItemCAD.getSelectedSlot(held))
            : ItemCreativeCAD.getBullet(held, ItemCreativeCAD.getSelectedSlot(held));
        if (selected == null || !ItemStack.areItemStacksEqual(selected, cast.bullet)) {
            ACTIVE.remove(event.player.getUniqueID());
            return;
        }
        if (++cast.ticks % 5 != 0) return;
        try {
            if (!new ItemLoopcastSpellBullet().castSpellNow(cast.bullet, event.player, cast.iterations++))
                ACTIVE.remove(event.player.getUniqueID());
        } catch (Exception ignored) {
            ACTIVE.remove(event.player.getUniqueID());
        }
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
