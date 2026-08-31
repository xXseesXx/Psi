package vazkii.psi.api.spell.piece;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.IErrorCatcher;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamAny;

/** Shared base for error handlers, matching the modern handler parameter contract. */
public abstract class PieceErrorHandler extends SpellPiece implements IErrorCatcher {
    protected SpellParam<SpellParam.Any> piece;
    public PieceErrorHandler(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
    }
    @Override public EnumPieceType getPieceType() { return EnumPieceType.ERROR_HANDLER; }
    @Override public void initParams() { addParam(piece = new ParamAny(paramName(), SpellParam.BROWN, false)); }
    @Override public void addToMetadata(SpellMetadata meta) throws SpellCompilationException { meta.addStat(EnumSpellStat.COMPLEXITY, 1); }
    @Override public Object evaluate() { return null; }
    @Override public Object execute(SpellContext context) { return null; }
    @Override public Class<?> getEvaluationType() { return Void.class; }
    @Override public boolean catchParam(SpellParam<?> param) { return param == piece; }
    protected abstract String paramName();
}
