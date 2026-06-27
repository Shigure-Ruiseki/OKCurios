package ruiseki.okcurios.common.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.NotNull;

import cpw.mods.fml.common.eventhandler.Event;
import ruiseki.okcore.item.ItemStackHandler;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioEquipEvent;
import ruiseki.okcurios.api.event.CurioUnequipEvent;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class DynamicStackHandler extends ItemStackHandler implements IDynamicStackHandler {

    protected List<ItemStack> previousStacks;
    protected Function<Integer, SlotContext> ctxBuilder;

    public DynamicStackHandler(int size, Function<Integer, SlotContext> ctxBuilder) {
        super(size);
        this.previousStacks = new ArrayList<>(size);
        this.ctxBuilder = ctxBuilder;
    }

    @Override
    public void setPreviousStackInSlot(int slot, @NotNull ItemStack stack) {
        this.validateSlotIndex(slot);
        this.previousStacks.set(slot, stack);
        this.onContentsChanged(slot);
    }

    @NotNull
    @Override
    public ItemStack getPreviousStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        return this.previousStacks.get(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        SlotContext ctx = ctxBuilder.apply(slot);
        CurioEquipEvent equipEvent = new CurioEquipEvent(stack, ctx);
        MinecraftForge.EVENT_BUS.post(equipEvent);
        Event.Result result = equipEvent.getResult();

        if (result == Event.Result.DENY) {
            return false;
        }
        return result == Event.Result.ALLOW || (CuriosApi.isStackValid(ctx, stack) && CuriosApi.getCurio(stack)
            .map(curio -> curio.canEquip(ctx))
            .orElse(true) && super.isItemValid(slot, stack));
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack existing = this.stacks.get(slot);
        SlotContext ctx = ctxBuilder.apply(slot);
        CurioUnequipEvent unequipEvent = new CurioUnequipEvent(existing, ctx);
        MinecraftForge.EVENT_BUS.post(unequipEvent);
        Event.Result result = unequipEvent.getResult();

        if (result == Event.Result.DENY) {
            return null;
        }
        boolean isCreative = ctx.entity() instanceof EntityPlayer player && player.capabilities.isCreativeMode;

        if (result == Event.Result.ALLOW || ((existing == null || isCreative) && CuriosApi.getCurio(existing)
            .map(curio -> curio.canUnequip(ctx))
            .orElse(true))) {
            return super.extractItem(slot, amount, simulate);
        }
        return null;
    }

    @Override
    public void grow(int amount) {
        this.stacks = getResizedList(this.stacks.size() + amount, this.stacks);
        this.previousStacks = getResizedList(this.previousStacks.size() + amount, this.previousStacks);
    }

    @Override
    public void shrink(int amount) {
        this.stacks = getResizedList(this.stacks.size() - amount, this.stacks);
        this.previousStacks = getResizedList(this.previousStacks.size() - amount, this.previousStacks);
    }

    private static List<ItemStack> getResizedList(int size, List<ItemStack> stacks) {
        int targetSize = Math.max(0, size);
        List<ItemStack> newList = new ArrayList<>(targetSize);

        for (int i = 0; i < targetSize && i < stacks.size(); i++) {
            newList.add(stacks.get(i));
        }
        return newList;
    }
}
