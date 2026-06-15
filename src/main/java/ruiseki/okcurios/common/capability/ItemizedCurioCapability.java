package ruiseki.okcurios.common.capability;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Multimap;

import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurioItem;

public class ItemizedCurioCapability implements ICurio {

    private final ItemStack stackInstance;
    private final ICurioItem curioItem;

    public ItemizedCurioCapability(ICurioItem curio, ItemStack stack) {
        this.curioItem = curio;
        this.stackInstance = stack;
    }

    @Override
    public boolean canEquip(String identifier, EntityLivingBase livingEntity) {
        return this.curioItem.canEquip(identifier, livingEntity, this.stackInstance);
    }

    @Override
    public boolean canRender(String identifier, int index, EntityLivingBase livingEntity) {
        return this.curioItem.canRender(identifier, index, livingEntity, this.stackInstance);
    }

    @Override
    public boolean canSync(String identifier, int index, EntityLivingBase livingEntity) {
        return this.curioItem.canSync(identifier, index, livingEntity, this.stackInstance);
    }

    @Override
    public boolean canUnequip(String identifier, EntityLivingBase livingEntity) {
        return this.curioItem.canUnequip(identifier, livingEntity, this.stackInstance);
    }

    @Override
    public void curioAnimate(String identifier, int index, EntityLivingBase livingEntity) {
        this.curioItem.curioAnimate(identifier, index, livingEntity, this.stackInstance);
    }

    @Override
    public void curioBreak(ItemStack stack, EntityLivingBase livingEntity) {
        this.curioItem.curioBreak(stack, livingEntity);
    }

    @Override
    public Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
        return this.curioItem.getAttributeModifiers(slotContext, uuid, this.stackInstance);
    }

    @Override
    public void curioTick(String identifier, int index, EntityLivingBase livingEntity) {
        this.curioItem.curioTick(identifier, index, livingEntity, this.stackInstance);
    }

    @NotNull
    @Override
    public DropRule getDropRule(EntityLivingBase livingEntity) {
        return this.curioItem.getDropRule(livingEntity, this.stackInstance);
    }

    @Override
    public int getFortuneBonus(String identifier, EntityLivingBase livingEntity, ItemStack curioStack, int index) {
        return this.curioItem.getFortuneBonus(identifier, livingEntity, curioStack, index);
    }

    @Override
    public int getLootingBonus(String identifier, EntityLivingBase livingEntity, ItemStack curioStack, int index) {
        return this.curioItem.getLootingBonus(identifier, livingEntity, curioStack, index);
    }

    @Override
    public List<String> getTagsTooltip(List<String> tagTooltips) {
        return this.curioItem.getTagsTooltip(tagTooltips, this.stackInstance);
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.curioItem.deserializeNBT(nbt, this.stackInstance);
    }

    @Override
    public void render(String identifier, int index, EntityLivingBase livingEntity, float limbSwing,
        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.curioItem.render(
            identifier,
            index,
            livingEntity,
            limbSwing,
            limbSwingAmount,
            partialTicks,
            ageInTicks,
            netHeadYaw,
            headPitch,
            this.stackInstance);
    }

    @Override
    public boolean showAttributesTooltip(String identifier) {
        return this.curioItem.showAttributesTooltip(identifier, this.stackInstance);
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        return this.curioItem.serializeNBT(this.stackInstance);
    }

    @Override
    public void onEquipFromUse(SlotContext slotContext) {
        this.curioItem.onEquipFromUse(slotContext, this.stackInstance);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext) {
        return this.curioItem.canEquipFromUse(slotContext, this.stackInstance);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack) {
        this.curioItem.onEquip(slotContext, prevStack, this.stackInstance);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack) {
        this.curioItem.onUnequip(slotContext, newStack, this.stackInstance);
    }

    @NotNull
    @Override
    public SoundInfo getEquipSound(SlotContext slotContext) {
        return this.curioItem.getEquipSound(slotContext, this.stackInstance);
    }

}
