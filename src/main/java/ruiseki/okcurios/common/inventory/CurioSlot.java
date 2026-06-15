package ruiseki.okcurios.common.inventory;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.SlotItemHandler;
import ruiseki.okcurios.Reference;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioEquipEvent;
import ruiseki.okcurios.api.event.CurioUnequipEvent;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class CurioSlot extends SlotItemHandler {

    private final String identifier;
    private final EntityPlayer player;
    private final SlotContext slotContext;
    private final List<Boolean> renderStatuses;

    public CurioSlot(EntityPlayer player, IDynamicStackHandler handler, int index, String identifier, int xPosition,
        int yPosition, List<Boolean> renders) {
        super(handler, index, xPosition, yPosition);
        this.identifier = identifier;
        this.renderStatuses = renders;
        this.player = player;
        this.slotContext = new SlotContext(identifier, player, index);

        if (player.getEntityWorld().isRemote) {
            this.bindClientDirectTexture();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void bindClientDirectTexture() {
        ResourceLocation customTexture = CuriosApi.getIconHelper()
            .getIcon(this.identifier);
        if (customTexture != null) {
            this.setBackgroundIconTexture(customTexture);
        } else {
            this.setBackgroundIconTexture(new ResourceLocation(Reference.MOD_ID, "item/empty_curio_slot"));
        }
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean getRenderStatus() {
        return this.renderStatuses.size() > this.getSlotIndex() && this.renderStatuses.get(this.getSlotIndex());
    }

    @SideOnly(Side.CLIENT)
    public String getSlotName() {
        return LangHelpers.localize("curios.identifier." + this.identifier);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return false;

        CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
        MinecraftForge.EVENT_BUS.post(equipEvent);
        Event.Result result = equipEvent.getResult();

        if (result == Event.Result.DENY) return false;
        if (result == Event.Result.ALLOW) return true;

        ICurio curio = CuriosApi.getCuriosHelper()
            .getCurio(stack);
        boolean canEquip = (curio == null) || curio.canEquip(identifier, player);

        return CuriosApi.getCuriosHelper()
            .isStackValid(slotContext, stack) && canEquip
            && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(EntityPlayer playerIn) {
        ItemStack stack = this.getStack();
        if (stack == null) return true;

        CurioUnequipEvent unequipEvent = new CurioUnequipEvent(stack, slotContext);
        MinecraftForge.EVENT_BUS.post(unequipEvent);
        Event.Result result = unequipEvent.getResult();

        if (result == Event.Result.DENY) return false;
        if (result == Event.Result.ALLOW) return true;

        ICurio curio = CuriosApi.getCuriosHelper()
            .getCurio(stack);
        boolean canUnequip = (curio == null) || curio.canUnequip(this.identifier, playerIn);

        return canUnequip && super.canTakeStack(playerIn);
    }
}
