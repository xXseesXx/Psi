/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/selector/PieceSelectorBlockPresence.java:15
 * Modern: BlockPos+BlockState+CollisionContext.getCollisionShape. GTNH:
 * BlockPosCompat+World.getBlock+isAirBlock+isSideSolid.
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceSelector;
import vazkii.psi.compat.BlockPosCompat;

public class PieceSelectorBlockPresence extends PieceSelector {

    SpellParam<Vector3> position;

    public PieceSelectorBlockPresence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        BlockPosCompat pos = positionVal.toBlockPos();
        World world = context.focalPoint.worldObj;
        Block block = world.getBlock(pos.x, pos.y, pos.z);

        if (world.isAirBlock(pos.x, pos.y, pos.z)) {
            return 0.0;
        } else if (world.getBlock(pos.x, pos.y, pos.z)
            .isCollidable()
            && world.getBlock(pos.x, pos.y, pos.z)
                .getCollisionBoundingBoxFromPool(world, pos.x, pos.y, pos.z) == null) {
                    return 1.0;
                }
        return 2.0;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }
}
