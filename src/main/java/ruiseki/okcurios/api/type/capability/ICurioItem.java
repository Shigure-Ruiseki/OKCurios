/*
 * Copyright (c) 2018-2020 C4
 * This file is part of Curios, a mod made for Minecraft.
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios. If not, see <https://www.gnu.org/licenses/>.
 */
package ruiseki.okcurios.api.type.capability;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.Multimap;

import ruiseki.okcore.persist.nbt.INBTSerializable;
import ruiseki.okcurios.api.SlotContext;

/**
 * Designed to be directly implemented on {@link Item} objects.<br/>
 * <br/>
 * Curios will automatically create and attach {@link ICurio} capability to any ItemStacks that contain items
 * implementing this interface, redirecting all calls made on such capability to respective methods here.
 *
 * @author Extegral
 */
public interface ICurioItem extends INBTSerializable {

    /**
     * Default instance of {@link ICurio}, where all calls are redirected by default methods
     * of this interface to avoid needlessly copying over code from there.
     */
    ICurio defaultInstance = new ICurio() {
        // Khởi tạo một thực thể trống đại diện cho ICurio
    };

    /**
     * Called during automatic capability attachment to any ItemStack containing this {@link ICurioItem} instance.
     *
     * @param stack ItemStack in question
     * @return true to allow attach {@link ICurio} capability to this ItemStack; false to prevent attachment.
     */
    default boolean hasCurioCapability(ItemStack stack) {
        return true;
    }

    /**
     * Called every tick on both client and server while the ItemStack is equipped.
     */
    default void curioTick(String identifier, int index, EntityLivingBase livingEntity, ItemStack stack) {
        defaultInstance.curioTick(identifier, index, livingEntity);
    }

    /**
     * Called every tick only on the client while the ItemStack is equipped.
     */
    default void curioAnimate(String identifier, int index, EntityLivingBase livingEntity, ItemStack stack) {
        defaultInstance.curioAnimate(identifier, index, livingEntity);
    }

    /**
     * Called when the ItemStack is equipped into a slot or its data changes.
     */
    default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {}

    /**
     * Called when the ItemStack is unequipped from a slot or its data changes.
     */
    default void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {}

    /**
     * Determines if the ItemStack can be equipped into a slot.
     */
    default boolean canEquip(String identifier, EntityLivingBase livingEntity, ItemStack stack) {
        return defaultInstance.canEquip(identifier, livingEntity);
    }

    /**
     * Determines if the ItemStack can be unequipped from a slot.
     */
    default boolean canUnequip(String identifier, EntityLivingBase livingEntity, ItemStack stack) {
        return defaultInstance.canUnequip(identifier, livingEntity);
    }

    /**
     * Retrieves a list of tooltips when displaying curio tag information.
     */
    default List<String> getTagsTooltip(List<String> tagTooltips, ItemStack stack) {
        return defaultInstance.getTagsTooltip(tagTooltips);
    }

    /**
     * Retrieves a map of attribute modifiers for the curio.
     */
    default Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
        ItemStack stack) {
        return null;
    }

    /**
     * Called server-side when the ItemStack is equipped by using it (i.e. from the hotbar).
     */
    default void onEquipFromUse(SlotContext slotContext, ItemStack stack) {
        ICurio.SoundInfo soundInfo = getEquipSound(slotContext, stack);
        if (slotContext.wearer() != null && soundInfo != null) {
            slotContext.wearer().worldObj
                .playSoundAtEntity(slotContext.wearer(), soundInfo.soundName(), soundInfo.volume(), soundInfo.pitch());
        }
    }

    /**
     * Retrieves the equip sound information for the given slot context.
     */
    default ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo("item.armor.equip.generic", 1.0f, 1.0f);
    }

    /**
     * Determines if the ItemStack can be automatically equipped into the first available slot when used.
     */
    default boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return false;
    }

    /**
     * Called when rendering break animations and sounds client-side when a worn curio item is broken.
     */
    default void curioBreak(ItemStack stack, EntityLivingBase livingEntity) {
        defaultInstance.curioBreak(stack, livingEntity);
    }

    /**
     * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes.
     */
    default boolean canSync(String identifier, int index, EntityLivingBase livingEntity, ItemStack stack) {
        return defaultInstance.canSync(identifier, index, livingEntity);
    }

    /**
     * Gets a tag that is used to sync extra curio data from the server to the client.
     */
    default NBTTagCompound serializeNBT(ItemStack stack) {
        return defaultInstance.serializeNBT();
    }

    /**
     * Used client-side to read data tags received from the server.
     */
    default void deserializeNBT(NBTTagCompound compound, ItemStack stack) {
        defaultInstance.deserializeNBT(compound);
    }

    /**
     * Determines if the ItemStack should drop on death and persist through respawn.
     */
    default ICurio.DropRule getDropRule(EntityLivingBase livingEntity, ItemStack stack) {
        return defaultInstance.getDropRule(livingEntity);
    }

    /**
     * Determines whether or not Curios will automatically add tooltip listing attribute modifiers.
     */
    default boolean showAttributesTooltip(String identifier, ItemStack stack) {
        return defaultInstance.showAttributesTooltip(identifier);
    }

    /**
     * Allows to set the amount of bonus Fortune levels that are provided by curio.
     */
    default int getFortuneBonus(String identifier, EntityLivingBase livingEntity, ItemStack curio, int index) {
        return defaultInstance.getFortuneBonus(identifier, livingEntity, curio, index);
    }

    /**
     * Allows to set the amount of bonus Looting levels that are provided by curio.
     */
    default int getLootingBonus(String identifier, EntityLivingBase livingEntity, ItemStack curio, int index) {
        return defaultInstance.getLootingBonus(identifier, livingEntity, curio, index);
    }

    /**
     * Determines if the ItemStack has rendering.
     */
    default boolean canRender(String identifier, int index, EntityLivingBase livingEntity, ItemStack stack) {
        return defaultInstance.canRender(identifier, index, livingEntity);
    }

    /**
     * Performs rendering of the ItemStack if canRender returns true.
     * (Đã loại bỏ MatrixStack và IRenderTypeBuffer theo chuẩn Render 1.7.10)
     */
    default void render(String identifier, int index, EntityLivingBase livingEntity, float limbSwing,
        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch,
        ItemStack stack) {
        defaultInstance.render(
            identifier,
            index,
            livingEntity,
            limbSwing,
            limbSwingAmount,
            partialTicks,
            ageInTicks,
            netHeadYaw,
            headPitch);
    }
}
