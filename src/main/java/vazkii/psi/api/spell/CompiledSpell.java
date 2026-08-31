package vazkii.psi.api.spell;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.IPlayerData;
import vazkii.psi.common.core.handler.SpellExecutionHandler;

/** A compiled, reusable spell. Execution ordering and error handling mirror modern Psi. */
public class CompiledSpell {
    public final Spell sourceSpell;
    public final SpellMetadata metadata = new SpellMetadata();
    public final Stack<Action> actions = new Stack<Action>();
    public final Map<SpellPiece, CatchHandler> errorHandlers = new HashMap<SpellPiece, CatchHandler>();
    public final Map<SpellPiece, Action> actionMap = new HashMap<SpellPiece, Action>();
    public final boolean[][] spotsEvaluated;
    public Action currentAction;

    public CompiledSpell(Spell source) {
        sourceSpell = source;
        metadata.setStat(EnumSpellStat.BANDWIDTH, source.grid.getSize());
        spotsEvaluated = new boolean[SpellGrid.GRID_SIZE][SpellGrid.GRID_SIZE];
    }

    public boolean execute(SpellContext context) throws SpellRuntimeException {
        IPlayerData data = PsiAPI.internalHandler.getDataForPlayer(context.caster);
        while (!context.actions.isEmpty()) {
            Action action = context.actions.pop();
            currentAction = action;
            PsiAPI.internalHandler.setCrashData(this, action.piece);
            action.execute(data, context);
            PsiAPI.internalHandler.setCrashData(null, null);
            currentAction = null;
            if (context.stopped) return false;
            if (context.delay > 0) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void safeExecute(SpellContext context) {
        if (context.caster == null || context.caster.worldObj.isRemote) return;
        try {
            if (context.actions == null) context.actions = (Stack<Action>) actions.clone();
            if (context.cspell.execute(context)) SpellExecutionHandler.delayContext(context);
        } catch (SpellRuntimeException e) {
            if (!context.shouldSuppressErrors() && context.caster instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) context.caster;
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + e.getTranslatedMessage()));
                if (context.cspell.currentAction != null) {
                    SpellPiece piece = context.cspell.currentAction.piece;
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "(" + (piece.x + 1) + ", " + (piece.y + 1) + ")"));
                }
            }
        }
    }

    public static class CatchHandler {
        public final SpellPiece handlerPiece;
        public final IErrorCatcher handler;
        public CatchHandler(SpellPiece handlerPiece) {
            this.handlerPiece = handlerPiece;
            handler = (IErrorCatcher) handlerPiece;
        }
        public boolean suppress(SpellPiece piece, SpellContext context, SpellRuntimeException exception) {
            boolean handled = handler.catchException(piece, context, exception);
            if (handled && piece.getEvaluationType() != null && piece.getEvaluationType() != Void.class)
                context.evaluatedObjects[piece.x][piece.y] = handler.supplyReplacementValue(piece, context, exception);
            return handled;
        }
    }

    public class Action {
        public final SpellPiece piece;
        public Action(SpellPiece piece) { this.piece = piece; }
        public void execute(IPlayerData data, SpellContext context) throws SpellRuntimeException {
            try {
                data.markPieceExecuted(piece);
                Object result = piece.execute(context);
                Class<?> eval = piece.getEvaluationType();
                if (eval != null && eval != Void.class) context.evaluatedObjects[piece.x][piece.y] = result;
            } catch (SpellRuntimeException exception) {
                CatchHandler handler = errorHandlers.get(piece);
                if (handler == null || !handler.suppress(piece, context, exception)) throw exception;
            }
        }
    }
}
