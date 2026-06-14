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
package ruiseki.okcurios.api.type.helper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;

import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.SlotResult;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;

public interface ICuriosHelper {

    /**
     * Gets the curio capability attached to the ItemStack.
     *
     * @param stack The ItemStack to get the curio capability from
     * @return The curio capability, or null if none attached
     */
    ICurio getCurio(ItemStack stack);

    /**
     * Gets the curio inventory capability attached to the entity.
     *
     * @param livingEntity The EntityLivingBase to get the curio inventory capability from
     * @return The curio inventory capability, or null if none attached
     */
    ICuriosItemHandler getCuriosHandler(EntityLivingBase livingEntity);

    /**
     * Retrieves a set of string identifiers from the curio tags associated with the given item.
     *
     * @param item The Item to retrieve curio tags for
     * @return Set of unique curio identifiers associated with the item
     */
    Set<String> getCurioTags(Item item);

    /**
     * Gets an IInventory that contains all the equipped curio stacks (not including cosmetics).
     *
     * @param livingEntity The wearer of the curios
     * @return The equipped curio stacks inventory, or null if there is no curios handler
     */
    IInventory getEquippedCurios(EntityLivingBase livingEntity);

    /**
     * Replaces the currently equipped item in a specified curio slot, if it exists.
     *
     * @param livingEntity The wearer of the curio
     * @param identifier   The identifier of the curio slot
     * @param index        The index of the curio slot
     * @param stack        The new stack to place into the slot
     */
    void setEquippedCurio(EntityLivingBase livingEntity, String identifier, int index, ItemStack stack);

    /**
     * Gets the first matching item equipped in a curio slot.
     *
     * @param livingEntity The wearer of the item to be found
     * @param item         The item to search for
     * @return A SlotResult with the found item, or null if none were found
     */
    SlotResult findFirstCurio(EntityLivingBase livingEntity, Item item);

    /**
     * Gets the first matching item equipped in a curio slot that matches the filter.
     *
     * @param livingEntity The wearer of the item to be found
     * @param filter       The filter to test against
     * @return A SlotResult with the found item, or null if none were found
     */
    SlotResult findFirstCurio(EntityLivingBase livingEntity, Predicate<ItemStack> filter);

    /**
     * Gets all matching items equipped in a curio slot.
     *
     * @param livingEntity The wearer of the item to be found
     * @param item         The item to search for
     * @return A list of matching results
     */
    List<SlotResult> findCurios(EntityLivingBase livingEntity, Item item);

    /**
     * Gets all matching items equipped in a curio slot that matches the filter.
     *
     * @param livingEntity The wearer of the item to be found
     * @param filter       The filter to test against
     * @return A list of matching results
     */
    List<SlotResult> findCurios(EntityLivingBase livingEntity, Predicate<ItemStack> filter);

    /**
     * Gets all items equipped in all curio slots with specific identifiers.
     *
     * @param livingEntity The wearer of the item to be found
     * @param identifiers  The identifiers for the slot types
     * @return A list of matching results
     */
    List<SlotResult> findCurios(EntityLivingBase livingEntity, String... identifiers);

    /**
     * Gets the currently equipped item in a specified curio slot, if it exists.
     *
     * @param livingEntity The wearer of the curio
     * @param identifier   The identifier of the curio slot
     * @param index        The index of the curio slot
     * @return The equipped curio slot result, or null if there is none
     */
    SlotResult findCurio(EntityLivingBase livingEntity, String identifier, int index);

    /**
     * Retrieves a map of attribute modifiers for the ItemStack.
     *
     * @param slotContext Context about the slot that the ItemStack is equipped in
     * @param uuid        Slot-unique UUID
     * @param stack       The ItemStack in question
     * @return A map of attribute modifiers
     */
    Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack);

    /**
     * Adds a slot modifier to a specified attribute map.
     */
    void addSlotModifier(Multimap<IAttribute, AttributeModifier> map, String identifier, UUID uuid, double amount,
        int operation); // 1.7.10 dùng giá trị int (0, 1, 2) cho Operation của Attribute

    /**
     * Adds a slot modifier to an ItemStack's tag data.
     */
    void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid, double amount, int operation,
        String slot);

    /**
     * Adds an attribute modifier to an ItemStack's tag data.
     */
    void addModifier(ItemStack stack, IAttribute attribute, String name, UUID uuid, double amount, int operation,
        String slot);

    /**
     * Checks if the ItemStack is valid for a particular stack and slot context.
     *
     * @param slotContext Context about the slot that the ItemStack is being checked for
     * @param stack       The ItemStack in question
     * @return True if the ItemStack is valid for the slot, false otherwise
     */
    boolean isStackValid(SlotContext slotContext, ItemStack stack);

    /**
     * Triggered manual break animations in curio slots when an item breaks.
     *
     * @param id      The slot type String identifier
     * @param index   The slot index of the identifier
     * @param damager The entity that is breaking the item
     */
    void onBrokenCurio(String id, int index, EntityLivingBase damager);
}
