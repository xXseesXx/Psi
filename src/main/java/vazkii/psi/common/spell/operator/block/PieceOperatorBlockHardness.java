/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockHardness.java:1
 * Modern: BlockPos+BlockState.getDestroySpeed(Level). GTNH: BlockPosCompat + World.getBlock + getBlockHardness.
 */
package vazkii.psi.common.spell.operator.block;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceOperatorBlockHardness extends PieceOperator {

    SpellParam<Vector3> target;

    public PieceOperatorBlockHardness(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.RED, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPosCompat pos = SpellHelpers.getBlockPos(this, context, target, false, false);
        World world = context.focalPoint.worldObj;
        Block block = world.getBlock(pos.x, pos.y, pos.z);
        if (block == null || world.isAirBlock(pos.x, pos.y, pos.z)) return -1.0D;
        return (double) block.getBlockHardness(world, pos.x, pos.y, pos.z);
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
