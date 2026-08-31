/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickBlaze.java:1
 * Modern: BlockSnapshot + EntityPlaceEvent + Blocks.FIRE.defaultBlockState(). GTNH: World.setBlock(x,y,z, Blocks.fire)
 * + isAirBlock check.
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compat.BlockPosCompat;

public class PieceTrickBlaze extends PieceTrick {

    SpellParam<Vector3> target;

    public PieceTrickBlaze(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 targetVal = this.getParamValue(context, target);

        if (targetVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        if (!context.isInRadius(targetVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        World world = context.focalPoint.worldObj;

        BlockPosCompat pos = targetVal.toBlockPos();

        // Modern Psi first tries the block below the target.
        BlockPosCompat bpos = pos.down();
        Block block = world.getBlock(bpos.x, bpos.y, bpos.z);

        if (block.isReplaceable(world, pos.x, pos.y, pos.z)) {
            world.setBlock(pos.x, pos.y, pos.z, Blocks.fire);
            return null;
        }
        // If the block below cannot be replaced, try the original target
        block = world.getBlock(pos.x, pos.y, pos.z);

        if (block.isReplaceable(world, pos.x, pos.y, pos.z)) {
            world.setBlock(pos.x, pos.y, pos.z, Blocks.fire);
        }

        return null;
    }
}
