package ruiseki.okcurios.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.SlotItemHandler;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioEquipEvent;
import ruiseki.okcurios.api.event.CurioUnequipEvent;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class CurioSlot extends SlotItemHandler {

    private final String identifier;
    private final EntityPlayer player;
    private final SlotContext slotContext;

    private NonNullList<Boolean> renderStatuses;
    private boolean canToggleRender;

    public CurioSlot(EntityPlayer player, IDynamicStackHandler handler, int index, String identifier, int xPosition,
        int yPosition, NonNullList<Boolean> renders, boolean canToggleRender) {
        super(handler, index, xPosition, yPosition);
        this.identifier = identifier;
        this.renderStatuses = renders;
        this.player = player;
        this.canToggleRender = canToggleRender;
        this.slotContext = new SlotContext(
            identifier,
            player,
            index,
            this instanceof CosmeticCurioSlot,
            this instanceof CosmeticCurioSlot || renders.get(index));
        if (player.getEntityWorld().isRemote) {
            this.bindClientDirectTexture();
        }
    }

    @SideOnly(Side.CLIENT)
    protected void bindClientDirectTexture() {
        this.setBackgroundTexture(CuriosApi.getSlotIcon(identifier));
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean canToggleRender() {
        return this.canToggleRender;
    }

    public boolean getRenderStatus() {

        if (!this.canToggleRender) {
            return true;
        }
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

        return CuriosApi.isStackValid(slotContext, stack) && CuriosApi.getCurio(stack)
            .map(curio -> curio.canEquip(slotContext))
            .orElse(false) && super.isItemValid(stack);
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

        return CuriosApi.getCurio(stack)
            .map(curio -> curio.canUnequip(slotContext))
            .orElse(false) && super.canTakeStack(playerIn);
    }
}
