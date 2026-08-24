/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.entity.player.EntityPlayer;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * Selector that returns the caster (the player who cast the spell).
 */
public class PieceSelectorCaster extends PieceSelector {

    public PieceSelectorCaster(Spell spell) {
        super(spell);
    }

    @Override
    public Class<?> getEvaluationType() {
        return EntityPlayer.class;
    }

    @Override
    public Object execute(SpellContext context) {
        return context.caster;
    }
}
