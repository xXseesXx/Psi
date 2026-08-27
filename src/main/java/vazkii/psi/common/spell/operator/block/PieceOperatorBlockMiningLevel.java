/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockMiningLevel.java:1
 * Modern: PieceTrickBreakBlock.getHarvestLevel(BlockState). GTNH: Block.getHarvestLevel(meta) + tool lookup.
 * Simplified for 1.7.10 — returns 0 for non-harvestable, else level per metadata.
 * TODO: Mirrors modern getHarvestLevel after BlockConjured/trick port.
 */
package vazkii.psi.common.spell.operator.block;

import net.minecraft.block.Block;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceOperatorBlockMiningLevel extends PieceOperator {

    SpellParam<Vector3> position;

    public PieceOperatorBlockMiningLevel(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        BlockPosCompat pos = SpellHelpers.getBlockPos(this, context, position, false, false);
        Block block = context.focalPoint.worldObj.getBlock(pos.x, pos.y, pos.z);
        int meta = context.focalPoint.worldObj.getBlockMetadata(pos.x, pos.y, pos.z);
        String tool = block.getHarvestTool(meta);
        if (tool == null) return -1.0;
        int level = block.getHarvestLevel(meta);
        return (double) level;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
