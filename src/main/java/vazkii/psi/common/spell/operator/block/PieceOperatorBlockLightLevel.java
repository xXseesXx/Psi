/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockLightLevel.java:1
 * Modern: Level.getMaxLocalRawBrightness(BlockPos). GTNH: World.getBlockLightValue + getSunBrightness?
 */
package vazkii.psi.common.spell.operator.block;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compat.BlockPosCompat;

public class PieceOperatorBlockLightLevel extends PieceOperator {

    SpellParam<Vector3> target;

    public PieceOperatorBlockLightLevel(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPosCompat pos = SpellHelpers.getBlockPos(this, context, target, false, false);
        int j = context.focalPoint.worldObj.getBlockLightValue(pos.x, pos.y, pos.z);
        return j * 1.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
