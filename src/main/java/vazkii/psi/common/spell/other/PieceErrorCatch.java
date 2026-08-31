package vazkii.psi.common.spell.other;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.piece.PieceErrorHandler;

/** Replaces the failed handled value with its fallback input. */
public class PieceErrorCatch extends PieceErrorHandler {
    SpellParam<SpellParam.Any> fallback;
    public PieceErrorCatch(Spell spell) { super(spell); }
    @Override public void initParams() {
        super.initParams();
        addParam(fallback = new ParamAny(SpellParam.PSI_PREFIX + "fallback", SpellParam.GRAY, false) {
            @Override public boolean canAccept(SpellPiece other) {
                try {
                    SpellPiece actual = spell.grid.getPieceAtSideWithRedirections(x, y, paramSides.get(piece));
                    return super.canAccept(other) && actual.getEvaluationType().isAssignableFrom(other.getEvaluationType());
                } catch (SpellCompilationException e) { return super.canAccept(other); }
            }
        });
    }
    @Override protected String paramName() { return SpellParam.GENERIC_NAME_TARGET; }
    @Override public boolean catchException(SpellPiece errorPiece, SpellContext context, SpellRuntimeException exception) {
        try { return errorPiece == spell.grid.getPieceAtSideWithRedirections(x, y, paramSides.get(piece)); }
        catch (SpellCompilationException e) { return false; }
    }
    @Override public Object supplyReplacementValue(SpellPiece errorPiece, SpellContext context, SpellRuntimeException exception) {
        return getRawParamValue(context, fallback);
    }
}
