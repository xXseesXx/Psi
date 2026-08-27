/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\spell\trick\block\PieceTrickCollapseBlock.java:1
 * GTNH: World falling block via BlockFalling.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compampac.BlockPosCompat;

public class PieceTrickCollapseBlock extends PieceTrick {

    SpellParam<Vector3> position;

    public PieceTrickCollapseBlock(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 posVal = this.getParamValue(context, position);
        if (posVal == null) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        if (!context.isInRadius(posVal)) throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        BlockPosCompat pos = posVal.toBlockPos();
        net.minecraft.world.World world = context.focalPoint.worldObj;
        Block block = world.getBlock(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);
        // GTNH: modern checks stateDown.isAir() && getDestroySpeed != -1 && canHarvest && no tile + BreakEvent
        // Serviceable: if below is air and block is not unbreakable and no tile, spawn falling — on server only, with
        // correct spawn order
        if (world.isAirBlock(pos.x, pos.y - 1, pos.z) && block.getBlockHardness(world, pos.x, pos.y, pos.z) != -1
            && world.getTileEntity(pos.x, pos.y, pos.z) == null) {
            if (!world.isRemote) {
                net.minecraft.entity.item.EntityFallingBlock falling = new net.minecraft.entity.item.EntityFallingBlock(
                    world,
                    pos.x + 0.5,
                    pos.y + 0.5,
                    pos.z + 0.5,
                    block,
                    meta);
                world.setBlockToAir(pos.x, pos.y, pos.z);
                world.spawnEntityInWorld(falling);
                world.markBlockForUpdate(pos.x, pos.y, pos.z);
                world.markBlockForUpdate(pos.x, pos.y - 1, pos.z);
            } else {
                world.setBlockToAir(pos.x, pos.y, pos.z);
            }
        }
        return null;
    }
}
