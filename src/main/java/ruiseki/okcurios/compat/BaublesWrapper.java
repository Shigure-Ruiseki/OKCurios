package ruiseki.okcurios.compat;

import java.util.List;

import net.minecraft.item.ItemStack;

import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.capability.ICurio;

public class BaublesWrapper implements ICurio {

    private final ItemStack stack;
    private final IBauble bauble;

    public BaublesWrapper(ItemStack stack, IBauble bauble) {
        this.stack = stack;
        this.bauble = bauble;
    }

    private String getPrimaryIdentifier() {
        if (bauble instanceof IBaubleExpanded expanded) {
            String[] types = expanded.getBaubleTypes(stack);
            if (types != null && types.length > 0) return types[0];
        }
        return BaubleExpandedSlots.getTypeFromBaubleType(bauble.getBaubleType(stack));
    }

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        this.bauble.onWornTick(stack, slotContext.entity());
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
        if (!slotContext.identifier()
            .equals(getPrimaryIdentifier())) {
            return false;
        }
        return this.bauble.canEquip(stack, slotContext.entity());
    }

    @Override
    public boolean canUnequip(SlotContext slotContext) {
        return this.bauble.canUnequip(stack, slotContext.entity());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
        this.bauble.onEquipped(stack, slotContext.entity());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
        this.bauble.onUnequipped(stack, slotContext.entity());
    }

    @Override
    public List<String> getSlotsTooltip(List<String> tooltips) {
        String type = getPrimaryIdentifier();
        if (type != null) {
            tooltips.add("\u00A76" + type);
        }
        return tooltips;
    }
}
