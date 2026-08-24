/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in GitHub:
 * https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.trick.PieceTrickDebug;

/**
 * Test command for executing barebones spells.
 * 
 * Usage: /psitest debug <message>
 * 
 * 1.7.10 Backport: Uses CommandBase and direct spell execution.
 */
public class CommandPsiTest extends CommandBase {

    @Override
    public String getCommandName() {
        return "psitest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/psitest debug <message>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Op level 2
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /psitest debug <message>"));
            return;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("debug")) {
            executeDebugTrick(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Available: debug"));
        }
    }

    private void executeDebugTrick(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        // Get message (everything after "debug")
        final String message;
        if (args.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) {
                    sb.append(" ");
                }
                sb.append(args[i]);
            }
            message = sb.toString();
        } else {
            message = "null";
        }

        try {
            // Create a simple spell with just PieceTrickDebug
            Spell spell = new Spell();
            spell.name = "Debug Test";

            PieceTrickDebug trick = new PieceTrickDebug(spell);
            trick.x = 0;
            trick.y = 0;
            trick.isInGrid = true;

            // Place in grid
            spell.grid[0][0] = trick;

            // Create context
            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            // For now, we'll execute the trick directly
            // In Phase 7, this will go through proper spell compilation/execution

            // Since we don't have parameter linking yet, we'll create a simple wrapper
            // that returns the message when getParamValue is called
            PieceTrickDebug hackTrick = new PieceTrickDebug(spell) {

                @Override
                public <T> T getParamValue(SpellContext context, vazkii.psi.api.spell.SpellParam<T> param)
                    throws SpellRuntimeException {
                    // Return the message for the target parameter
                    if (param == this.target) {
                        return (T) message;
                    }
                    // No number parameter
                    return null;
                }
            };

            hackTrick.execute(context);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] " + EnumChatFormatting.RESET + "Spell executed successfully"));

        } catch (SpellRuntimeException e) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[Psi Error] " + e.getMessage()));
        } catch (Exception e) {
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi Error] "
                        + e.getClass()
                            .getSimpleName()
                        + ": "
                        + e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "debug");
        }
        return null;
    }

}
