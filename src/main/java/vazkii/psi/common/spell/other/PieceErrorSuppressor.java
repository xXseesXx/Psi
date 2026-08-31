package vazkii.psi.common.spell.other;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellPiece;

/** Modifier that suppresses uncaught runtime error reporting. */
public class PieceErrorSuppressor extends SpellPiece {
    public PieceErrorSuppressor(Spell spell) { super(spell); }
    @Override public void addToMetadata(SpellMetadata meta) { meta.errorsSuppressed = true; }
    @Override public EnumPieceType getPieceType() { return EnumPieceType.MODIFIER; }
    @Override public Class<?> getEvaluationType() { return Void.class; }
    @Override public Object evaluate() { return null; }
    @Override public Object execute(SpellContext context) { return null; }
}
