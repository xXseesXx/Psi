package vazkii.psi.common.block.tile.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import vazkii.psi.common.block.tile.TileCADAssembler;
import vazkii.psi.common.item.ItemCreativeCAD;

/** Magazine-only assembler container. Component/CAD construction slots await the real CAD port. */
public class ContainerCADAssembler extends Container {
    public final TileCADAssembler assembler;
    public ContainerCADAssembler(EntityPlayer player, TileCADAssembler assembler) {
        this.assembler=assembler;
        addSlotToContainer(new Slot(assembler,0,35,21) { @Override public void onPickupFromSlot(EntityPlayer p,ItemStack stack) { super.onPickupFromSlot(p,stack); assembler.clearMagazineView(); } });
        for(int row=0;row<4;row++) for(int col=0;col<3;col++) { final int index=1+col+row*3; addSlotToContainer(new Slot(assembler,index,17+col*18,57+row*18) { private boolean loaded(){return assembler.getStackInSlot(0)!=null&&assembler.getStackInSlot(0).getItem() instanceof ItemCreativeCAD;} @Override public boolean isItemValid(ItemStack stack){return loaded()&&super.isItemValid(stack);}@Override public boolean canTakeStack(EntityPlayer p){return loaded()&&super.canTakeStack(p);} }); }
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlotToContainer(new Slot(player.inventory,col+row*9+9,48+col*18,143+row*18));
        for(int col=0;col<9;col++) addSlotToContainer(new Slot(player.inventory,col,48+col*18,201));
    }
    @Override public boolean canInteractWith(EntityPlayer player) { return assembler.isUseableByPlayer(player); }
    @Override public ItemStack transferStackInSlot(EntityPlayer player, int slot) { return null; }
}
