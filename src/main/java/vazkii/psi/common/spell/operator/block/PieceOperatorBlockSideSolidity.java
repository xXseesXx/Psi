/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockSideSolidity.java:1
 * Modern: BlockState.isFaceSturdy(Level, BlockPos, Direction). GTNH: World.isSideSolid(x,y,z, ForgeDirection)
 */
package vazkii.psi.common.spell.operator.block;

import net.minecraftforge.common.util.ForgeDirection;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceOperatorBlockSideSolidity extends PieceOperator {

    SpellParam<Vector3> axisParam;
    SpellParam<Vector3> target;

    public PieceOperatorBlockSideSolidity(Spell spell) {
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
        ForgeDirection facing = SpellHelpers.getFacing(this, context, axisParam);
        return context.focalPoint.worldObj.isSideSolid(pos.x, pos.y, pos.z, facing) ? 1.0D : 0.D;
    }

    @Override
    public Class<Double> getEvaluationType() {
        return Double.class;
    }
}
