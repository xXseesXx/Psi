/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The 1.7.10 adaptation of the normal Psi spell compiler. The public
 * result/exception contract remains native to this platform; compilation
 * order and validation rules mirror the 1.21.1 compiler.
 */
public final class SpellCompiler implements ISpellCompiler {

    private final Set<SpellPiece> redirectionPieces = new HashSet<SpellPiece>();
    private CompiledSpell compiled;

    @Override
    public CompiledSpell compile(Spell spell) throws SpellCompilationException {
        return doCompile(spell);
    }

    public CompiledSpell doCompile(Spell spell) throws SpellCompilationException {
        if (spell == null || spell.grid == null) throw new SpellCompilationException(SpellCompilationException.NO_SPELL);
        redirectionPieces.clear();
        compiled = new CompiledSpell(spell);

        for (SpellPiece piece : findPieces(EnumPieceType.ERROR_HANDLER)) buildHandler(piece);
        List<SpellPiece> tricks = findTricks();
        if (tricks.isEmpty()) throw new SpellCompilationException(SpellCompilationException.NO_TRICKS);
        for (SpellPiece trick : tricks) buildPiece(trick);

        if (compiled.metadata.getStat(EnumSpellStat.COST) < 0 || compiled.metadata.getStat(EnumSpellStat.POTENCY) < 0)
            throw new SpellCompilationException(SpellCompilationException.STAT_OVERFLOW);
        if (spell.name == null || spell.name.isEmpty()) throw new SpellCompilationException(SpellCompilationException.NO_NAME);
        return compiled;
    }

    public void buildPiece(SpellPiece piece) throws SpellCompilationException {
        buildPiece(piece, new HashSet<SpellPiece>());
    }

    public void buildPiece(SpellPiece piece, Set<SpellPiece> visited) throws SpellCompilationException {
        if (!visited.add(piece)) throw new SpellCompilationException(SpellCompilationException.INFINITE_LOOP, piece.x, piece.y);
        if (compiled.actionMap.containsKey(piece)) {
            CompiledSpell.Action action = compiled.actionMap.get(piece);
            compiled.actions.remove(action);
            compiled.actions.push(action);
        } else {
            CompiledSpell.Action action = compiled.new Action(piece);
            compiled.actions.push(action);
            compiled.actionMap.put(piece, action);
            piece.addToMetadata(compiled.metadata);
        }

        CompiledSpell.CatchHandler handler = compiled.errorHandlers.get(piece);
        if (handler != null) buildPiece(handler.handlerPiece, new HashSet<SpellPiece>(visited));

        EnumSet<SpellParam.Side> usedSides = EnumSet.noneOf(SpellParam.Side.class);
        Set<SpellPiece> params = new HashSet<SpellPiece>();
        Set<SpellPiece> handledErrors = new HashSet<SpellPiece>();
        for (SpellParam<?> param : piece.paramSides.keySet()) {
            if (checkSideDisabled(param, piece, usedSides)) continue;
            SpellPiece pieceAt = getRedirectedPiece(piece, piece.paramSides.get(param));
            if (pieceAt == null) throw new SpellCompilationException(SpellCompilationException.NULL_PARAM, piece.x, piece.y);
            if (!param.canAccept(pieceAt)) throw new SpellCompilationException(SpellCompilationException.INVALID_PARAM, piece.x, piece.y);
            if (piece instanceof IErrorCatcher && ((IErrorCatcher) piece).catchParam(param)) handledErrors.add(pieceAt);
            else params.add(pieceAt);
        }
        for (SpellPiece pieceAt : params) {
            Set<SpellPiece> visitedCopy = new HashSet<SpellPiece>(visited);
            visitedCopy.addAll(handledErrors);
            buildPiece(pieceAt, visitedCopy);
        }
    }

    public void buildHandler(SpellPiece piece) throws SpellCompilationException {
        if (!(piece instanceof IErrorCatcher)) return;
        IErrorCatcher errorCatcher = (IErrorCatcher) piece;
        CompiledSpell.CatchHandler handler = new CompiledSpell.CatchHandler(piece);
        EnumSet<SpellParam.Side> usedSides = EnumSet.noneOf(SpellParam.Side.class);
        for (SpellParam<?> param : piece.paramSides.keySet()) {
            if (!errorCatcher.catchParam(param) || checkSideDisabled(param, piece, usedSides)) continue;
            SpellPiece pieceAt = getRedirectedPiece(piece, piece.paramSides.get(param));
            if (pieceAt == null) throw new SpellCompilationException(SpellCompilationException.NULL_PARAM, piece.x, piece.y);
            if (!param.canAccept(pieceAt)) throw new SpellCompilationException(SpellCompilationException.INVALID_PARAM, piece.x, piece.y);
            compiled.errorHandlers.put(pieceAt, handler);
        }
    }

    private SpellPiece getRedirectedPiece(SpellPiece piece, SpellParam.Side side) throws SpellCompilationException {
        return compiled.sourceSpell.grid.getPieceAtSideWithRedirections(piece.x, piece.y, side,
            new SpellGrid.SpellPieceConsumer() {
                @Override public void accept(SpellPiece redirector) throws SpellCompilationException { buildRedirect(redirector); }
            });
    }

    public void buildRedirect(SpellPiece piece) throws SpellCompilationException {
        if (!redirectionPieces.add(piece)) return;
        piece.addToMetadata(compiled.metadata);
        EnumSet<SpellParam.Side> usedSides = EnumSet.noneOf(SpellParam.Side.class);
        for (SpellParam<?> param : piece.paramSides.keySet()) checkSideDisabled(param, piece, usedSides);
    }

    private boolean checkSideDisabled(SpellParam<?> param, SpellPiece parent, EnumSet<SpellParam.Side> seen)
        throws SpellCompilationException {
        SpellParam.Side side = parent.paramSides.get(param);
        if (side.isEnabled()) {
            if (!seen.add(side)) throw new SpellCompilationException(SpellCompilationException.SAME_SIDE_PARAMS, parent.x, parent.y);
            return false;
        }
        if (!param.canDisable) throw new SpellCompilationException(SpellCompilationException.UNSET_PARAM, parent.x, parent.y);
        return true;
    }

    private List<SpellPiece> findTricks() { return findPieces(null); }

    private List<SpellPiece> findPieces(EnumPieceType type) {
        List<SpellPiece> results = new LinkedList<SpellPiece>();
        for (int i = 0; i < SpellGrid.GRID_SIZE; i++) for (int j = 0; j < SpellGrid.GRID_SIZE; j++) {
            SpellPiece piece = compiled.sourceSpell.grid.gridData[j][i];
            if (piece != null && (type == null ? piece.getPieceType().isTrick() : piece.getPieceType() == type)) results.add(0, piece);
        }
        return results;
    }
}
