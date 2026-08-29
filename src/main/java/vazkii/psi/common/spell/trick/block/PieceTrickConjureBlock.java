/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\spell\trick\block\PieceTrickConjureBlock.java:34
 * Modern: Level, BlockState, BlockConjured.SOLID, ModBlocks.conjured, TileConjured.colorize, scheduleTick.
 * GTNH: World, BlockPosCompat, BlockConjured as BlockContainer, TileConjured, scheduleBlockUpdate, colorizer via
 * CADData.
 * Pathing for hard pieces: now BlockConjured/TileConjured stubs exist, trick can be implemented.
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.ICAD;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;
import vazkii.psi.common.block.BlockConjured;
import vazkii.psi.common.block.tile.TileConjured;
import vazkii.psi.compat.BlockPosCompat;

public class PieceTrickConjureBlock extends PieceTrick {

    SpellParam<Vector3> position;
    SpellParam<Double> time;

    public PieceTrickConjureBlock(Spell spell) {
        super(spell);
        setStatLabel(EnumSpellStat.POTENCY, new StatLabel(15));
        setStatLabel(EnumSpellStat.COST, new StatLabel(20));
    }

    public static void conjure(SpellContext context, Double timeVal, BlockPosCompat pos,
        net.minecraft.world.World world, net.minecraft.block.Block state) {
        if (world.getBlock(pos.x, pos.y, pos.z) != state) {
            if (conjure(world, pos, (EntityPlayer) context.caster, state)) {
                if (timeVal != null && timeVal.intValue() > 0) {
                    int val = timeVal.intValue();
                    world.scheduleBlockUpdate(pos.x, pos.y, pos.z, state, val);
                }

                TileConjured tile = (TileConjured) world.getTileEntity(pos.x, pos.y, pos.z);

                ItemStack cad = PsiAPI.getPlayerCAD(context.caster);
                if (tile != null && cad != null && cad.getItem() instanceof ICAD) {
                    tile.colorizer = ((ICAD) cad.getItem()).getComponentInSlot(cad, EnumCADComponent.DYE);
                }
            }
        }
    }

    public static boolean conjure(net.minecraft.world.World world, BlockPosCompat pos, EntityPlayer player,
        net.minecraft.block.Block state) {
        if (!world.blockExists(pos.x, pos.y, pos.z) || !world.canMineBlock(player, pos.x, pos.y, pos.z)) {
            return false;
        }

        if (world.isAirBlock(pos.x, pos.y, pos.z) || world.getBlock(pos.x, pos.y, pos.z)
            .isReplaceable(world, pos.x, pos.y, pos.z)) {
            return world.setBlock(pos.x, pos.y, pos.z, state);
        }
        return false;
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(time = new ParamNumber(SpellParam.GENERIC_NAME_TIME, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);
        meta.addStat(EnumSpellStat.POTENCY, 15);
        meta.addStat(EnumSpellStat.COST, 20);
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = this.getParamValue(context, position);
        Double timeVal = this.getParamValue(context, time);

        if (positionVal == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
        }
        if (!context.isInRadius(positionVal)) {
            throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
        }

        BlockPosCompat pos = positionVal.toBlockPos();

        if (!context.focalPoint.worldObj.canMineBlock((EntityPlayer) context.caster, pos.x, pos.y, pos.z)) {
            return null;
        }

        conjure(context, timeVal, pos, context.focalPoint.worldObj, new BlockConjured());

        return null;
    }

    public net.minecraft.block.Block messWithState(net.minecraft.block.Block state) {
        return state;
    }
}
