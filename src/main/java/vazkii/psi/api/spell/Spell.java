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

    private static final String TAG_VALID = "validSpell";
    private static final String TAG_NAME = "spellName";
    private static final String TAG_LEGACY_NAME = "name";
    private static final String TAG_LIST = "spellList";
    private static final String TAG_LEGACY_LIST = "grid";

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
        nbt.setBoolean(TAG_VALID, true);
        nbt.setString(TAG_NAME, name);

        // Match the 1.21.1 spell format: positions are outside nested piece data.
        net.minecraft.nbt.NBTTagList gridList = new net.minecraft.nbt.NBTTagList();
        for (int x = 0; x < SpellGrid.GRID_SIZE; x++) {
            for (int y = 0; y < SpellGrid.GRID_SIZE; y++) {
                SpellPiece piece = grid.gridData[x][y];
                if (piece != null) {
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setInteger("x", x);
                    entry.setInteger("y", y);
                    NBTTagCompound data = new NBTTagCompound();
                    piece.writeToNBT(data);
                    entry.setTag("data", data);
                    gridList.appendTag(entry);
                }
            }
        }
        nbt.setTag(TAG_LIST, gridList);
    }

    /**
     * Read spell from NBT.
     */
    public static Spell readFromNBT(NBTTagCompound nbt) {
        Spell spell = new Spell();
        spell.name = nbt.hasKey(TAG_NAME) ? nbt.getString(TAG_NAME) : nbt.getString(TAG_LEGACY_NAME);

        // Read modern spellList/data and the old backport grid/direct-piece format.
        String listKey = nbt.hasKey(TAG_LIST) ? TAG_LIST : TAG_LEGACY_LIST;
        net.minecraft.nbt.NBTTagList gridList = nbt.getTagList(listKey, 10);
        for (int i = 0; i < gridList.tagCount(); i++) {
            NBTTagCompound entry = gridList.getCompoundTagAt(i);
            int x = entry.hasKey("spellPosX") ? entry.getInteger("spellPosX") : entry.getInteger("x");
            int y = entry.hasKey("spellPosY") ? entry.getInteger("spellPosY") : entry.getInteger("y");
            NBTTagCompound pieceNbt;
            if (entry.hasKey("data")) {
                pieceNbt = entry.getCompoundTag("data");
            } else if (entry.hasKey("spellData")) {
                pieceNbt = entry.getCompoundTag("spellData");
            } else {
                pieceNbt = entry;
            }

            if (!SpellGrid.exists(x, y)) {
                continue;
            }

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
