package vazkii.psi.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.common.core.handler.LoopcastHandler;
import vazkii.psi.common.core.handler.PlayerPsiHandler;

public class ItemLoopcastSpellBullet extends ItemSpellBullet {

    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster) {
        try {
            if (!castSpellNow(stack, caster, 0)) return;
        } catch (Exception ignored) {
            return;
        }
        LoopcastHandler.start(caster, stack);
    }

    /**
     * Loopcasts have no spawned visual entity to carry a colorizer. The
     * loopcast renderer reads the held CAD directly, so retain the normal
     * scheduler path when ItemCAD supplies its installed colorizer.
     */
    @Override
    public void castSpell(ItemStack stack, EntityPlayer caster, ItemStack colorizer) {
        castSpell(stack, caster);
    }

    public boolean castSpellNow(ItemStack stack, EntityPlayer caster, int index) throws Exception {
        return castSpellNow(stack, caster, index, true);
    }

    public boolean castSpellNow(ItemStack stack, EntityPlayer caster, int index, boolean applyCooldown)
        throws Exception {
        Spell spell = getSpell(stack);
        if (spell == null) return false;
        CompiledSpell compiled = new SpellCompiler().compile(spell);
        ItemStack cad = caster.getHeldItem();
        if (cad != null && cad.getItem() instanceof ItemCAD
            && !PlayerPsiHandler.spend(caster, ItemCAD.getRealCost(cad, stack, spell), cad, applyCooldown))
            return false;
        compiled.safeExecute(new SpellContext().setPlayer(caster)
            .setCompiledSpell(compiled).setLoopcastIndex(index));
        return true;
    }

    @Override
    public String getBulletType() {
        return "loopcast";
    }

    @Override
    public boolean isCADOnlyContainer(ItemStack stack) {
        return true;
    }
}
