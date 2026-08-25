package vazkii.psi.client.core.handler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import vazkii.psi.client.gui.GuiCADSelect;
import vazkii.psi.common.item.ItemCreativeCAD;

public class KeybindHandler {
    public static final KeyBinding PSI_MASTER=new KeyBinding("key.psi.master",Keyboard.KEY_C,"key.categories.psi");
    public static void init(){ClientRegistry.registerKeyBinding(PSI_MASTER);}
    @SubscribeEvent public void clientTick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END||!PSI_MASTER.isPressed())return;Minecraft mc=Minecraft.getMinecraft();if(mc.currentScreen!=null||mc.thePlayer==null)return;ItemStack held=mc.thePlayer.getHeldItem();if(held!=null&&held.getItem() instanceof ItemCreativeCAD)mc.displayGuiScreen(new GuiCADSelect(held));}
}
