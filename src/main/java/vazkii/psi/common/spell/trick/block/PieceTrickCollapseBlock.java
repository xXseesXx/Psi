/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickCollapseBlock.java:1
 * GTNH: Collapse blocks using vanilla EntityFallingBlock, regardless of
 * whether the block itself extends BlockFalling.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compat.BlockPosCompat;

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
        World world = context.focalPoint.worldObj;

        Block block = world.getBlock(pos.x, pos.y, pos.z);
        int meta = world.getBlockMetadata(pos.x, pos.y, pos.z);

        /*
         * Only collapse blocks that have empty space directly underneath.
         * EntityFallingBlock in 1.7.10 can represent ANY block. The block
         * does not need to extend BlockFalling.
         */
        if (world.isAirBlock(pos.x, pos.y - 1, pos.z) && block.getBlockHardness(world, pos.x, pos.y, pos.z) != -1
            && world.getTileEntity(pos.x, pos.y, pos.z) == null) {

            /*
             * Falling entities are server-side.
             * IMPORTANT:
             * Do NOT remove the original block here.
             * Vanilla EntityFallingBlock checks on its first tick that
             * the block still exists at its starting position. It then
             * removes the block itself.
             */
            if (!world.isRemote) {
                EntityFallingBlock falling = new EntityFallingBlock(
                    world,
                    pos.x + 0.5D,
                    pos.y + 0.5D,
                    pos.z + 0.5D,
                    block,
                    meta);

                world.spawnEntityInWorld(falling);
            }
        }

        return null;
    }
}
