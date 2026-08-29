/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/api/spell/SpellHelpers.java:16
 * Modern uses net.minecraft.core.BlockPos, Direction, Level, BlockState.
 * GTNH adaptation: BlockPosCompat, ForgeDirection, World, Block+meta.
 * Keep method names/signatures 1:1 where possible; param generic Number→Double and
 * Vector-based checks use 1.7.10 Vector3.isInRadius.
 */
package vazkii.psi.api.spell;

import net.minecraftforge.common.util.ForgeDirection;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.compat.BlockPosCompat;

public class SpellHelpers {

    public static double getBoundedNumber(SpellPiece piece, SpellContext context, SpellParam<Double> param,
        double def) {
        Double val = piece.getParamValueOrDefault(context, param, def);
        if (val == null) return def;
        return Math.min(val, def);
    }

    public static double ensurePositiveOrZero(SpellPiece piece, SpellParam<Double> param)
        throws SpellCompilationException {
        Double val = piece.getParamEvaluation(param);
        if (val == null || val < 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, piece.x, piece.y);
        }
        return val;
    }

    public static double ensurePositiveAndNonzero(SpellPiece piece, SpellParam<Double> param)
        throws SpellCompilationException {
        Double val = piece.getParamEvaluation(param);
        if (val == null || val <= 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, piece.x, piece.y);
        }
        return val;
    }

    public static double ensurePositiveOrZero(SpellPiece piece, SpellParam<Double> param, double def)
        throws SpellCompilationException {
        Double val = piece.getParamEvaluation(param);
        if (val == null) val = def;
        if (val == null || val < 0) {
            throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, piece.x, piece.y);
        }
        return val;
    }

    public static double rangeLimitParam(SpellPiece piece, SpellContext context, SpellParam<Double> param, double max)
        throws SpellRuntimeException {
        Double numberVal = piece.getParamValue(context, param);
        if (numberVal == null) {
            return max;
        }
        return Math.min(max, Math.max(-max, numberVal));
    }

    public static ForgeDirection getFacing(SpellPiece piece, SpellContext context, SpellParam<Vector3> param)
        throws SpellRuntimeException {
        Vector3 face = getVector3(piece, context, param, false, true);
        // ForgeDirection.getOrientation with nearest logic
        return ForgeDirection.getOrientation(getFacingIndex(face));
    }

    private static int getFacingIndex(Vector3 face) {
        // Mirrors Direction.getNearest logic — pick largest absolute component
        double ax = Math.abs(face.x), ay = Math.abs(face.y), az = Math.abs(face.z);
        if (ax > ay && ax > az) return face.x > 0 ? 5 : 4; // EAST:5 WEST:4
        if (ay > ax && ay > az) return face.y > 0 ? 1 : 0; // UP:1 DOWN:0
        return face.z > 0 ? 3 : 2; // SOUTH:3 NORTH:2
    }

    public static boolean isBlockPosInRadius(SpellContext context, BlockPosCompat pos) {
        return context.isInRadius(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5);
    }

    public static Vector3 getVector3(SpellPiece piece, SpellContext context, SpellParam<Vector3> param, boolean check,
        boolean shouldBeAxial) throws SpellRuntimeException {
        return checkPos(piece, context, param, check, shouldBeAxial);
    }

    public static Vector3 getVector3(SpellPiece piece, SpellContext context, SpellParam<Vector3> param, boolean NotNull,
        boolean check, boolean shouldBeAxial) throws SpellRuntimeException {
        return checkPos(piece, context, param, NotNull, check, shouldBeAxial);
    }

    public static BlockPosCompat getBlockPos(SpellPiece piece, SpellContext context, SpellParam<Vector3> param,
        boolean check, boolean shouldBeAxial) throws SpellRuntimeException {
        return checkPos(piece, context, param, check, shouldBeAxial).toBlockPos();
    }

    public static Vector3 checkPos(SpellPiece piece, SpellContext context, SpellParam<Vector3> param, boolean check,
        boolean shouldBeAxial) throws SpellRuntimeException {
        return checkPos(piece, context, param, true, check, shouldBeAxial);
    }

    public static Vector3 checkPos(SpellPiece piece, SpellContext context, SpellParam<Vector3> param, boolean NotNull,
        boolean check, boolean shouldBeAxial) throws SpellRuntimeException {
        Vector3 position = piece.getParamValue(context, param);
        if (NotNull && (position == null || position.isZero())) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (check && position != null && !context.isInRadius(position)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }
        if (shouldBeAxial && position != null && !position.isAxial()) {
            throw new SpellRuntimeException(SpellRuntimeException.NON_AXIAL_VECTOR);
        }
        return position;
    }

    public static Vector3 getDefaultedVector(SpellPiece piece, SpellContext context, SpellParam<Vector3> param,
        boolean check, boolean shouldBeAxial, Vector3 def) throws SpellRuntimeException {
        Vector3 position = piece.getParamValue(context, param);
        if (position == null || position.isZero()) {
            if (def == null || def.isZero()) throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
            return def;
        } else return checkPos(piece, context, param, false, check, shouldBeAxial);
    }
}
