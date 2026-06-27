package ruiseki.okcurios.common.capability;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Multimap;

import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurioItem;

public class ItemizedCurioCapability implements ICurio {

    private final ItemStack stack;
    private final ICurioItem curioItem;

    public ItemizedCurioCapability(ICurioItem curio, ItemStack stack) {
        this.curioItem = curio;
        this.stack = stack;
    }

    @Override
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        this.curioItem.curioTick(slotContext, this.getStack());
    }

    @Override
    public boolean canEquip(SlotContext slotContext) {
        return this.curioItem.canEquip(slotContext, this.getStack());
    }

    @Override
    public boolean canUnequip(SlotContext slotContext) {
        return this.curioItem.canUnequip(slotContext, this.getStack());
    }

    @Override
    public List<String> getSlotsTooltip(List<String> tooltips) {
        return this.curioItem.getSlotsTooltip(tooltips, this.getStack());
    }

    @Override
    public void curioBreak(SlotContext slotContext) {
        this.curioItem.curioBreak(slotContext, this.getStack());
    }

    @Override
    public boolean canSync(SlotContext slotContext) {
        return this.curioItem.canSync(slotContext, this.getStack());
    }

    @Override
    public NBTTagCompound serializeNBT(SlotContext slotContext) {
        return this.curioItem.serializeNBT(slotContext, this.getStack());
    }

    @Override
    public void deserializeNBT(SlotContext slotContext, NBTTagCompound compound) {
        this.curioItem.deserializeNBT(slotContext, compound, this.getStack());
    }

    @Override
    public @NotNull DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
        boolean recentlyHit) {
        return this.curioItem.getDropRule(slotContext, source, lootingLevel, recentlyHit, this.getStack());
    }

    @Override
    public List<String> getAttributesTooltip(List<String> tooltips) {
        return this.curioItem.getAttributesTooltip(tooltips, this.getStack());
    }

    @Override
    public Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
        return this.curioItem.getAttributeModifiers(slotContext, uuid, this.getStack());
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext) {
        this.curioItem.onEquipFromUse(slotContext, this.getStack());
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext) {
        return this.curioItem.canEquipFromUse(slotContext, this.getStack());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
        this.curioItem.onEquip(slotContext, prevStack, this.getStack());
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
        this.curioItem.onUnequip(slotContext, newStack, this.getStack());
    }

    @Override
    public @NotNull SoundInfo getEquipSound(SlotContext slotContext) {
        return this.curioItem.getEquipSound(slotContext, this.getStack());
    }
}
