/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.constant;

import vazkii.psi.api.spell.EnumPieceType;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.api.spell.SpellRuntimeException;

/**
 * Number constant piece for spells.
 * Barebones version for 1.7.10 - just holds a double value.
 */
public class PieceConstantNumber extends SpellPiece {

    public double constant = 0.0;

    public PieceConstantNumber(Spell spell) {
        super(spell);
    }

    @Override
    public EnumPieceType getPieceType() {
        return EnumPieceType.CONSTANT;
    }

    @Override
    public Class<?> getEvaluationType() {
        return Double.class;
    }

    @Override
    public Object evaluate() throws SpellCompilationException {
        return constant;
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        return constant;
    }

    @Override
    public boolean interceptKeystrokes() {
        return true;
    }

    @Override
    public boolean onCharTyped(char character, int keyCode, boolean doit) {
        if (Character.isISOControl(character) || "fFdD".indexOf(character) >= 0) {
            return false;
        }

        String oldValue = getDisplayValue();
        String next = oldValue;
        if ((next.equals("0") || next.equals("-0")) && "+-.".indexOf(character) < 0) {
            next = next.replace("0", "");
        }
        if (character == '+') {
            next = next.replace("-", "");
        } else if (character == '-') {
            if (!next.startsWith("-")) {
                next = "-" + next;
            }
        } else {
            next += character;
        }
        return setFromProgrammerText(oldValue, next, doit);
    }

    @Override
    public boolean onKeyPressed(int keyCode, boolean doit) {
        // LWJGL 2 Keyboard.KEY_BACK is 14.
        if (keyCode != 14) {
            return false;
        }
        String oldValue = getDisplayValue();
        String next = oldValue;
        if (next.length() == 2 && next.startsWith("-")) {
            next = "-0";
        } else if (next.equals("-")) {
            next = "0";
        } else if (!next.isEmpty()) {
            next = next.substring(0, next.length() - 1);
        }
        return setFromProgrammerText(oldValue, next, doit);
    }

    public String getDisplayValue() {
        if (constant == Math.rint(constant)) {
            return Long.toString((long) constant);
        }
        return Double.toString(constant);
    }

    private boolean setFromProgrammerText(String oldValue, String next, boolean doit) {
        if (next.isEmpty() || next.equals("-")) {
            next = next.equals("-") ? "-0" : "0";
        }
        if (next.length() > 5) {
            return false;
        }
        try {
            double parsed = Double.parseDouble(next);
            if (Double.isInfinite(parsed) || Double.isNaN(parsed)) {
                return false;
            }
            if (doit) {
                constant = parsed;
            }
            return !next.equals(oldValue);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    protected void writePieceToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        // 1.21.1 stores the editable number text as a string; retain the old
        // numeric field as a fallback for spells written by earlier backport builds.
        nbt.setString("constantValue", Double.toString(constant));
        nbt.setDouble("constant", constant);
    }

    @Override
    protected void readPieceFromNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        if (nbt.hasKey("constantValue")) {
            try {
                constant = Double.parseDouble(nbt.getString("constantValue"));
            } catch (NumberFormatException e) {
                constant = nbt.getDouble("constantValue");
            }
        } else {
            constant = nbt.getDouble("constant");
        }
    }
}
