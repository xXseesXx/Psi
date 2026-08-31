package vazkii.psi.common.spell.other;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.IRedirector;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.param.ParamAny;

/** The normal one-input connector used by redirect-aware compilation and reads. */
public class PieceConnector extends SpellPiece implements IRedirector {
    public SpellParam<SpellParam.Any> target;
    public PieceConnector(Spell spell) { super(spell); }
    @Override public String getSortingName() { return "00000000000"; }
    @Override public String getEvaluationTypeString() { return net.minecraft.client.resources.I18n.format("psi.datatype.any"); }
    @Override public void initParams() { addParam(target = new ParamAny(SpellParam.GENERIC_NAME_TARGET, SpellParam.GRAY, false)); }
    @Override public EnumPieceType getPieceType() { return EnumPieceType.CONNECTOR; }
    @Override public SpellParam.Side getRedirectionSide() { return paramSides.get(target); }
    @Override public Class<?> getEvaluationType() { return null; }
    @Override public Object evaluate() { return null; }
    @Override public Object execute(SpellContext context) { return null; }
}
