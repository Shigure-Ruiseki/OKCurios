package ruiseki.okcurios.compat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import baubles.api.IBauble;
import baubles.api.expanded.BaubleExpandedSlots;
import baubles.api.expanded.IBaubleExpanded;
import ruiseki.okcurios.api.SlotContext;
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
    public void curioTick(String identifier, int index, EntityLivingBase livingEntity) {
        bauble.onWornTick(stack, livingEntity);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
        if (slotContext.wearer() != null) {
            bauble.onEquipped(stack, slotContext.wearer());
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
        if (slotContext.wearer() != null) {
            bauble.onUnequipped(stack, slotContext.wearer());
        }
    }

    @Override
    public boolean canEquip(String identifier, EntityLivingBase livingEntity) {
        String expectedId = getPrimaryIdentifier();
        if (!identifier.equals(expectedId)) return false;
        return bauble.canEquip(stack, livingEntity);
    }

    @Override
    public boolean canUnequip(String identifier, EntityLivingBase livingEntity) {
        return bauble.canUnequip(stack, livingEntity);
    }
}
