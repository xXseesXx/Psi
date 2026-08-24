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
import vazkii.psi.common.spell.operator.PieceOperatorSum;
import vazkii.psi.common.spell.selector.PieceSelectorRaycast;
import vazkii.psi.common.spell.trick.PieceTrickBreakBlock;
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
        return "/psitest <debug|math|break> <args>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Op level 2
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "Usage: /psitest <debug|math|break> <args>"));
            return;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("debug")) {
            executeDebugTrick(sender, args);
        } else if (subcommand.equalsIgnoreCase("math")) {
            executeMathTest(sender, args);
        } else if (subcommand.equalsIgnoreCase("break")) {
            executeBreakTest(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Available: debug, math, break"));
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

    private void executeMathTest(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        // Parse numbers
        if (args.length < 3) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage: /psitest math <num1> <num2>"));
            return;
        }

        final double value1;
        final double value2;

        try {
            value1 = Double.parseDouble(args[1]);
            value2 = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "Invalid numbers. Use format: /psitest math 5 3"));
            return;
        }

        try {
            // Create spell with constants, sum operator, and debug trick
            Spell spell = new Spell();
            spell.name = "Math Test";

            // Create context
            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            // Create parameter override hack for barebones execution
            PieceOperatorSum hackSum = new PieceOperatorSum(spell) {

                @Override
                public <T> T getParamValue(SpellContext context, vazkii.psi.api.spell.SpellParam<T> param)
                    throws SpellRuntimeException {
                    if (param == this.num1) {
                        return (T) (Double) value1;
                    } else if (param == this.num2) {
                        return (T) (Double) value2;
                    }
                    return null;
                }
            };
            hackSum.initParams();

            // Execute sum
            final Object result = hackSum.execute(context);

            // Create debug trick wrapper to display result
            PieceTrickDebug hackDebug = new PieceTrickDebug(spell) {

                @Override
                public <T> T getParamValue(SpellContext context, vazkii.psi.api.spell.SpellParam<T> param)
                    throws SpellRuntimeException {
                    if (param == this.target) {
                        return (T) result;
                    }
                    return null;
                }
            };

            hackDebug.execute(context);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "Math spell executed successfully"));

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

    private void executeBreakTest(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        try {
            // Create spell with raycast selector and break trick
            Spell spell = new Spell();
            spell.name = "Break Test";

            // Create context
            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            // Create raycast selector with parameter override hack
            PieceSelectorRaycast hackRaycast = new PieceSelectorRaycast(spell) {

                @Override
                public <T> T getParamValue(SpellContext context, vazkii.psi.api.spell.SpellParam<T> param)
                    throws SpellRuntimeException {
                    // Use default max distance (32 blocks)
                    if (param == this.maxDist) {
                        return (T) (Double) 32.0;
                    }
                    return null;
                }
            };
            hackRaycast.initParams();

            // Execute raycast to get block position
            final Object blockPos = hackRaycast.execute(context);

            // Create break trick wrapper to use the raycast result
            PieceTrickBreakBlock hackBreak = new PieceTrickBreakBlock(spell) {

                @Override
                public <T> T getParamValue(SpellContext context, vazkii.psi.api.spell.SpellParam<T> param)
                    throws SpellRuntimeException {
                    if (param == this.position) {
                        return (T) blockPos;
                    }
                    return null;
                }
            };
            hackBreak.initParams();

            // Execute break
            hackBreak.execute(context);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "Break spell executed successfully"));
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GRAY + "Block position: " + blockPos.toString()));

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
            return getListOfStringsMatchingLastWord(args, "debug", "math", "break");
        }
        return null;
    }

}
