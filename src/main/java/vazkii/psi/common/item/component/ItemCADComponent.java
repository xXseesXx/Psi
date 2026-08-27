package vazkii.psi.common.item.component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import vazkii.psi.api.cad.EnumCADComponent;
import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICADComponent;

/** Shared base for the individual parts used to assemble a CAD. */
public abstract class ItemCADComponent extends Item implements ICADComponent {

    private final String componentType;
    private final Map<String, Integer> stats = new LinkedHashMap<String, Integer>();

    protected ItemCADComponent(String componentType) {
        this.componentType = componentType;
        setMaxStackSize(1);
    }

    public ItemCADComponent addStat(String stat, int value) {
        stats.put(stat, Integer.valueOf(value));
        return this;
    }

    public Map<String, Integer> getStats() {
        return Collections.unmodifiableMap(stats);
    }

    @Override
    public EnumCADComponent getComponentType(ItemStack stack) {
        if ("ASSEMBLY".equalsIgnoreCase(componentType)) return EnumCADComponent.ASSEMBLY;
        if ("CORE".equalsIgnoreCase(componentType)) return EnumCADComponent.CORE;
        if ("SOCKET".equalsIgnoreCase(componentType)) return EnumCADComponent.SOCKET;
        if ("BATTERY".equalsIgnoreCase(componentType)) return EnumCADComponent.BATTERY;
        if ("DYE".equalsIgnoreCase(componentType)) return EnumCADComponent.DYE;
        return null;
    }

    @Override
    public int getCADStatValue(ItemStack stack, EnumCADStat stat) {
        if (stat == null) return 0;

        Integer value = stats.get(stat.name());
        if (value != null) return value.intValue();

        value = stats.get(stat.name().toLowerCase());
        if (value != null) return value.intValue();

        String displayName = stat.name().substring(0, 1) + stat.name().substring(1).toLowerCase();
        value = stats.get(displayName);
        return value == null ? 0 : value.intValue();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add(
                EnumChatFormatting.GRAY + "Hold "
                    + EnumChatFormatting.AQUA
                    + "SHIFT"
                    + EnumChatFormatting.GRAY
                    + " for more info");
            return;
        }

        tooltip.add(EnumChatFormatting.GREEN + "Component Type" + EnumChatFormatting.GRAY + ": " + componentType);
        for (Map.Entry<String, Integer> stat : stats.entrySet()) {
            String value = stat.getValue().intValue() == -1
                ? "∞"
                : stat.getValue().toString();
            tooltip.add(" " + EnumChatFormatting.AQUA + stat.getKey() + EnumChatFormatting.GRAY + ": " + value);
        }
    }
}
