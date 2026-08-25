package vazkii.psi.common.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellPiece;
import vazkii.psi.common.spell.constant.PieceConstantNumber;
import vazkii.psi.common.spell.constant.PieceConstantString;
import vazkii.psi.common.spell.operator.PieceOperatorSum;
import vazkii.psi.common.spell.selector.PieceSelectorCaster;
import vazkii.psi.common.spell.selector.PieceSelectorEntityPosition;
import vazkii.psi.common.spell.selector.PieceSelectorRaycast;
import vazkii.psi.common.spell.trick.PieceTrickBreakBlock;
import vazkii.psi.common.spell.trick.PieceTrickDebug;
import vazkii.psi.common.spell.trick.PieceTrickExplode;

/**
 * Registry for spell pieces. Maps string IDs to constructor functions.
 * Used for NBT serialization - allows creating pieces by ID.
 */
public class SpellPieceRegistry {

    private static final Map<String, Function<Spell, SpellPiece>> REGISTRY = new HashMap<>();

    /**
     * Register a spell piece type with an ID and factory function.
     */
    public static void register(String id, Function<Spell, SpellPiece> factory) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Spell piece ID already registered: " + id);
        }
        REGISTRY.put(id, factory);
    }

    /**
     * Create a spell piece from its ID.
     * Returns null if ID not registered.
     */
    public static SpellPiece create(String id, Spell spell) {
        Function<Spell, SpellPiece> factory = REGISTRY.get(id);
        if (factory == null) {
            System.err.println("[Psi] Unknown spell piece ID: " + id);
            return null;
        }
        SpellPiece piece = factory.apply(spell);

        // Fix registryKey using reflection (piece's constructor sets it based on class name)
        // We need to override it with the actual registry ID
        try {
            java.lang.reflect.Field keyField = SpellPiece.class.getDeclaredField("registryKey");
            keyField.setAccessible(true);
            keyField.set(piece, new ResourceLocation(id));
        } catch (Exception e) {
            System.err.println("[Psi] Failed to set registryKey for " + id);
            e.printStackTrace();
        }

        return piece;
    }

    /**
     * Get the ID for a spell piece class.
     * Returns null if not registered.
     */
    public static String getID(SpellPiece piece) {
        if (piece == null) {
            return null;
        }

        // Search registry for this piece's class
        Class<? extends SpellPiece> pieceClass = piece.getClass();
        for (Map.Entry<String, Function<Spell, SpellPiece>> entry : REGISTRY.entrySet()) {
            // Create a test instance to check class
            // This is a bit hacky but works for barebones
            try {
                SpellPiece test = entry.getValue()
                    .apply(piece.spell);
                if (test.getClass() == pieceClass) {
                    return entry.getKey();
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        System.err.println("[Psi] No ID registered for piece class: " + pieceClass.getName());
        return null;
    }

    /** A stable snapshot for GUI population and addon-friendly piece pickers. */
    public static java.util.List<String> getRegisteredIds() {
        java.util.List<String> ids = new java.util.ArrayList<String>(REGISTRY.keySet());
        java.util.Collections.sort(ids);
        return ids;
    }

    /**
     * Initialize the registry with all piece types.
     * Called on mod init.
     */
    public static void init() {
        // Constants
        register("psi:constant_number", PieceConstantNumber::new);
        register("psi:constant_string", PieceConstantString::new);

        // Operators
        register("psi:operator_sum", PieceOperatorSum::new);

        // Selectors
        register("psi:selector_caster", PieceSelectorCaster::new);
        register("psi:selector_raycast", PieceSelectorRaycast::new);
        register("psi:selector_entity_position", PieceSelectorEntityPosition::new);

        // Tricks
        register("psi:trick_debug", PieceTrickDebug::new);
        register("psi:trick_break_block", PieceTrickBreakBlock::new);
        register("psi:trick_explode", PieceTrickExplode::new);
    }
}
