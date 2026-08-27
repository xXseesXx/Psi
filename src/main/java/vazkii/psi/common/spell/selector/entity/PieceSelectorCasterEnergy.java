/*
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/entity/PieceSelectorCasterEnergy.java:10
 */
package vazkii.psi.common.spell.selector.entity;

import net.minecraft.item.ItemStack;

public class PieceSelectorCasterEnergy extends PieceSelector {

    public PieceSelectorCasterEnergy(Spell spell) {
        super(spell);
    }

    @Override
    public Object execute(SpellContext context) {
        ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
        if (cad != null && cad.getItem() instanceof ICAD) {
            ICAD icad = (ICAD) cad.getItem();
            return (double) icad.getStoredPsi(cad);
        }
        return 0.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
