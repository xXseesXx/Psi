/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorCasterBattery.java:10
 * GTNH: ICAD getStatValue with EnumCADStat, PsiAPI.getPlayerCAD null check.
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorCasterBattery extends PieceSelector {

    public PieceSelectorCasterBattery(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) {
        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        if (cad != null && cad.getItem() instanceof ICAD) {
            return (double) ((ICAD) cad.getItem()).getStatValue(cad, EnumCADStat.OVERFLOW);
        }
        return 0.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
