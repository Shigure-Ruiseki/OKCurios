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

import java.util.Map;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.google.common.collect.Multimap;

import ruiseki.okcore.persist.nbt.INBTSerializable;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;

public interface ICuriosItemHandler extends INBTSerializable {

    /**
     * A map of the current curios, keyed by the slot type identifier.
     *
     * @return The current curios equipped
     */
    Map<String, ICurioStacksHandler> getCurios();

    /**
     * Sets the current curios map to the one passed in.
     *
     * @param map The curios collection that will replace the current one
     */
    void setCurios(Map<String, ICurioStacksHandler> map);

    /**
     * Gets the number of slots across all slot type identifiers.
     *
     * @return The number of slots
     */
    int getSlots();

    default int getVisibleSlots() {
        return this.getSlots();
    }

    /**
     * Gets the identifiers of slot types locked for this handler.
     *
     * @return A set of slot type identifiers
     */
    Set<String> getLockedSlots();

    /**
     * Resets the current curios map to default values.
     */
    void reset();

    /**
     * Gets the ICurioStacksHandler associated with the given identifier
     * or null if it doesn't exist.
     *
     * @param identifier The identifier for the slot type
     * @return The stack handler, or null if not found
     */
    ICurioStacksHandler getStacksHandler(String identifier);

    /**
     * Enables the slot type for a given identifier, adding the default settings to the curio map.
     */
    void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic);

    /**
     * Disables the slot type for a given identifier, removing it from the curio map.
     *
     * @param identifier The identifier for the slot type
     */
    void lockSlotType(String identifier);

    /**
     * Processes the lock/unlock slot states that are enqueued
     */
    void processSlots();

    /**
     * Gets the wearer/owner of this handler instance.
     *
     * @return The wearer
     */
    EntityLivingBase getWearer();

    /**
     * Adds an ItemStack to the invalid cache. Used for storing items found in the process of
     * disabling/removing a non-empty slot.
     *
     * @param stack The ItemStack to add
     */
    void loseInvalidStack(ItemStack stack);

    /**
     * Drops all the ItemStacks found in the invalid stacks list. Used for handling items found in
     * disabling/removing slots.
     */
    void handleInvalidStacks();

    /**
     * Returns the total Fortune bonus of all equipped curios.
     * Recalculated with each LivingUpdateEvent.
     */
    int getFortuneBonus();

    /**
     * Returns the total Looting bonus of all equipped curios.
     * Recalculated with each LivingUpdateEvent.
     */
    int getLootingBonus();

    /**
     * Saves the curios inventory stacks to NBT.
     *
     * @param clear True to clear the inventory while saving, false to just save the data
     * @return NBTTagList with the curios inventory stacks data
     */
    NBTTagList saveInventory(boolean clear);

    /**
     * Loads the curios inventory stacks from NBT.
     *
     * @param data NBTTagList data from saveInventory
     */
    void loadInventory(NBTTagList data);

    /**
     * Sets the total Fortune and Looting bonuses associated with the handler.
     *
     * @param fortuneAndLooting An array where [0] is Fortune bonus and [1] is Looting bonus
     */
    void setEnchantmentBonuses(int[] fortuneAndLooting);

    /**
     * Retrieves a set containing the ICurioStacksHandler that require its slot modifiers be
     * synced to tracking clients.
     *
     * @return A set of ICurioStacksHandler that need to be synced to tracking clients
     */
    Set<ICurioStacksHandler> getUpdatingInventories();

    /**
     * Adds the specified slot modifiers to the handler as temporary slot modifiers.
     * <br>
     * These slot modifiers are not serialized and disappear upon deserialization.
     *
     * @param modifiers A Multimap with slot identifiers as keys and attribute modifiers as values
     */
    void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers);

    /**
     * Adds the specified slot modifiers to the handler as permanent slot modifiers.
     *
     * @param modifiers A Multimap with slot identifiers as keys and attribute modifiers as values
     */
    void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers);

    /**
     * Removes the specified slot modifiers from the handler.
     *
     * @param modifiers A Multimap with slot identifiers as keys and attribute modifiers as values
     */
    void removeSlotModifiers(Multimap<String, AttributeModifier> modifiers);

    /**
     * Removes all the slot modifiers from the handler.
     */
    void clearSlotModifiers();

    /**
     * Retrieves all the slot modifiers from the handler.
     *
     * @return A Multimap with slot identifiers as keys and attribute modifiers as values
     */
    Multimap<String, AttributeModifier> getModifiers();

    /**
     * Serializes the handler to NBT.
     *
     * @return Data for the handler represented as a NBTTagCompound
     */
    @Override
    NBTTagCompound serializeNBT();

    /**
     * Deserializes the handler from NBT.
     *
     * @param nbtTagCompound Data for the handler represented as a NBTTagCompound
     */
    @Override
    void deserializeNBT(NBTTagCompound nbtTagCompound);

    /**
     * Removes the cached modifiers that appear upon deserialization of the handler.
     */
    void clearCachedSlotModifiers();
}
