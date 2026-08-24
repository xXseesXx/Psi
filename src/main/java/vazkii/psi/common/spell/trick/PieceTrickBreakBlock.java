/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.StatLabel;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.compampac.BlockPosCompat;

/**
 * BAREBONES implementation for 1.7.10
 * Breaks a block at the given position with drops
 */
public class PieceTrickBreakBlock extends PieceTrick {

    public SpellParam<Vector3> position;

    public PieceTrickBreakBlock(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(20));
        setStatLabel(EnumSpellStat.COST, new StatLabel(50));
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        meta.addStat(EnumSpellStat.POTENCY, 20);
        meta.addStat(EnumSpellStat.COST, 50);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }

        // Check radius (barebones: use simple distance check)
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        // Convert to block coordinates
        BlockPosCompat pos = positionVal.toBlockPos();
        World world = context.caster.worldObj;

        // Check block is loaded
        if (!world.blockExists(pos.getX(), pos.getY(), pos.getZ())) {
            return null;
        }

        // Get block
        Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());

        // Don't break air or bedrock
        if (block.isAir(world, pos.getX(), pos.getY(), pos.getZ())) {
            return null;
        }

        float hardness = block.getBlockHardness(world, pos.getX(), pos.getY(), pos.getZ());
        if (hardness < 0) {
            // Unbreakable block (bedrock, barrier, etc.)
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }

        // Break block with drops (true = drop items)
        // In 1.7.10: func_147480_a is destroyBlock
        world.func_147480_a(pos.getX(), pos.getY(), pos.getZ(), true);

        return null;
    }
}
