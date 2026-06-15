package ruiseki.okcurios.common.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.item.ItemStackHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class DynamicStackHandler extends ItemStackHandler implements IDynamicStackHandler {

    protected List<ItemStack> previousStacks;

    public DynamicStackHandler(int size) {
        super(size);
        this.previousStacks = new ArrayList<>(Math.max(0, size));
        for (int i = 0; i < size; i++) {
            this.previousStacks.add(null);
        }
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @Override
    public void setPreviousStackInSlot(int slot, @NotNull ItemStack stack) {
        this.validateSlotIndex(slot);
        this.previousStacks.set(slot, stack);
        this.onContentsChanged(slot);
    }

    @Override
    public ItemStack getPreviousStackInSlot(int slot) {
        this.validateSlotIndex(slot);
        return this.previousStacks.get(slot);
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

    private static List<ItemStack> getResizedList(int size, List<ItemStack> oldStacks) {
        int targetSize = Math.max(0, size);
        List<ItemStack> newList = new ArrayList<ItemStack>(targetSize);

        for (int i = 0; i < targetSize; i++) {
            if (i < oldStacks.size()) {
                newList.add(oldStacks.get(i));
            } else {
                newList.add(null);
            }
        }
        return newList;
    }
}
