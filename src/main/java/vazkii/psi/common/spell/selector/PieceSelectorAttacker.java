/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorAttacker.java:1
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.util.FakePlayer;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.piece.PieceSelector;

public class PieceSelectorAttacker extends PieceSelector {

    public PieceSelectorAttacker(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityLivingBase.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        if (context.attackingEntity == null || context.attackingEntity instanceof FakePlayer) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        return context.attackingEntity;
    }

}
