/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common\spell\trick\block\PieceTrickBreakInSequence.java:1
 * GTNH: World break block sequence via World.func_147480_a (destroyBlock) + isInRadius per block.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceTrickBreakInSequence extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Vector3> target;
    SpellParam<Double> max;

    public PieceTrickBreakInSequence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
        addParam(max = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 posVal = this.getParamValue(context, position);
        Vector3 targetVal = this.getParamValue(context, target);
        Double maxVal = this.getParamValueOrDefault(context, max, 9.0);
        if (posVal == null || targetVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        Vector3 dir = targetVal.copy()
            .sub(posVal)
            .normalize()
            .multiply(1);
        for (int i = 0; i < maxVal; i++) {
            Vector3 cur = posVal.copy()
                .add(
                    dir.copy()
                        .multiply(i));
            if (!context.isInRadius(cur)) break;
            BlockPosCompat pos = cur.toBlockPos();
            Block block = context.focalPoint.worldObj.getBlock(pos.x, pos.y, pos.z);
            if (block != null && !context.focalPoint.worldObj.isAirBlock(pos.x, pos.y, pos.z)) {
                context.focalPoint.worldObj.func_147480_a(pos.x, pos.y, pos.z, true);
            }
        }
        return null;
    }
}
