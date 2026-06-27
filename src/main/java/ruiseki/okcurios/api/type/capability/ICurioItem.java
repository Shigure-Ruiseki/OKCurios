/*
 * Copyright (c) 2018-2023 C4
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
import net.minecraft.util.DamageSource;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Multimap;

import baubles.api.BaubleType;
import baubles.api.expanded.IBaubleExpanded;
import cpw.mods.fml.common.Optional;
import ruiseki.okcurios.api.SlotContext;

/**
 * Designed to be directly implemented on {@link Item} objects.<br/>
 * <br/>
 * Curios will automatically create and attach {@link ICurio} capability to any ItemStacks that contain items
 * implementing this interface, redirecting all calls made on such capability to respective methods here.
 *
 * @author Extegral
 */
@Optional.Interface(iface = "baubles.api.IBaubleExpanded", modid = "Baubles")
public interface ICurioItem extends IBaubleExpanded {

    /**
     * Default instance of {@link ICurio}, where all calls are redirected by default methods
     * of this interface to avoid needlessly copying over code from there.
     */
    ICurio defaultInstance = () -> null;

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
     *
     * @param slotContext The context for the slot that the ItemStack is in
     * @param stack       The ItemStack in question
     */
    default void curioTick(SlotContext slotContext, ItemStack stack) {
        defaultInstance.curioTick(slotContext);
    }

    /**
     * Called when the ItemStack is equipped into a slot or its data changes.
     *
     * @param slotContext Context about the slot that the ItemStack was just unequipped from
     * @param prevStack   The previous ItemStack in the slot
     * @param stack       The ItemStack in question
     */
    default void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {

    }

    /**
     * Called when the ItemStack is unequipped from a slot or its data changes.
     */
    default void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {

    }

    /**
     * Determines if the ItemStack can be equipped into a slot.
     */
    default boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return defaultInstance.canEquip(slotContext);
    }

    /**
     * Determines if the ItemStack can be unequipped from a slot.
     */
    default boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return defaultInstance.canUnequip(slotContext);
    }

    /**
     * Retrieves a list of tooltips when displaying curio tag information.
     */
    default List<String> getSlotsTooltip(List<String> tagTooltips, ItemStack stack) {
        return defaultInstance.getSlotsTooltip(tagTooltips);
    }

    /**
     * Retrieves a map of attribute modifiers for the curio.
     */

    default Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
        ItemStack stack) {
        return defaultInstance.getAttributeModifiers(slotContext, uuid);
    }

    /**
     * Called server-side when the ItemStack is equipped by using it (i.e. from the hotbar).
     */
    default void onEquipFromUse(SlotContext slotContext, ItemStack stack) {

    }

    /**
     * Retrieves the equip sound information for the given slot context.
     *
     * @param slotContext Context about the slot that the ItemStack was just equipped into
     * @return {@link ICurio.SoundInfo} containing
     *         information about the sound event, volume, and pitch
     */
    @NotNull
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
    default void curioBreak(SlotContext slotContext, ItemStack stack) {
        defaultInstance.curioBreak(slotContext);
    }

    /**
     * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and
     * returns true if the change should be synced to all tracking clients. Note that this check
     * occurs every tick so implementations need to code their own timers for other intervals.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @param stack       The ItemStack in question
     * @return True to sync the ItemStack change to all tracking clients, false to do nothing
     */
    default boolean canSync(SlotContext slotContext, ItemStack stack) {
        return defaultInstance.canSync(slotContext);
    }

    /**
     * Gets a tag that is used to sync extra curio data from the server to the client. Only used when
     * {@link ICurioItem#canSync(SlotContext, ItemStack)} returns true.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @param stack       The ItemStack in question
     * @return Data to be sent to the client
     */
    @NotNull
    default NBTTagCompound serializeNBT(SlotContext slotContext, ItemStack stack) {
        return defaultInstance.serializeNBT(slotContext);
    }

    /**
     * Used client-side to read data tags created by {@link ICurioItem#serializeNBT(SlotContext, ItemStack)}
     * received from the server.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @param compound    Data received from the server
     * @param stack       The ItemStack in question
     */
    default void deserializeNBT(SlotContext slotContext, NBTTagCompound compound, ItemStack stack) {
        defaultInstance.deserializeNBT(slotContext, compound);
    }

    /**
     * Determines if the ItemStack should drop on death and persist through respawn.
     */
    default ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel,
        boolean recentlyHit, ItemStack stack) {
        return defaultInstance.getDropRule(slotContext, source, lootingLevel, recentlyHit);
    }

    /**
     * Retrieves a list of tooltips when displaying curio attribute modifier information returned by
     * {@link ICurio#getAttributeModifiers(SlotContext, UUID)}. By default, this will display a list
     * similar to the vanilla attribute modifier tooltips.
     *
     * @param tooltips A list of {@link String} with the attribute modifier information
     * @param stack    The ItemStack in question
     * @return A list of ITextComponent to display as curio attribute modifier information
     */
    default List<String> getAttributesTooltip(List<String> tooltips, ItemStack stack) {
        return defaultInstance.getAttributesTooltip(tooltips);
    }

    // /**
    // * Allows to set the amount of bonus Fortune levels that are provided by curio.
    // * Default implementation returns level of Fortune enchantment on ItemStack.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @param lootContext Context for the loot drops
    // * @param stack The ItemStack in question
    // * @return Amount of additional Fortune levels that will be applied when mining
    // */
    // default int getFortuneLevel(SlotContext slotContext, LootContext lootContext, ItemStack stack) {
    // return getFortuneBonus(slotContext.identifier(), slotContext.entity(), stack,
    // slotContext.index());
    // }
    //
    // /**
    // * Allows to set the amount of bonus Looting levels that are provided by curio.
    // * Default implementation returns level of Looting enchantment on ItemStack.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @param source Damage source that triggers the looting
    // * @param target The target that drops the loot
    // * @param baseLooting The original looting level before bonuses
    // * @param stack The ItemStack in question
    // * @return Amount of additional Looting levels that will be applied in LootingLevelEvent
    // */
    // default int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target,
    // int baseLooting, ItemStack stack) {
    // return getLootingBonus(slotContext.identifier(), slotContext.entity(), stack,
    // slotContext.index());
    // }
    //
    // /**
    // * Determines whether wearing the curio makes nearby piglins neutral, in the same manner as
    // * wearing gold armor in vanilla.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @return True if nearby piglins are neutral, false otherwise
    // */
    // default boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
    // return stack.makesPiglinsNeutral(slotContext.entity());
    // }
    //
    // /**
    // * Determines whether wearing the curio will allow the user to walk on powder snow, in the same manner as
    // * wearing leather boots in vanilla.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @return True if the user can walk on powder snow, false otherwise
    // */
    // default boolean canWalkOnPowderedSnow(SlotContext slotContext, ItemStack stack) {
    // return stack.canWalkOnPowderedSnow(slotContext.entity());
    // }
    //
    // /**
    // * Determines whether wearing the curio masks the user's eyes against Enderman, in the same manner
    // * as wearing a pumpkin in vanilla.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @param enderMan The Enderman entity that the user is looking at
    // * @return True if it can mask the user from Enderman, false otherwise
    // */
    // default boolean isEnderMask(SlotContext slotContext, EnderMan enderMan, ItemStack stack) {
    //
    // if (slotContext.entity() instanceof Player player) {
    // return stack.isEnderMask(player, enderMan);
    // } else {
    // return false;
    // }
    // }

    /**
     * BAUBLES EXPANDED COMPATIBILITY
     */
    @Override
    default String[] getBaubleTypes(ItemStack itemstack) {
        return new String[] { "ring" };
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default BaubleType getBaubleType(ItemStack itemstack) {
        String[] types = getBaubleTypes(itemstack);
        if (types != null && types.length > 0) {
            String primaryType = types[0];
            if ("amulet".equals(primaryType)) return BaubleType.AMULET;
            if ("belt".equals(primaryType)) return BaubleType.BELT;
        }
        return BaubleType.RING;
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        String[] types = getBaubleTypes(itemstack);
        String identifier = (types != null && types.length > 0) ? types[0] : "ring";
        this.curioTick(new SlotContext(identifier, player, 0, false, true), itemstack);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default void onEquipped(ItemStack itemstack, EntityLivingBase player) {
        String[] types = getBaubleTypes(itemstack);
        String identifier = (types != null && types.length > 0) ? types[0] : "curios";
        this.onEquip(new SlotContext(identifier, player, 0, false, true), null, itemstack);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        String[] types = getBaubleTypes(itemstack);
        String identifier = (types != null && types.length > 0) ? types[0] : "curios";
        this.onUnequip(new SlotContext(identifier, player, 0, false, true), null, itemstack);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default boolean canEquip(ItemStack itemstack, EntityLivingBase player) {
        String[] types = getBaubleTypes(itemstack);
        String identifier = (types != null && types.length > 0) ? types[0] : "curios";
        return this.canEquip(new SlotContext(identifier, player, 0, false, true), itemstack);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    default boolean canUnequip(ItemStack itemstack, EntityLivingBase player) {
        String[] types = getBaubleTypes(itemstack);
        String identifier = (types != null && types.length > 0) ? types[0] : "curios";
        return this.canUnequip(new SlotContext(identifier, player, 0, false, true), itemstack);
    }
}
