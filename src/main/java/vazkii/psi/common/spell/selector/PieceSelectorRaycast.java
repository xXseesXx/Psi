/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.selector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceSelector;

/**
 * BAREBONES implementation for 1.7.10
 * Raycasts from caster's eye to find the block they're looking at
 */
public class PieceSelectorRaycast extends PieceSelector {

    public SpellParam<Double> maxDist;

    public PieceSelectorRaycast(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(maxDist = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.PURPLE, true, false));
    }

    @Override
    public Class<?> getEvaluationType() {
        return Vector3.class;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        EntityPlayer player = context.caster;

        if (player == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        // Get max distance (default to 32 blocks)
        Double maxDistVal = this.getParamValue(context, maxDist);
        double distance = (maxDistVal != null) ? maxDistVal : 32.0;

        // Clamp to reasonable range
        if (distance > SpellContext.MAX_DISTANCE) {
            distance = SpellContext.MAX_DISTANCE;
        }
        if (distance < 0) {
            distance = 0;
        }

        // Get player eye position
        Vec3 eyePos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        // Get look direction
        Vec3 lookVec = player.getLookVec();

        // Calculate end position
        Vec3 endPos = eyePos.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);

        // Raycast for blocks
        MovingObjectPosition result = player.worldObj.rayTraceBlocks(eyePos, endPos);

        // Check if we hit a block
        if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        // Return block position as Vector3
        return new Vector3(result.blockX, result.blockY, result.blockZ);
    }
}
