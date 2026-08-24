/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Barebones spell compiler for 1.7.10 backport.
 * Compiles a Spell's grid into a CompiledSpell by:
 * 1. Finding all Trick pieces (entry points)
 * 2. Recursively traversing parameters
 * 3. Building execution order
 * 4. Validating connections
 *
 * Simplified from modern Psi - no error handlers, redirectors, or advanced features yet.
 */
public class SpellCompiler {

    private CompiledSpell compiled;
    private Set<SpellPiece> visited;

    /**
     * Compile a spell from its grid structure.
     *
     * @param spell The spell to compile
     * @return The compiled spell ready for execution
     * @throws SpellCompilationException if compilation fails
     */
    public CompiledSpell compile(Spell spell) throws SpellCompilationException {
        if (spell == null) {
            throw new SpellCompilationException(SpellCompilationException.NO_SPELL);
        }

        if (spell.grid == null) {
            throw new SpellCompilationException(SpellCompilationException.NO_SPELL);
        }

        // Initialize compilation state
        compiled = new CompiledSpell(spell);
        visited = new HashSet<>();

        // Find all trick pieces (entry points)
        List<SpellPiece> tricks = findTricks(spell);
        if (tricks.isEmpty()) {
            throw new SpellCompilationException(SpellCompilationException.NO_TRICKS);
        }

        // Compile each trick piece (this recursively compiles dependencies)
        for (SpellPiece trick : tricks) {
            compilePiece(trick);
        }

        // Validate final spell
        if (compiled.metadata.getStat(EnumSpellStat.COST) < 0 || compiled.metadata.getStat(EnumSpellStat.POTENCY) < 0) {
            throw new SpellCompilationException(SpellCompilationException.STAT_OVERFLOW);
        }

        if (spell.name == null || spell.name.isEmpty()) {
            throw new SpellCompilationException(SpellCompilationException.NO_NAME);
        }

        return compiled;
    }

    /**
     * Compile a single piece and all its dependencies recursively.
     * Uses depth-first traversal to ensure dependencies are compiled before dependents.
     */
    private void compilePiece(SpellPiece piece) throws SpellCompilationException {
        // Check for infinite loops
        if (!visited.add(piece)) {
            throw new SpellCompilationException(SpellCompilationException.INFINITE_LOOP, piece.x, piece.y);
        }

        // If already compiled, move to top of execution stack
        if (compiled.actionMap.containsKey(piece)) {
            CompiledSpell.Action action = compiled.actionMap.get(piece);
            compiled.actions.remove(action);
            compiled.actions.add(action);
            return;
        }

        // Add piece stats to metadata
        piece.addToMetadata(compiled.metadata);

        // Compile all parameter dependencies first (depth-first)
        for (Map.Entry<SpellParam<?>, SpellParam.Side> entry : piece.paramSides.entrySet()) {
            SpellParam<?> param = entry.getKey();
            SpellParam.Side side = entry.getValue();

            // Check if parameter is disabled (optional parameter not set)
            if (!side.isEnabled()) {
                if (!param.canDisable) {
                    throw new SpellCompilationException(SpellCompilationException.UNSET_PARAM, piece.x, piece.y);
                }
                continue;
            }

            // Get piece at this side
            SpellPiece paramPiece = getPieceAtSide(piece.x, piece.y, side);

            if (paramPiece == null) {
                throw new SpellCompilationException(SpellCompilationException.NULL_PARAM, piece.x, piece.y);
            }

            // Validate parameter type
            if (!param.canAccept(paramPiece)) {
                throw new SpellCompilationException(SpellCompilationException.INVALID_PARAM, piece.x, piece.y);
            }

            // Recursively compile the parameter piece
            compilePiece(paramPiece);
        }

        // Create action for this piece and add to execution list
        CompiledSpell.Action action = compiled.new Action(piece);
        compiled.actions.add(action);
        compiled.actionMap.put(piece, action);
    }

    /**
     * Get the piece at a specific side of a grid position.
     * Simplified - no redirector support yet.
     */
    private SpellPiece getPieceAtSide(int x, int y, SpellParam.Side side) {
        int targetX = x + side.offx;
        int targetY = y + side.offy;

        if (!SpellGrid.exists(targetX, targetY)) {
            return null;
        }

        return compiled.sourceSpell.grid.gridData[targetX][targetY];
    }

    /**
     * Find all trick pieces in the spell grid.
     * Tricks are entry points for spell execution.
     */
    private List<SpellPiece> findTricks(Spell spell) {
        List<SpellPiece> tricks = new LinkedList<>();

        for (int x = 0; x < SpellGrid.GRID_SIZE; x++) {
            for (int y = 0; y < SpellGrid.GRID_SIZE; y++) {
                SpellPiece piece = spell.grid.gridData[x][y];
                if (piece != null && piece instanceof PieceTrick) {
                    tricks.add(0, piece); // Add to front for reverse execution order
                }
            }
        }

        return tricks;
    }
}
