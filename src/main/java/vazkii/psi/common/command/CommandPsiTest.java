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

import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
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
        } else if (subcommand.equalsIgnoreCase("projectile")) {
            executeProjectileTest(sender, args);
        } else if (subcommand.equalsIgnoreCase("givecad")) {
            executeGiveCAD(sender, args);
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown subcommand: " + subcommand));
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.YELLOW + "Available: debug, math, break, explode, projectile, givecad"));
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

            // Create the debug trick
            PieceTrickDebug debugTrick = new PieceTrickDebug(spell);
            debugTrick.x = 1;
            debugTrick.y = 0;
            debugTrick.isInGrid = true;

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
            // Create spell with proper grid: [Const:num1] [Const:num2]
            // ↓ ↓
            // [Operator:Sum] → [Trick:Debug]
            Spell spell = new Spell();
            spell.name = "Math Test";

            // Create constant pieces for the two numbers
            PieceConstantNumber const1 = new PieceConstantNumber(spell);
            const1.constant = value1;
            const1.x = 0;
            const1.y = 0;
            const1.isInGrid = true;

            PieceConstantNumber const2 = new PieceConstantNumber(spell);
            const2.constant = value2;
            const2.x = 1;
            const2.y = 1;
            const2.isInGrid = true;

            // Create sum operator
            PieceOperatorSum sumOperator = new PieceOperatorSum(spell);
            sumOperator.x = 1;
            sumOperator.y = 0;
            sumOperator.isInGrid = true;
            // Link parameters: num1 from LEFT (0,0), num2 from BOTTOM (1,1)
            sumOperator.setParamSide(sumOperator.num1, SpellParam.Side.LEFT);
            sumOperator.setParamSide(sumOperator.num2, SpellParam.Side.BOTTOM);

            // Create debug trick to display result
            PieceTrickDebug debugTrick = new PieceTrickDebug(spell);
            debugTrick.x = 2;
            debugTrick.y = 0;
            debugTrick.isInGrid = true;
            // Link to sum result from LEFT
            debugTrick.setParamSide(debugTrick.target, SpellParam.Side.LEFT);

            // Place pieces in grid
            spell.grid.gridData[0][0] = const1;
            spell.grid.gridData[1][1] = const2;
            spell.grid.gridData[1][0] = sumOperator;
            spell.grid.gridData[2][0] = debugTrick;

            // Compile and execute
            SpellCompiler compiler = new SpellCompiler();
            CompiledSpell compiled = compiler.compile(spell);

            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            compiled.execute(context);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "Math spell executed successfully"));

        } catch (SpellCompilationException e) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "[Psi Compilation Error] " + e.getMessage()));
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
            // Create spell with proper grid: [Selector:Raycast] → [Trick:BreakBlock]
            Spell spell = new Spell();
            spell.name = "Break Test";

            // Create constant for max distance
            PieceConstantNumber maxDistConst = new PieceConstantNumber(spell);
            maxDistConst.constant = 32.0;
            maxDistConst.x = 0;
            maxDistConst.y = 0;
            maxDistConst.isInGrid = true;

            // Create raycast selector
            PieceSelectorRaycast raycast = new PieceSelectorRaycast(spell);
            raycast.x = 1;
            raycast.y = 0;
            raycast.isInGrid = true;
            // Link maxDist from LEFT
            raycast.setParamSide(raycast.maxDist, SpellParam.Side.LEFT);

            // Create break trick
            PieceTrickBreakBlock breakTrick = new PieceTrickBreakBlock(spell);
            breakTrick.x = 2;
            breakTrick.y = 0;
            breakTrick.isInGrid = true;
            // Link position from LEFT (raycast result)
            breakTrick.setParamSide(breakTrick.position, SpellParam.Side.LEFT);

            // Place pieces in grid
            spell.grid.gridData[0][0] = maxDistConst;
            spell.grid.gridData[1][0] = raycast;
            spell.grid.gridData[2][0] = breakTrick;

            // Compile and execute
            SpellCompiler compiler = new SpellCompiler();
            CompiledSpell compiled = compiler.compile(spell);

            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            compiled.execute(context);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "Break spell executed successfully"));

        } catch (SpellCompilationException e) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "[Psi Compilation Error] " + e.getMessage()));
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

        try {
            // Create spell with proper grid: [Const:power] [Caster]
            // ↓ ↓
            // [Explode] ← [EntityPos]
            Spell spell = new Spell();
            spell.name = "Explode Test";

            // Grid layout:
            // [0,0] = powerConst [0,1] = casterSelector
            // [1,0] = explodeTrick [1,1] = entityPos
            //
            // Connections:
            // explodeTrick.position ← entityPos (BOTTOM)
            // explodeTrick.power ← powerConst (LEFT)
            // entityPos.target ← casterSelector (LEFT)

            PieceConstantNumber powerConst = new PieceConstantNumber(spell);
            powerConst.constant = 3.0; // TNT is 4.0
            powerConst.x = 0;
            powerConst.y = 0;
            powerConst.isInGrid = true;

            PieceSelectorCaster casterSelector = new PieceSelectorCaster(spell);
            casterSelector.x = 0;
            casterSelector.y = 1;
            casterSelector.isInGrid = true;

            PieceSelectorEntityPosition entityPos = new PieceSelectorEntityPosition(spell);
            entityPos.x = 1;
            entityPos.y = 1;
            entityPos.isInGrid = true;
            entityPos.setParamSide(entityPos.target, SpellParam.Side.LEFT);

            PieceTrickExplode explodeTrick = new PieceTrickExplode(spell);
            explodeTrick.x = 1;
            explodeTrick.y = 0;
            explodeTrick.isInGrid = true;
            explodeTrick.setParamSide(explodeTrick.position, SpellParam.Side.BOTTOM);
            explodeTrick.setParamSide(explodeTrick.power, SpellParam.Side.LEFT);

            // Place pieces in grid
            spell.grid.gridData[0][0] = powerConst;
            spell.grid.gridData[0][1] = casterSelector;
            spell.grid.gridData[1][1] = entityPos;
            spell.grid.gridData[1][0] = explodeTrick;

            // Compile and execute
            SpellCompiler compiler = new SpellCompiler();
            CompiledSpell compiled = compiler.compile(spell);

            SpellContext context = new SpellContext();
            context.setPlayer(player);
            context.setSpell(spell);

            compiled.execute(context);

            // Success message
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GREEN + "[Psi] Explode spell executed successfully"));

        } catch (SpellCompilationException e) {
            sender.addChatMessage(
                new ChatComponentText(EnumChatFormatting.RED + "[Psi Compilation Error] " + e.getMessage()));
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

    private void executeProjectileTest(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        try {
            // Create a simple break spell
            Spell spell = new Spell();
            spell.name = "Projectile Break Test";

            // Create constant for max distance
            PieceConstantNumber maxDistConst = new PieceConstantNumber(spell);
            maxDistConst.constant = 32.0;
            maxDistConst.x = 0;
            maxDistConst.y = 0;
            maxDistConst.isInGrid = true;

            // Create raycast selector
            PieceSelectorRaycast raycast = new PieceSelectorRaycast(spell);
            raycast.x = 1;
            raycast.y = 0;
            raycast.isInGrid = true;
            raycast.setParamSide(raycast.maxDist, SpellParam.Side.LEFT);

            // Create break trick
            PieceTrickBreakBlock breakTrick = new PieceTrickBreakBlock(spell);
            breakTrick.x = 2;
            breakTrick.y = 0;
            breakTrick.isInGrid = true;
            breakTrick.setParamSide(breakTrick.position, SpellParam.Side.LEFT);

            // Place pieces in grid
            spell.grid.gridData[0][0] = maxDistConst;
            spell.grid.gridData[1][0] = raycast;
            spell.grid.gridData[2][0] = breakTrick;

            // Create and shoot projectile
            vazkii.psi.common.entity.EntitySpellProjectile projectile = new vazkii.psi.common.entity.EntitySpellProjectile(
                player.worldObj,
                player,
                spell);

            // Set velocity based on player's look direction
            double velocity = 1.5; // Speed multiplier
            double mx = -Math.sin(Math.toRadians(player.rotationYaw)) * Math.cos(Math.toRadians(player.rotationPitch))
                * velocity;
            double my = -Math.sin(Math.toRadians(player.rotationPitch)) * velocity;
            double mz = Math.cos(Math.toRadians(player.rotationYaw)) * Math.cos(Math.toRadians(player.rotationPitch))
                * velocity;

            projectile.setThrowableHeading(mx, my, mz, 1.5F, 0.0F);

            // Spawn the projectile
            player.worldObj.spawnEntityInWorld(projectile);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] " + EnumChatFormatting.RESET + "Spell projectile shot!"));

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

    private void executeGiveCAD(ICommandSender sender, String[] args) {
        // Must be a player
        if (!(sender instanceof EntityPlayer)) {
            sender
                .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This command must be run by a player"));
            return;
        }

        EntityPlayer player = (EntityPlayer) sender;

        // Parse spell type (default: break)
        String spellType = args.length > 1 ? args[1] : "break";

        try {
            Spell spell;

            if (spellType.equalsIgnoreCase("break")) {
                // Create break spell
                spell = new Spell();
                spell.name = "Break Spell";

                PieceConstantNumber maxDistConst = new PieceConstantNumber(spell);
                maxDistConst.constant = 32.0;
                maxDistConst.x = 0;
                maxDistConst.y = 0;
                maxDistConst.isInGrid = true;

                PieceSelectorRaycast raycast = new PieceSelectorRaycast(spell);
                raycast.x = 1;
                raycast.y = 0;
                raycast.isInGrid = true;
                raycast.setParamSide(raycast.maxDist, SpellParam.Side.LEFT);

                PieceTrickBreakBlock breakTrick = new PieceTrickBreakBlock(spell);
                breakTrick.x = 2;
                breakTrick.y = 0;
                breakTrick.isInGrid = true;
                breakTrick.setParamSide(breakTrick.position, SpellParam.Side.LEFT);

                spell.grid.gridData[0][0] = maxDistConst;
                spell.grid.gridData[1][0] = raycast;
                spell.grid.gridData[2][0] = breakTrick;

            } else if (spellType.equalsIgnoreCase("explode")) {
                // Create explode spell
                spell = new Spell();
                spell.name = "Explode Spell";

                PieceConstantNumber powerConst = new PieceConstantNumber(spell);
                powerConst.constant = 3.0;
                powerConst.x = 0;
                powerConst.y = 0;
                powerConst.isInGrid = true;

                PieceSelectorCaster casterSelector = new PieceSelectorCaster(spell);
                casterSelector.x = 0;
                casterSelector.y = 1;
                casterSelector.isInGrid = true;

                PieceSelectorEntityPosition entityPos = new PieceSelectorEntityPosition(spell);
                entityPos.x = 1;
                entityPos.y = 1;
                entityPos.isInGrid = true;
                entityPos.setParamSide(entityPos.target, SpellParam.Side.LEFT);

                PieceTrickExplode explodeTrick = new PieceTrickExplode(spell);
                explodeTrick.x = 1;
                explodeTrick.y = 0;
                explodeTrick.isInGrid = true;
                explodeTrick.setParamSide(explodeTrick.position, SpellParam.Side.BOTTOM);
                explodeTrick.setParamSide(explodeTrick.power, SpellParam.Side.LEFT);

                spell.grid.gridData[0][0] = powerConst;
                spell.grid.gridData[0][1] = casterSelector;
                spell.grid.gridData[1][1] = entityPos;
                spell.grid.gridData[1][0] = explodeTrick;

            } else {
                sender
                    .addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Unknown spell type: " + spellType));
                sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Available: break, explode"));
                return;
            }

            // Create CAD item with spell
            net.minecraft.item.ItemStack cad = new net.minecraft.item.ItemStack(
                vazkii.psi.common.core.proxy.CommonProxy.itemCAD);
            vazkii.psi.common.item.ItemCAD.setSpell(cad, spell);

            // Give to player
            player.inventory.addItemStackToInventory(cad);

            // Success feedback
            sender.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "CAD with "
                        + spell.name
                        + " given!"));

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
            return getListOfStringsMatchingLastWord(args, "debug", "math", "break", "explode", "projectile", "givecad");
        }
        return null;
    }

}
