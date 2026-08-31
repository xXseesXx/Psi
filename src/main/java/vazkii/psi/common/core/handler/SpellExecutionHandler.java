package vazkii.psi.common.core.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import vazkii.psi.api.spell.SpellContext;

/** Resumes contexts paused by Trick: Delay without recompiling their remaining actions. */
public final class SpellExecutionHandler {
    private static final List<SpellContext> DELAYED = new ArrayList<SpellContext>();

    public static void delayContext(SpellContext context) {
        if (context.delay > 0 && !DELAYED.contains(context)) DELAYED.add(context);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Iterator<SpellContext> iterator = DELAYED.iterator();
        while (iterator.hasNext()) {
            SpellContext context = iterator.next();
            if (context.caster == null || context.caster.isDead || context.cspell == null) {
                iterator.remove();
                continue;
            }
            if (--context.delay <= 0) {
                iterator.remove();
                context.delay = 0;
                context.cspell.safeExecute(context);
            }
        }
    }
}
