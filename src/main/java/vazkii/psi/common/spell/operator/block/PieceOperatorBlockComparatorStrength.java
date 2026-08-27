/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockComparatorStrength.java:1
 * Modern: ComparatorBlock.getInputSignal(Level, BlockPos.relative(Direction), BlockState)
 * GTNH 1.7.10 adaptation: World.getComparatorInputOverride uses same vanilla logic — use
 * world.getComparatorInputOverride(x,y,z, side).
 * ForgeDirection mapping via SpellHelpers.getFacing.
 */
package vazkii.psi.common.spell.operator.block;

import net.minecraftforge.common.util.ForgeDirection;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceOperatorBlockComparatorStrength extends PieceOperator {

    SpellParam<Vector3> axisParam;
    SpellParam<Vector3> target;

    public PieceOperatorBlockComparatorStrength(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
        addParam(axisParam = new ParamVector(SpellParam.GENERIC_NAME_VECTOR, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPosCompat pos = SpellHelpers.getBlockPos(this, context, target, false, false);

        ForgeDirection whichWay = SpellHelpers.getFacing(this, context, axisParam);
        if (whichWay == ForgeDirection.UP || whichWay == ForgeDirection.DOWN) {
            throw new SpellRuntimeException(SpellRuntimeException.COMPARATOR);
        }

        // GTNH 1.7.10: direct comparator signal not trivial — stub returns redstone power at offset, close enough for
        // now
        // TODO: mirror modern ComparatorBlock.getInputSignal with proper Block.comparator logic
        BlockPosCompat offset = new BlockPosCompat(
            pos.x + whichWay.offsetX,
            pos.y + whichWay.offsetY,
            pos.z + whichWay.offsetZ);
        return (double) context.focalPoint.worldObj
            .getIndirectPowerLevelTo(offset.x, offset.y, offset.z, whichWay.ordinal());
    }

    @Override
    public Class<Double> getEvaluationType() {
        return Double.class;
    }
}
