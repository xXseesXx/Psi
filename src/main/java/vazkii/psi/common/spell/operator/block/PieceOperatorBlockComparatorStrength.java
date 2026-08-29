/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on
 * Psi-1.21.1/src/main/java/vazkii/psi/common/spell/operator/block/PieceOperatorBlockComparatorStrength.java:1
 */
package vazkii.psi.common.spell.operator.block;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceOperator;
import vazkii.psi.compat.BlockPosCompat;

public class PieceOperatorBlockComparatorStrength extends PieceOperator {

    SpellParam<Vector3> axisParam;
    SpellParam<Vector3> target;

    public PieceOperatorBlockComparatorStrength(Spell spell) {
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

        ForgeDirection whichWay = SpellHelpers.getFacing(this, context, axisParam);

        if (whichWay == ForgeDirection.UP || whichWay == ForgeDirection.DOWN) {
            throw new SpellRuntimeException(SpellRuntimeException.COMPARATOR);
        }

        World world = context.focalPoint.worldObj;

        int side = toMinecraftSide(whichWay.getOpposite());
        Block block = world.getBlock(pos.x, pos.y, pos.z);

        if (block.hasComparatorInputOverride()) {
            return (double) block.getComparatorInputOverride(world, pos.x, pos.y, pos.z, side);
        }

        int signal = world.getIndirectPowerLevelTo(pos.x, pos.y, pos.z, side);

        if (signal < 15 && block.isNormalCube()) {
            int nextX = pos.x + whichWay.offsetX;
            int nextY = pos.y + whichWay.offsetY;
            int nextZ = pos.z + whichWay.offsetZ;

            Block nextBlock = world.getBlock(nextX, nextY, nextZ);

            if (nextBlock.hasComparatorInputOverride()) {
                signal = nextBlock.getComparatorInputOverride(world, nextX, nextY, nextZ, side);
            }
        }

        return (double) signal;
    }

    private int toMinecraftSide(ForgeDirection direction) {
        return switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case WEST -> 4;
            case EAST -> 5;
            default -> 0;
        };
    }

    @Override
    public Class<Double> getEvaluationType() {
        return Double.class;
    }
}
