/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common/spell/trick/PieceTrickOvergrow.java:1
 * Modern: Level bonemeal via BonemealableBlock. GTNH: World bonemeal via ItemDye.applyBonemeal.
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compat.BlockPosCompat;

public class PieceTrickOvergrow extends PieceTrick {

    SpellParam<Vector3> position;

    public PieceTrickOvergrow(Spell spell) {
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

        ItemStack boneMeal = new ItemStack(Items.dye, 1, 15);

        if (ItemDye.applyBonemeal(boneMeal, world, pos.x, pos.y, pos.z, context.caster)) {

            if (!world.isRemote) {
                world.playAuxSFX(2005, pos.x, pos.y, pos.z, 0);
            }
        }

        return null;
    }
}
