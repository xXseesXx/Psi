/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 * 1.7.10 Backport: Based on Psi-1.21.1/src/main/java/vazkii/psi/common\block\BlockConjured.java:40
 * Modern: Block with BlockStateProperties SOLID/LIGHT/WATERLOGGED, VoxelShape, Level. GTNH: BlockContainer with
 * TileConjured, Material, lightValue, no VoxelShape.
 * Pathing for hard pieces: provides BlockConjured.SOLID/LIGHT constants and TileConjured creation, updateTick removes
 * block.
 */
package vazkii.psi.common.block;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import vazkii.psi.common.block.tile.TileConjured;

public class BlockConjured extends BlockContainer {

    public static final String SOLID = "solid";
    public static final String LIGHT = "light";

    public BlockConjured() {
        super(Material.rock);
        setBlockName("psi:conjured");
        setHardness(1.5F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileConjured();
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        world.setBlockToAir(x, y, z);
    }

    @Override
    public int getLightValue(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        return 15;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
}
