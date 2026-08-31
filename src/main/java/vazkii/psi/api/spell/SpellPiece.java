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
        String s = getDatatypeName(getEvaluationType());
        if (getPieceType() == EnumPieceType.CONSTANT) {
            s += " " + net.minecraft.client.resources.I18n.format("psimisc.constant");
        }
        return s;
    }

    /** Whether this piece accepts a connection on the given side. */
    public boolean isInputSide(SpellParam.Side side) {
        return paramSides.containsValue(side);
    }

    /** Sort key used by the modern piece picker. */
    public String getSortingName() {
        return net.minecraft.client.resources.I18n.format(getUnlocalizedName());
    }

    /** Returns the localized programmer name for a spell value type. */
    public static String getDatatypeName(Class<?> type) {
        String key;
        if (type == null) key = "null";
        else if (type == Void.class) key = "void";
        else if (type == Double.class || type == Float.class
            || type == Integer.class
            || type == Long.class
            || Number.class.isAssignableFrom(type)) key = "number";
        else if (type == vazkii.psi.api.internal.Vector3.class) key = "vector3";
        else if (net.minecraft.entity.player.EntityPlayer.class.isAssignableFrom(type)) key = "player";
        else if (net.minecraft.entity.Entity.class.isAssignableFrom(type)) key = "entity";
        else if (type == String.class) key = "string";
        else key = type.getSimpleName()
            .toLowerCase(java.util.Locale.ROOT);
        return net.minecraft.client.resources.I18n.format("psi.datatype." + key);
    }

    /**
     * The translation key used by the programmer. Keeping this on the piece,
     * rather than teaching the GUI about every registered piece, is important
     * for addon pieces to behave exactly like built-in ones.
     */
    public String getUnlocalizedName() {
        return "psi.spellpiece." + registryKey.getResourcePath();
    }

    public String getUnlocalizedDesc() {
        return getUnlocalizedName() + ".desc";
    }

    /** Adds the standard programmer tooltip for this piece. */
    public void getTooltip(java.util.List<String> tooltip) {
        tooltip.add(net.minecraft.client.resources.I18n.format(getUnlocalizedName()));
        String description = net.minecraft.client.resources.I18n.format(getUnlocalizedDesc());
        // Missing translations are returned as their key by 1.7.10's I18n.
        if (!description.equals(getUnlocalizedDesc())) {
            tooltip.add("\u00a77" + description);
        }
    }

    /** Whether this piece consumes programmer key input while selected. */
    public boolean interceptKeystrokes() {
        return false;
    }

    /**
     * Handles typed programmer input. {@code doit} permits callers to check
     * whether an edit is valid before committing it.
     */
    public boolean onCharTyped(char character, int keyCode, boolean doit) {
        return false;
    }

    /** Handles non-character programmer keys such as Backspace. */
    public boolean onKeyPressed(int keyCode, boolean doit) {
        return false;
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

        try {
            SpellPiece piece = spell.grid.getPieceAtSideWithRedirections(x, y, side);
            return piece == null || !param.canAccept(piece) ? null : context.evaluatedObjects[piece.x][piece.y];
        } catch (SpellCompilationException e) {
            return null;
        }
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
     * Null-safe version of getParamValue — mirrors modern Psi.
     * Modern counterpart: Psi-1.21.1/src/main/java/vazkii/psi/api/spell/SpellPiece.java:257
     */
    public <T> T getNotNullParamValue(SpellContext context, SpellParam<T> param) throws SpellRuntimeException {
        T v = getParamValue(context, param);
        if (v == null) {
            throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
        }
        return v;
    }

    /**
     * Null-safe version of getParamEvaluation — mirrors modern Psi.
     * Modern counterpart: SpellPiece.java:312
     */
    public <T> T getNotNullParamEvaluation(SpellParam<T> param) throws SpellCompilationException {
        T v = getParamEvaluation(param);
        if (v == null) {
            throw new SpellCompilationException(SpellCompilationException.NULL_PARAM, this.x, this.y);
        }
        return v;
    }

    /**
     * Defaulted version of getParamEvaluation with typo-preserved name for close-to-source.
     * Modern has getParamEvaluationeOrDefault (typo). Added for diff parity.
     */
    public <T> T getParamEvaluationeOrDefault(SpellParam<T> param, T def) throws SpellCompilationException {
        T v = getParamEvaluation(param);
        return v == null ? def : v;
    }

    /**
     * Evaluates a constant parameter while compiling metadata. This is the
     * 1.7.10 equivalent of modern Psi's getParamEvaluation implementation.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParamEvaluation(SpellParam<?> param) throws SpellCompilationException {
        SpellParam.Side side = paramSides.get(param);
        if (side == null || !side.isEnabled()) return null;

        SpellPiece piece = spell.grid.getPieceAtSideWithRedirections(x, y, side);
        return piece == null || !param.canAccept(piece) ? null : (T) piece.evaluate();
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
        if (label == null) statLabels.remove(stat);
        else statLabels.put(stat, label);
    }

    /** Whether this piece exposes formula details in the programmer tooltip. */
    public boolean hasStatLabels() {
        return !statLabels.isEmpty();
    }

    public StatLabel getDefinedStatLabel(EnumSpellStat stat) {
        return statLabels.get(stat);
    }

    // NBT Serialization ============================================================

    /**
     * Write this piece to NBT, including its ID, parameter sides, and custom data.
     */
    public void writeToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        // 1.21.1 uses "key" and short underscore IDs for Psi pieces.
        String id = vazkii.psi.common.spell.SpellPieceRegistry.getID(this);
        if (id != null) {
            nbt.setString("key", id.startsWith("psi:") ? "_" + id.substring(4) : id);
        }

        net.minecraft.nbt.NBTTagCompound paramsNbt = new net.minecraft.nbt.NBTTagCompound();
        for (Map.Entry<SpellParam<?>, SpellParam.Side> entry : paramSides.entrySet()) {
            String name = entry.getKey().name;
            if (name.startsWith(SpellParam.PSI_PREFIX)) {
                name = "_" + name.substring(SpellParam.PSI_PREFIX.length());
            }
            paramsNbt.setInteger(
                name,
                entry.getValue()
                    .asInt());
        }
        if (!paramSides.isEmpty()) nbt.setTag("params", paramsNbt);
        if (comment != null && !comment.isEmpty()) nbt.setString("comment", comment);

        // Save piece-specific data (override in subclasses)
        writePieceToNBT(nbt);
    }

    /**
     * Override in subclasses to save custom data (e.g., constant values).
     * Default implementation does nothing.
     */
    protected void writePieceToNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        // NO-OP - override in subclasses
    }

    /**
     * Create a piece from NBT data.
     * Returns null if piece ID is not registered.
     */
    public static SpellPiece createFromNBT(Spell spell, net.minecraft.nbt.NBTTagCompound nbt) {
        String id = nbt.hasKey("key") ? nbt.getString("key") : nbt.getString("id");
        if (id == null || id.isEmpty()) {
            System.err.println("[Psi] Piece NBT missing ID");
            return null;
        }

        // Create piece based on ID
        if (id.startsWith("_")) id = "psi:" + id.substring(1);
        SpellPiece piece = vazkii.psi.common.spell.SpellPieceRegistry.create(id, spell);
        if (piece == null) {
            return null;
        }

        // Fix the registryKey to use the proper ID from NBT (not the class name placeholder)
        try {
            java.lang.reflect.Field keyField = SpellPiece.class.getDeclaredField("registryKey");
            keyField.setAccessible(true);
            keyField.set(piece, new ResourceLocation(id));
        } catch (Exception e) {
            System.err.println("[Psi] Failed to set registryKey: " + e.getMessage());
        }

        // Load parameter sides
        net.minecraft.nbt.NBTTagCompound sidesNbt = nbt.getCompoundTag("paramSides");
        net.minecraft.nbt.NBTTagCompound paramsNbt = nbt.getCompoundTag("params");
        for (SpellParam<?> param : piece.params.values()) {
            String modernName = param.name.startsWith(SpellParam.PSI_PREFIX)
                ? "_" + param.name.substring(SpellParam.PSI_PREFIX.length())
                : param.name;
            if (paramsNbt.hasKey(modernName)) {
                int side = paramsNbt.getInteger(modernName);
                if (side >= 0 && side < SpellParam.Side.values().length) {
                    piece.setParamSide(param, SpellParam.Side.values()[side]);
                }
            } else if (sidesNbt.hasKey(param.name)) {
                String sideName = sidesNbt.getString(param.name);
                try {
                    SpellParam.Side side = SpellParam.Side.valueOf(sideName);
                    piece.setParamSide(param, side);
                } catch (IllegalArgumentException e) {
                    System.err.println("[Psi] Invalid param side: " + sideName);
                }
            }
        }

        // Load piece-specific data
        piece.comment = nbt.hasKey("comment") ? nbt.getString("comment") : null;
        piece.readPieceFromNBT(nbt);

        return piece;
    }

    /**
     * Override in subclasses to load custom data (e.g., constant values).
     * Default implementation does nothing.
     */
    protected void readPieceFromNBT(net.minecraft.nbt.NBTTagCompound nbt) {
        // NO-OP - override in subclasses
    }

    // End NBT Serialization ========================================================

    @Override
    public String toString() {
        return getClass().getSimpleName() + " @ (" + x + ", " + y + ")";
    }

}
