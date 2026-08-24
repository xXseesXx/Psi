/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamAny;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.piece.PieceTrick;

/**
 * Trick: Debug
 * Sends a debug message to the player's chat.
 * 
 * 1.7.10 Backport: Uses ChatComponentText instead of modern Component API.
 */
public class PieceTrickDebug extends PieceTrick {

    public SpellParam<SpellParam.Any> target;
    public SpellParam<Double> number;

    public PieceTrickDebug(Spell spell) {
        super(spell);
        // Debug trick has no complexity or projection cost
        setStatLabel(EnumSpellStat.COMPLEXITY, null);
        setStatLabel(EnumSpellStat.PROJECTION, null);
    }

    @Override
    public void initParams() {
        addParam(target = new ParamAny(SpellParam.GENERIC_NAME_TARGET, SpellParam.BLUE, false));
        addParam(number = new ParamNumber(SpellParam.GENERIC_NAME_NUMBER, SpellParam.RED, true, false));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) {
        // Debug trick doesn't add to metadata (free to use)
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Double numberVal = this.getParamValue(context, number);
        Object targetVal = getParamValue(context, target);

        // Build the message
        String message = String.valueOf(targetVal);

        // If number parameter is provided, prefix it
        if (numberVal != null) {
            String numStr = "" + numberVal;
            // Format as integer if it's a whole number
            if (numberVal - numberVal.intValue() == 0) {
                int numInt = numberVal.intValue();
                numStr = "" + numInt;
            }

            message = EnumChatFormatting.AQUA + "[" + numStr + "] " + EnumChatFormatting.RESET + message;
        }

        // Send to player's chat
        context.caster.addChatMessage(new ChatComponentText(message));

        return null;
    }

}
