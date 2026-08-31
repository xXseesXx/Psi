package vazkii.psi.common.spell.other;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.IGenericRedirector;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellParam.ArrowType;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamAny;

/** Two independent redirect paths crossing in one grid square. */
public class PieceCrossConnector extends SpellPiece implements IGenericRedirector {
    private SpellParam<SpellParam.Any> in1, out1, in2, out2;
    public PieceCrossConnector(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.COMPLEXITY, new StatLabel(1));
    }
    @Override public void initParams() {
        addParam(in1 = new ParamAny(SpellParam.CONNECTOR_NAME_FROM1, 0xA0A0A0, false));
        addParam(out1 = new ParamAny(SpellParam.CONNECTOR_NAME_TO1, 0xA0A0A0, false, ArrowType.NONE));
        addParam(in2 = new ParamAny(SpellParam.CONNECTOR_NAME_FROM2, 0xA040FF, false));
        addParam(out2 = new ParamAny(SpellParam.CONNECTOR_NAME_TO2, 0xA040FF, false, ArrowType.NONE));
    }
    @Override public void addToMetadata(SpellMetadata meta) throws SpellCompilationException { meta.addStat(EnumSpellStat.COMPLEXITY, 1); }
    @Override public EnumPieceType getPieceType() { return EnumPieceType.CONNECTOR; }
    @Override public boolean isInputSide(SpellParam.Side side) {
        return paramSides.get(in1) == side || paramSides.get(in2) == side;
    }
    @Override public String getSortingName() { return "00000000000"; }
    @Override public String getEvaluationTypeString() { return net.minecraft.client.resources.I18n.format("psi.datatype.any"); }
    public SpellParam.Side[] getLineSides() {
        return new SpellParam.Side[] { paramSides.get(in1), paramSides.get(out1), paramSides.get(in2), paramSides.get(out2) };
    }
    public int[] getLineColors() { return new int[] { 0xA0A0A0, 0xA0A0A0, 0xA040FF, 0xA040FF }; }
    @Override public SpellParam.Side remapSide(SpellParam.Side inputSide) {
        if (paramSides.get(out1).getOpposite() == inputSide) return paramSides.get(in1);
        if (paramSides.get(out2).getOpposite() == inputSide) return paramSides.get(in2);
        return SpellParam.Side.OFF;
    }
    @Override public Class<?> getEvaluationType() { return null; }
    @Override public Object evaluate() { return null; }
    @Override public Object execute(SpellContext context) { return null; }
}
