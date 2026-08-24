/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A spell that has been compiled and is ready for execution.
 * Contains the execution order and metadata for a spell.
 *
 * Barebones version for 1.7.10 - no error handlers or advanced features yet.
 */
public class CompiledSpell {

    /** The original source spell */
    public final Spell sourceSpell;

    /** Spell metadata (stats like cost, potency, bandwidth) */
    public final SpellMetadata metadata;

    /** Ordered list of actions to execute */
    public final List<Action> actions;

    /** Map from piece to its action (for quick lookup) */
    public final Map<SpellPiece, Action> actionMap;

    public CompiledSpell(Spell sourceSpell) {
        this.sourceSpell = sourceSpell;
        this.metadata = new SpellMetadata();
        this.actions = new LinkedList<>();
        this.actionMap = new HashMap<>();

        // Set bandwidth based on grid size
        if (sourceSpell.grid != null) {
            metadata.setStat(EnumSpellStat.BANDWIDTH, sourceSpell.grid.getSize());
        }
    }

    /**
     * Execute the compiled spell in the given context.
     *
     * @param context The spell context
     * @throws SpellRuntimeException if execution fails
     */
    public void execute(SpellContext context) throws SpellRuntimeException {
        // Execute actions in order
        for (Action action : actions) {
            action.execute(context);

            // Check if spell was stopped
            if (context.stopped) {
                return;
            }
        }
    }

    /**
     * An action that executes a single spell piece.
     */
    public class Action {

        public final SpellPiece piece;

        public Action(SpellPiece piece) {
            this.piece = piece;
        }

        /**
         * Execute this action's piece.
         */
        public void execute(SpellContext context) throws SpellRuntimeException {
            // Execute the piece
            Object result = piece.execute(context);

            // Store result in evaluated objects grid if piece has a return value
            Class<?> evaluationType = piece.getEvaluationType();
            if (evaluationType != null && evaluationType != Void.class) {
                context.evaluatedObjects[piece.x][piece.y] = result;
            }
        }
    }
}
