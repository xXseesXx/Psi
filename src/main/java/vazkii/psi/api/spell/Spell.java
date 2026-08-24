/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import net.minecraft.nbt.NBTTagCompound;

/**
 * STUB for Phase 2: Minimal spell container.
 * Full implementation will be added in Phase 7 (spell piece linking).
 * 
 * 1.7.10 Backport: Barebones spell structure.
 */
public class Spell {

    /**
     * Metadata for this spell (stats, flags).
     */
    public final SpellMetadata metadata = new SpellMetadata();

    /**
     * The spell grid containing all pieces.
     */
    public final SpellGrid grid;

    /**
     * Name of this spell.
     */
    public String name = "";

    public Spell() {
        this.grid = new SpellGrid(this);
    }

    /**
     * Write spell to NBT.
     */
    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("name", name);

        // Write grid pieces as a list
        net.minecraft.nbt.NBTTagList gridList = new net.minecraft.nbt.NBTTagList();
        for (int x = 0; x < SpellGrid.GRID_SIZE; x++) {
            for (int y = 0; y < SpellGrid.GRID_SIZE; y++) {
                SpellPiece piece = grid.gridData[x][y];
                if (piece != null) {
                    NBTTagCompound pieceNbt = new NBTTagCompound();
                    pieceNbt.setInteger("x", x);
                    pieceNbt.setInteger("y", y);
                    piece.writeToNBT(pieceNbt);
                    gridList.appendTag(pieceNbt);
                }
            }
        }
        nbt.setTag("grid", gridList);

        // TODO: Serialize metadata if needed
    }

    /**
     * Read spell from NBT.
     */
    public static Spell readFromNBT(NBTTagCompound nbt) {
        Spell spell = new Spell();
        spell.name = nbt.getString("name");

        // Read grid pieces
        net.minecraft.nbt.NBTTagList gridList = nbt.getTagList("grid", 10); // 10 = compound tag
        for (int i = 0; i < gridList.tagCount(); i++) {
            NBTTagCompound pieceNbt = gridList.getCompoundTagAt(i);
            int x = pieceNbt.getInteger("x");
            int y = pieceNbt.getInteger("y");

            SpellPiece piece = SpellPiece.createFromNBT(spell, pieceNbt);
            if (piece != null) {
                piece.x = x;
                piece.y = y;
                piece.isInGrid = true;
                spell.grid.gridData[x][y] = piece;
            }
        }

        // TODO: Deserialize metadata if needed

        return spell;
    }

}
