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

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.common.spell.constant.PieceConstantString;
import vazkii.psi.common.spell.operator.PieceOperatorSum;
import vazkii.psi.common.spell.selector.PieceSelectorRaycast;
import vazkii.psi.common.spell.trick.PieceTrickBreakBlock;
import vazkii.psi.common.spell.trick.PieceTrickDebug;
import vazkii.psi.common.spell.trick.PieceTrickExplode;

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
        return "/psitest <debug|math|break|explode> <args>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // Op level 2
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "Usage: /psitest <debug|math|break|explode> <args>"));
            return;
        }

        String subcommand = args[0];

        if (subcommand.equalsIgnoreCase("debug")) {
            executeDebugTrick(sender, args);
        } else if (subcommand.equalsIgnoreCase("math")) {
            executeMathTest(sender, args);
        } else if (subcommand.equalsIgnoreCase("break")) {
            executeBreakTest(sender, args);
        } else if (subcommand.equalsIgnoreCase("explode")) {
            executeExplodeTest(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand));
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.YELLOW + "Available: debug, math, break, explode"));
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
            // Create a spell with proper grid: [Constant: message] -> [Trick: Debug]
            Spell spell = new Spell();
            spell.name = "Debug Test";

            // Create a constant piece that holds the message
            PieceConstantString messageConstant = new PieceConstantString(spell);
            messageConstant.constant = message;
            messageConstant.x = 0;
            messageConstant.y = 0;
            messageConstant.isInGrid = true;
            messageConstant.initParams();

            // Create the debug trick
            PieceTrickDebug debugTrick = new PieceTrickDebug(spell);
            debugTrick.x = 1;
            debugTrick.y = 0;
            debugTrick.isInGrid = true;
            debugTrick.initParams();

            // Link the constant to the trick's target parameter (from the LEFT)
            debugTrick.setParamSide(debugTrick.target, SpellParam.Side.LEFT);

            // Place pieces in grid
            spell.grid.gridData[0][0] = messageConstant;
            spell.grid.gridData[1][0] = debugTrick;

            // Compile the spell
            SpellCompiler compiler = new SpellCompiler();
            CompiledSpell compiled = compiler.compile(spell);

            // Create context and execute
            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            compiled.execute(context);

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

    private void executeExplodeTest(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        // Create spell and context
        Spell spell = new Spell();
        SpellContext context = new SpellContext().setPlayer(player);

        try {
            // Get player's position directly - simpler than using entity selector chain
            final Vector3 playerPosition = new Vector3(player.posX, player.posY, player.posZ);

            // Create explosion at player position
            final double explosionPower = 3.0; // TNT is 4.0, this is slightly weaker

            PieceTrickExplode explodeTrick = new PieceTrickExplode(spell) {

                @Override
                public <T> T getParamValue(SpellContext ctx, vazkii.psi.api.spell.SpellParam<T> param) {
                    if (param == this.position) {
                        return (T) playerPosition;
                    }
                    if (param == this.power) {
                        return (T) (Double) explosionPower;
                    }
                    return null;
                }
            };
            explodeTrick.initParams();

            explodeTrick.execute(context);

            // Success message
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GREEN + "[Psi] Explode spell executed successfully"));
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GRAY + "Explosion position: "
                        + String.format(
                            "Vector3{x=%.1f, y=%.1f, z=%.1f}",
                            playerPosition.x,
                            playerPosition.y,
                            playerPosition.z)));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "Power: " + explosionPower));

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
            return getListOfStringsMatchingLastWord(args, "debug", "math", "break", "explode");
        }
        return null;
    }

}
