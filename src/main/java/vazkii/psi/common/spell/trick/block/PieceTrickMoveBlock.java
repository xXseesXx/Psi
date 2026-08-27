/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\spell\trick\block\PieceTrickMoveBlock.java:1
 * GTNH: World move block via getBlock/setBlock + TileEntity.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceTrickMoveBlock extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Vector3> target;

    public PieceTrickMoveBlock(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 posVal = this.getParamValue(context, position);
        Vector3 targetVal = this.getParamValue(context, target);
        if (posVal == null || targetVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(posVal) || !context.isInRadius(targetVal))
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        BlockPosCompat pos = posVal.toBlockPos();
        BlockPosCompat targetPos = targetVal.toBlockPos();
        Block block = context.focalPoint.worldObj.getBlock(pos.x, pos.y, pos.z);
        int meta = context.focalPoint.worldObj.getBlockMetadata(pos.x, pos.y, pos.z);
        TileEntity tile = context.focalPoint.worldObj.getTileEntity(pos.x, pos.y, pos.z);
        if (context.focalPoint.worldObj.isAirBlock(targetPos.x, targetPos.y, targetPos.z)) {
            context.focalPoint.worldObj.setBlock(targetPos.x, targetPos.y, targetPos.z, block, meta, 3);
            if (tile != null) context.focalPoint.worldObj.setTileEntity(targetPos.x, targetPos.y, targetPos.z, tile);
            context.focalPoint.worldObj.setBlockToAir(pos.x, pos.y, pos.z);
        }
        return null;
    }
}
