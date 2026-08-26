package vazkii.psi.common.item;

import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.psi.api.cad.ICADColorizer;
import vazkii.psi.api.spell.CompiledSpell;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellCompiler;
import vazkii.psi.common.Psi;
import vazkii.psi.common.core.handler.GuiHandler;
import vazkii.psi.common.core.handler.PlayerPsiHandler;
import vazkii.psi.common.core.proxy.CommonProxy;
import vazkii.psi.common.item.component.ItemCADComponent;

/**
 * CAD (Computer-Aided Design) Assembly - stores and casts spells as projectiles.
 *
 * Right-click to shoot spell projectile.
 * Stores spell in NBT.
 */
public class ItemCAD extends Item {

    private static final String TAG_SPELL = "spell";
    private static final String TAG_ASSEMBLY = "cadAssembly";
    private static final String TAG_CORE = "cadCore";
    private static final String TAG_SOCKET = "cadSocket";
    private static final String TAG_BATTERY = "cadBattery";
    private static final String TAG_COLORIZER = "cadColorizer";
    private static final String TAG_BULLETS = "bullets";
    private static final String TAG_SELECTED_SLOT = "selectedSlot";
    private static final String TAG_STORED_PSI = "storedPsi";
    public static final int MAX_MAGAZINE_SIZE = 12;
    public static final String STAT_MEMORY = "Memory";

    private IIcon defaultIcon;
    private IIcon ironIcon, goldIcon, psimetalIcon, ebonyIcon, ivoryIcon, creativeIcon;

    public ItemCAD() {
        setMaxStackSize(1);
        setMaxDamage(0); // No durability for now
    }

    /** Creates a CAD and records the parts used by the CAD Assembler. */
    public static ItemStack createCAD(ItemStack assembly, ItemStack core, ItemStack socket, ItemStack battery) {
        return createCAD(assembly, core, socket, battery, null);
    }

    public static ItemStack createCAD(ItemStack assembly, ItemStack core, ItemStack socket, ItemStack battery,
        ItemStack colorizer) {
        ItemStack cad = new ItemStack(CommonProxy.itemCAD);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(TAG_ASSEMBLY, componentName(assembly));
        tag.setString(TAG_CORE, componentName(core));
        tag.setString(TAG_SOCKET, componentName(socket));
        tag.setString(TAG_BATTERY, componentName(battery));
        cad.setTagCompound(tag);
        setColorizer(cad, colorizer);
        return cad;
    }

    public static ItemStack getColorizer(ItemStack cad) {
        if (cad == null || !cad.hasTagCompound()
            || !cad.getTagCompound()
                .hasKey(TAG_COLORIZER))
            return null;
        ItemStack colorizer = ItemStack.loadItemStackFromNBT(
            cad.getTagCompound()
                .getCompoundTag(TAG_COLORIZER));
        return colorizer != null && colorizer.getItem() instanceof ICADColorizer ? colorizer : null;
    }

    public static void setColorizer(ItemStack cad, ItemStack colorizer) {
        if (cad == null) return;
        if (!cad.hasTagCompound()) cad.setTagCompound(new NBTTagCompound());
        if (colorizer == null || colorizer.getItem() == null) {
            cad.getTagCompound()
                .removeTag(TAG_COLORIZER);
            return;
        }
        ItemStack stored = colorizer.copy();
        stored.stackSize = 1;
        cad.getTagCompound()
            .setTag(TAG_COLORIZER, stored.writeToNBT(new NBTTagCompound()));
    }

    public static int getSpellColor(ItemStack cad) {
        return Psi.proxy.getColorForColorizer(getColorizer(cad));
    }

    /** Colourizer installed in the CAD currently held by the player, if any. */
    public static ItemStack getHeldColorizer(EntityPlayer player) {
        if (player == null) return null;
        ItemStack held = player.getHeldItem();
        return held != null && held.getItem() instanceof ItemCAD ? getColorizer(held) : null;
    }

    /** Returns a component stat from the part that supplies it, or zero when that part is absent. */
    public static int getStat(ItemStack cad, String stat) {
        String componentKey;
        if ("Efficiency".equals(stat) || "Potency".equals(stat)) componentKey = TAG_ASSEMBLY;
        else if ("Complexity".equals(stat) || "Projection".equals(stat)) componentKey = TAG_CORE;
        else if ("Bandwidth".equals(stat) || "Sockets".equals(stat) || STAT_MEMORY.equals(stat))
            componentKey = TAG_SOCKET;
        else if ("Overflow".equals(stat)) componentKey = TAG_BATTERY;
        else return 0;
        ItemCADComponent part = component(cad, componentKey);
        Integer value = part == null ? null
            : part.getStats()
                .get(stat);
        return value == null ? 0 : value.intValue();
    }

    public static int getMagazineSize(ItemStack cad) {
        int sockets = getStat(cad, "Sockets");
        return sockets < 0 ? MAX_MAGAZINE_SIZE : Math.min(MAX_MAGAZINE_SIZE, Math.max(0, sockets));
    }

    public static int getStoredPsi(ItemStack cad) {
        if (cad == null) return 0;
        int capacity = getStat(cad, "Overflow");
        if (capacity < 0) return -1;
        return cad.hasTagCompound() && cad.getTagCompound()
            .hasKey(TAG_STORED_PSI) ? Math.min(
                capacity,
                cad.getTagCompound()
                    .getInteger(TAG_STORED_PSI))
                : 0;
    }

    public static int consumeStoredPsi(ItemStack cad, int amount) {
        int stored = getStoredPsi(cad);
        if (stored < 0) return 0;
        int used = Math.min(stored, amount);
        if (!cad.hasTagCompound()) cad.setTagCompound(new NBTTagCompound());
        cad.getTagCompound()
            .setInteger(TAG_STORED_PSI, stored - used);
        return amount - used;
    }

    public static void regenStoredPsi(ItemStack cad, int amount) {
        int capacity = getStat(cad, "Overflow");
        if (capacity < 0) return;
        if (!cad.hasTagCompound()) cad.setTagCompound(new NBTTagCompound());
        cad.getTagCompound()
            .setInteger(TAG_STORED_PSI, Math.min(capacity, getStoredPsi(cad) + amount));
    }

    public static ItemStack getBullet(ItemStack cad, int slot) {
        if (cad == null || slot < 0 || slot >= getMagazineSize(cad) || !cad.hasTagCompound()) return null;
        NBTTagList list = cad.getTagCompound()
            .getTagList(TAG_BULLETS, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            if ((tag.getByte("Slot") & 255) == slot) return ItemStack.loadItemStackFromNBT(tag);
        }
        return null;
    }

    public static void setBullet(ItemStack cad, int slot, ItemStack bullet) {
        if (cad == null || slot < 0 || slot >= getMagazineSize(cad)) return;
        if (!cad.hasTagCompound()) cad.setTagCompound(new NBTTagCompound());
        NBTTagList result = new NBTTagList();
        for (int i = 0; i < getMagazineSize(cad); i++) {
            ItemStack current = i == slot ? bullet : getBullet(cad, i);
            if (current != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                current.writeToNBT(tag);
                result.appendTag(tag);
            }
        }
        cad.getTagCompound()
            .setTag(TAG_BULLETS, result);
    }

    public static int getSelectedSlot(ItemStack cad) {
        int size = getMagazineSize(cad);
        return size == 0 || cad == null || !cad.hasTagCompound() ? 0
            : Math.max(
                0,
                Math.min(
                    size - 1,
                    cad.getTagCompound()
                        .getInteger(TAG_SELECTED_SLOT)));
    }

    /** Returns the installed Assembly id used by the client CAD model renderer. */
    public static String getAssemblyId(ItemStack cad) {
        return cad != null && cad.hasTagCompound() ? cad.getTagCompound()
            .getString(TAG_ASSEMBLY) : "cad_assembly_iron";
    }

    public static void setSelectedSlot(ItemStack cad, int slot) {
        int size = getMagazineSize(cad);
        if (cad == null || size == 0) return;
        if (!cad.hasTagCompound()) cad.setTagCompound(new NBTTagCompound());
        cad.getTagCompound()
            .setInteger(TAG_SELECTED_SLOT, Math.max(0, Math.min(size - 1, slot)));
    }

    private static boolean canCast(ItemStack cad, Spell spell) {
        try {
            CompiledSpell compiled = new SpellCompiler().compile(spell);
            return statAllows(cad, "Complexity", compiled.metadata.getStat(EnumSpellStat.COMPLEXITY))
                && statAllows(cad, "Potency", compiled.metadata.getStat(EnumSpellStat.POTENCY))
                && statAllows(cad, "Projection", compiled.metadata.getStat(EnumSpellStat.PROJECTION))
                && statAllows(cad, "Bandwidth", compiled.metadata.getStat(EnumSpellStat.BANDWIDTH));
        } catch (SpellCompilationException e) {
            return false;
        }
    }

    private static boolean statAllows(ItemStack cad, String stat, int required) {
        int available = getStat(cad, stat);
        return available == -1 || required <= available;
    }

    private static String componentName(ItemStack stack) {
        return stack == null ? ""
            : stack.getUnlocalizedName()
                .replace("item.psi.", "");
    }

    private static String componentDisplayName(ItemStack stack, String key) {
        if (stack == null || !stack.hasTagCompound()) return "";
        String component = stack.getTagCompound()
            .getString(key);
        return component.isEmpty() ? "" : StatCollector.translateToLocal("item.psi." + component + ".name");
    }

    private static ItemCADComponent component(ItemStack stack, String key) {
        if (stack == null || !stack.hasTagCompound()) return null;
        Item item = GameRegistry.findItem(
            "psi",
            stack.getTagCompound()
                .getString(key));
        return item instanceof ItemCADComponent ? (ItemCADComponent) item : null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addComponentTooltip(ItemStack stack, List tooltip, String key, String componentType) {
        String name = componentDisplayName(stack, key);
        tooltip.add(
            EnumChatFormatting.GREEN + componentType
                + EnumChatFormatting.GRAY
                + ": "
                + (name.isEmpty() ? "None" : name));
        ItemCADComponent component = component(stack, key);
        if (component != null) for (Map.Entry<String, Integer> stat : component.getStats()
            .entrySet()) {
                String value = stat.getValue()
                    .intValue() == -1 ? "Infinity"
                        : stat.getValue()
                            .toString();
                tooltip.add(" " + EnumChatFormatting.AQUA + stat.getKey() + EnumChatFormatting.GRAY + ": " + value);
            }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String assembly = stack != null && stack.hasTagCompound() ? stack.getTagCompound()
            .getString(TAG_ASSEMBLY) : "";
        if (!assembly.isEmpty()) return StatCollector.translateToLocal("item.psi." + assembly + ".name")
            .replace(" Assembly", "");
        return super.getItemStackDisplayName(stack);
    }

    @Override
    public void registerIcons(IIconRegister register) {
        defaultIcon = register.registerIcon("psi:cad_assembly_iron");
        ironIcon = defaultIcon;
        goldIcon = register.registerIcon("psi:cad_assembly_gold");
        psimetalIcon = register.registerIcon("psi:cad_assembly_psimetal");
        ebonyIcon = register.registerIcon("psi:cad_assembly_ebony_psimetal");
        ivoryIcon = register.registerIcon("psi:cad_assembly_ivory_psimetal");
        creativeIcon = register.registerIcon("psi:cad_assembly_creative");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        // This is the 1.7.10 counterpart to ModelCAD in modern Psi: the finished
        // CAD's visual is determined by its Assembly component, not its other
        // installed parts. RenderItemCAD gives this selected silhouette depth in
        // hand and as an entity; GUIs intentionally use the same icon directly.
        if (stack == null || !stack.hasTagCompound()) return defaultIcon;
        String assembly = getAssemblyId(stack);
        if ("cad_assembly_gold".equals(assembly)) return goldIcon;
        if ("cad_assembly_psimetal".equals(assembly)) return psimetalIcon;
        if ("cad_assembly_ebony_psimetal".equals(assembly)) return ebonyIcon;
        if ("cad_assembly_ivory_psimetal".equals(assembly)) return ivoryIcon;
        if ("cad_assembly_creative".equals(assembly)) return creativeIcon;
        return ironIcon;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, CreativeTabs tab, List subItems) {
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyIron),
                new ItemStack(CommonProxy.itemCADCoreBasic),
                new ItemStack(CommonProxy.itemCADSocketBasic),
                new ItemStack(CommonProxy.itemCADBatteryBasic)));
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyGold),
                new ItemStack(CommonProxy.itemCADCoreBasic),
                new ItemStack(CommonProxy.itemCADSocketBasic),
                new ItemStack(CommonProxy.itemCADBatteryBasic)));
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyPsimetal),
                new ItemStack(CommonProxy.itemCADCoreOverclocked),
                new ItemStack(CommonProxy.itemCADSocketSignaling),
                new ItemStack(CommonProxy.itemCADBatteryExtended)));
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyEbonyPsimetal),
                new ItemStack(CommonProxy.itemCADCoreHyperclocked),
                new ItemStack(CommonProxy.itemCADSocketTransmissive),
                new ItemStack(CommonProxy.itemCADBatteryUltradense)));
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyIvoryPsimetal),
                new ItemStack(CommonProxy.itemCADCoreHyperclocked),
                new ItemStack(CommonProxy.itemCADSocketTransmissive),
                new ItemStack(CommonProxy.itemCADBatteryUltradense)));
        subItems.add(
            createCAD(
                new ItemStack(CommonProxy.itemCADAssemblyCreative),
                new ItemStack(CommonProxy.itemCADCoreHyperclocked),
                new ItemStack(CommonProxy.itemCADSocketHuge),
                new ItemStack(CommonProxy.itemCADBatteryUltradense)));
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Shift+Right-Click: Open spell programmer GUI
        if (player.isSneaking()) {
            if (world.isRemote) {
                // Open GUI only on client side
                player.openGui(Psi.instance, GuiHandler.GUI_SPELL_PROGRAMMER, world, 0, 0, 0);
            }
            return stack;
        }

        // Don't execute spell casting on client
        if (world.isRemote) {
            return stack;
        }

        // Normal Right-Click: Cast spell
        ItemStack bullet = getBullet(stack, getSelectedSlot(stack));
        Spell spell = ItemSpellBullet.getSpell(bullet);
        if (spell == null) spell = getSpell(stack);

        if (spell == null) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi] "
                        + EnumChatFormatting.RESET
                        + "No spell programmed into this CAD"));
            return stack;
        }

        if (!canCast(stack, spell)) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi] Your CAD's stats are too weak to cast this spell."));
            return stack;
        }

        try {
            int cost = getRealCost(stack, bullet, spell);
            if (!PlayerPsiHandler.spend(player, cost, stack)) {
                player.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "[Psi] Not enough Psi to cast this spell."));
                return stack;
            }
            if (bullet != null && bullet.getItem() instanceof ItemSpellBullet) {
                ((ItemSpellBullet) bullet.getItem()).castSpell(bullet, player, getColorizer(stack));
            } else {
                // A spell stored directly on a legacy CAD is a basic bullet.
                new ItemSpellBullet().castSpell(stack, player);
            }

            // Success feedback
            player.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GREEN + "[Psi] " + EnumChatFormatting.RESET + "Spell cast!"));

        } catch (Exception e) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[Psi Error] "
                        + e.getClass()
                            .getSimpleName()
                        + ": "
                        + e.getMessage()));
            e.printStackTrace();
        }

        return stack;
    }

    /** Applies the CAD efficiency and the selected bullet's modern cost multiplier. */
    public static int getRealCost(ItemStack cad, ItemStack bullet, Spell spell) throws SpellCompilationException {
        int raw = new SpellCompiler().compile(spell).metadata.getStat(EnumSpellStat.COST);
        int efficiency = getStat(cad, "Efficiency");
        if (efficiency == -1) return 0;
        double result = efficiency <= 0 ? raw : raw / (efficiency / 100D);
        if (bullet != null && bullet.getItem() instanceof ItemSpellBullet)
            result *= ((ItemSpellBullet) bullet.getItem()).getCostModifier();
        return (int) result;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        Spell spell = getSpell(stack);

        if (spell != null) {
            // Show spell name
            tooltip.add(EnumChatFormatting.AQUA + "Spell: " + EnumChatFormatting.RESET + spell.name);

            // TODO Phase 9+: Add more detailed spell info (cost, effects, etc.)
        } else {
            tooltip.add(EnumChatFormatting.GRAY + "No spell programmed");
            tooltip.add(EnumChatFormatting.DARK_GRAY + "Use a spell programmer to set");
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            String assembly = componentDisplayName(stack, TAG_ASSEMBLY);
            if (!assembly.isEmpty()) {
                addComponentTooltip(stack, tooltip, TAG_ASSEMBLY, "Assembly");
                addComponentTooltip(stack, tooltip, TAG_CORE, "Core");
                addComponentTooltip(stack, tooltip, TAG_SOCKET, "Socket");
                addComponentTooltip(stack, tooltip, TAG_BATTERY, "Battery");
                ItemStack colorizer = getColorizer(stack);
                tooltip.add(
                    EnumChatFormatting.GREEN + "Colorizer"
                        + EnumChatFormatting.GRAY
                        + ": "
                        + (colorizer == null ? "None" : colorizer.getDisplayName()));
                tooltip.add(
                    EnumChatFormatting.AQUA + "Magazine"
                        + EnumChatFormatting.GRAY
                        + ": "
                        + getMagazineSize(stack)
                        + " slots");
            }
        } else if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey(TAG_ASSEMBLY)) {
                    tooltip.add(
                        EnumChatFormatting.GRAY + "Hold "
                            + EnumChatFormatting.AQUA
                            + "SHIFT"
                            + EnumChatFormatting.GRAY
                            + " for more info");
                }
    }

    /**
     * Get the spell stored in this CAD item.
     */
    public static Spell getSpell(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) {
            return null;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey(TAG_SPELL)) {
            return null;
        }

        try {
            return Spell.readFromNBT(nbt.getCompoundTag(TAG_SPELL));
        } catch (Exception e) {
            System.err.println("[Psi] Failed to read spell from CAD: " + e.getMessage());
            return null;
        }
    }

    /**
     * Set the spell stored in this CAD item.
     */
    public static void setSpell(ItemStack stack, Spell spell) {
        if (stack == null) {
            return;
        }

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound spellNbt = new NBTTagCompound();
        spell.writeToNBT(spellNbt);
        nbt.setTag(TAG_SPELL, spellNbt);
    }
}
