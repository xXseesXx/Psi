/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\spell\trick\block\PieceTrickTill.java:1
 * GTNH: World.setBlock farmland.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.init.Blocks;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compat.BlockPosCompat;

public class PieceTrickTill extends PieceTrick {

    SpellParam<Vector3> position;

    public PieceTrickTill(Spell spell) {
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
        net.minecraft.block.Block block = context.focalPoint.worldObj.getBlock(pos.x, pos.y, pos.z);
        // GTNH: modern uses fake Iron Hoe UseOnContext which handles grass/dirt/coarseDirt + mayInteract + chunk check
        // Serviceable: handle grass/dirt if air above and no tile, like modern
        if ((block == Blocks.grass || block == Blocks.dirt)
            && context.focalPoint.worldObj.isAirBlock(pos.x, pos.y + 1, pos.z)) {
            if (context.focalPoint.worldObj.getTileEntity(pos.x, pos.y, pos.z) == null) {
                context.focalPoint.worldObj.setBlock(pos.x, pos.y, pos.z, Blocks.farmland);
                context.focalPoint.worldObj
                    .playSoundEffect(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, "step.gravel", 1.0F, 0.8F);
            }
        }
        return null;
    }
}
