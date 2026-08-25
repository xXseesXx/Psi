package vazkii.psi.common.item.component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import org.lwjgl.input.Keyboard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

/** Shared base for the individual parts used to assemble a CAD. */
public abstract class ItemCADComponent extends Item {

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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            tooltip.add(EnumChatFormatting.GRAY + "Hold " + EnumChatFormatting.AQUA + "SHIFT"
                + EnumChatFormatting.GRAY + " for more info");
            return;
        }

        tooltip.add(EnumChatFormatting.GREEN + "Component Type" + EnumChatFormatting.GRAY + ": " + componentType);
        for (Map.Entry<String, Integer> stat : stats.entrySet()) {
            String value = stat.getValue().intValue() == -1 ? "∞" : stat.getValue().toString();
            tooltip.add(" " + EnumChatFormatting.AQUA + stat.getKey() + EnumChatFormatting.GRAY + ": " + value);
        }
    }
}
