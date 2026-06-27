package ruiseki.okcurios.compat;

import net.minecraft.item.ItemStack;

import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import ruiseki.okcurios.api.type.capability.ICurio;

public class BaubleToCurioCapabilityWrapper implements ICurio {

    private final ItemStack stack;
    private final IBauble bauble;

    public BaubleToCurioCapabilityWrapper(ItemStack stack, IBauble bauble) {
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
}
