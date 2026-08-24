/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

/**
 * A basic abstract piece of a spell. Instances of this class are created as needed
 * by the {@link Spell} object.
 * 
 * 1.7.10 Backport: Removed all rendering/GUI code. This is pure logic.
 * NBT serialization deferred to Phase 7.
 */
public abstract class SpellPiece {

    public static final Spell dummySpell = new Spell();

    public final ResourceLocation registryKey;
    public final Spell spell;
    public final Map<String, SpellParam<?>> params = new LinkedHashMap<>();
    public final Map<SpellParam<?>, SpellParam.Side> paramSides = new LinkedHashMap<>();
    private final Map<EnumSpellStat, StatLabel> statLabels = new HashMap<>();

    public boolean isInGrid = false;
    public int x, y;
    public String comment;

    public SpellPiece(Spell spell) {
        this.spell = spell;
        // Registry key will be set when registry system is implemented
        // For now, use a placeholder based on class name
        this.registryKey = new ResourceLocation(
            "psi",
            getClass().getSimpleName()
                .toLowerCase()
                .replace("piece", ""));
        initParams();
    }

    /**
     * Called to init this SpellPiece's {@link #params}. It's recommended you keep all params
     * registered here as fields in your implementation, as they should be used in
     * {@link #getParamValue(SpellContext, SpellParam)}.
     */
    public void initParams() {
        // NO-OP
    }

    /**
     * Gets what type of piece this is.
     */
    public abstract EnumPieceType getPieceType();

    /**
     * Gets what type this piece evaluates as. This is what other pieces
     * linked to it will read. For example, a number sum operator will return
     * Double.class, whereas a vector sum operator will return Vector3.class.<br>
     * If you want this piece to not evaluate to anything (for Tricks, for example),
     * return {@link Void}.class.
     */
    public abstract Class<?> getEvaluationType();

    /**
     * Evaluates this piece for the purpose of spell metadata calculation. If the piece
     * is not a constant, you can safely return null.
     */
    public abstract Object evaluate() throws SpellCompilationException;

    /**
     * Executes this piece and returns the value of this piece for later pieces to pick up
     * on. For example, the number sum operator would use this function to act upon its parameters
     * and return the result.
     */
    public abstract Object execute(SpellContext context) throws SpellRuntimeException;

    /**
     * Gets the string to be displayed describing this piece's evaluation type.
     * In full implementation, this would return a localized string.
     * For barebones, returns simple type name.
     */
    public String getEvaluationTypeString() {
        Class<?> evalType = getEvaluationType();
        String evalStr = evalType == null ? "null" : evalType.getSimpleName();
        String s = "Type: " + evalStr;
        if (getPieceType() == EnumPieceType.CONSTANT) {
            s += " (constant)";
        }
        return s;
    }

    /**
     * Adds this piece's stats to the Spell's metadata.
     */
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        // NO-OP
    }

    /**
     * Adds a {@link SpellParam} to this piece.
     */
    public void addParam(SpellParam<?> param) {
        params.put(param.name, param);
        paramSides.put(param, SpellParam.Side.OFF);
    }

    /**
     * Sets the side for a parameter.
     */
    public void setParamSide(SpellParam<?> param, SpellParam.Side side) {
        paramSides.put(param, side);
    }

    /**
     * Gets the value of a parameter by executing the piece connected to it.
     * This reads from the evaluatedObjects grid which is populated during spell execution.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParamValue(SpellContext context, SpellParam<T> param) throws SpellRuntimeException {
        T returnValue = (T) getRawParamValue(context, param);

        // Validate numeric values
        if (returnValue instanceof Number) {
            Number number = (Number) returnValue;
            double d = number.doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                throw new SpellRuntimeException(SpellRuntimeException.NAN);
            }
        }

        return returnValue;
    }

    /**
     * Gets the raw value of a parameter from the evaluated objects grid.
     * This is called by getParamValue after validation.
     */
    public Object getRawParamValue(SpellContext context, SpellParam<?> param) {
        SpellParam.Side side = paramSides.get(param);

        // If parameter is not enabled (optional parameter not set), return null
        if (side == null || !side.isEnabled()) {
            return null;
        }

        // Get the piece at this side
        int targetX = x + side.offx;
        int targetY = y + side.offy;

        if (!SpellGrid.exists(targetX, targetY)) {
            return null;
        }

        SpellPiece piece = spell.grid.gridData[targetX][targetY];
        if (piece == null || !param.canAccept(piece)) {
            return null;
        }

        // Return the evaluated value from the context grid
        return context.evaluatedObjects[piece.x][piece.y];
    }

    /**
     * Defaulted version of getParamValue.
     * Should be used for optional params.
     */
    public <T> T getParamValueOrDefault(SpellContext context, SpellParam<T> param, T def) {
        try {
            T v = getParamValue(context, param);
            return v == null ? def : v;
        } catch (SpellRuntimeException e) {
            return def;
        }
    }

    /**
     * Gets a stat label for the given stat.
     */
    public StatLabel getStatLabel(EnumSpellStat stat) {
        return statLabels.computeIfAbsent(stat, k -> new StatLabel(0));
    }

    /**
     * Sets a stat label.
     */
    public void setStatLabel(EnumSpellStat stat, StatLabel label) {
        statLabels.put(stat, label);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " @ (" + x + ", " + y + ")";
    }

}
