package ruiseki.okcurios.api.type.inventory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okcore.persist.nbt.INBTSerializable;

public interface ICurioStacksHandler extends INBTSerializable {

    /**
     * Gets the IDynamicStackHandler for the equipped curio stacks.
     *
     * @return The IDynamicStackHandler for the equipped curio stacks
     */
    IDynamicStackHandler getStacks();

    /**
     * Gets the IDynamicStackHandler for the equipped cosmetic curio stacks.
     * <br>
     * The size of this list should always match the sie of {@link ICurioStacksHandler#getStacks()}
     *
     * @return The IDynamicStackHandler for the equipped cosmetic curio stacks
     */
    IDynamicStackHandler getCosmeticStacks();

    /**
     * Gets a list of boolean values that represent render states. True for rendering and false for no
     * rendering.
     * <br>
     * The size of this list should always match the size of {@link ICurioStacksHandler#getStacks()}.
     *
     * @return A list of boolean values for render states
     */
    List<Boolean> getRenders();

    /**
     * Gets the number of slots for equipped curio stacks.
     * <br>
     * This number should always match the size of {@link ICurioStacksHandler#getStacks()}
     *
     * @return The number of slots for equipped curio stacks.
     */
    int getSlots();

    /**
     * Gets whether or not this stack handler should be visible. This does not lock the stack handler
     * from being used regardless.
     *
     * @return True or false for visibility
     */
    boolean isVisible();

    /**
     * Gets whether or not this stack handler has cosmetic handling. This does not lock the cosmetic
     * stack handler from being used regardless.
     *
     * @return True or false for cosmetic handling
     */
    boolean hasCosmetic();

    /**
     * Writes the data for this handler.
     *
     * @return A {@link NBTTagCompound} representing the serialized data
     */
    @Override
    NBTTagCompound serializeNBT();

    /**
     * Reads the data into this handler.
     *
     * @param nbt A {@link NBTTagCompound} representing the serialized data
     */
    @Override
    void deserializeNBT(NBTTagCompound nbt);

    /**
     * Retrieves the slot identifier associated with the handler.
     *
     * @return The slot identifier
     */
    String getIdentifier();

    /**
     * Retrieves all the slot modifiers on the handler.
     *
     * @return A map of modifiers with the UUID as keys and {@link AttributeModifier} as values
     */
    Map<UUID, AttributeModifier> getModifiers();

    /**
     * Retrieves all the permanent slot modifiers on the handler.
     * <br>
     * These slot modifiers are serialized on the handler.
     *
     * @return A set of {@link AttributeModifier}
     */
    Set<AttributeModifier> getPermanentModifiers();

    /**
     * Retrieves all the transient modifiers that have been deserialized but not yet processed.
     *
     * @return A set of {@link AttributeModifier}
     */
    Set<AttributeModifier> getCachedModifiers();

    /**
     * Retrieves all the slot modifiers for a given operation on the handler.
     *
     * @param operation The operation of the modifiers (0, 1, or 2 in 1.7.10)
     * @return A collection of {@link AttributeModifier}
     */
    Collection<AttributeModifier> getModifiersByOperation(int operation);

    /**
     * Adds a temporary slot modifier to the handler.
     * <br>
     * These slot modifiers are not serialized on the handler.
     *
     * @param modifier The {@link AttributeModifier} instance to add
     */
    void addTransientModifier(AttributeModifier modifier);

    /**
     * Adds a permanent slot modifier to the handler.
     * <br>
     * These slot modifiers are serialized on the handler.
     *
     * @param modifier The {@link AttributeModifier} instance to add
     */
    void addPermanentModifier(AttributeModifier modifier);

    /**
     * Removes a slot modifier from the handler.
     *
     * @param uuid The UUID of the modifier to remove
     */
    void removeModifier(UUID uuid);

    /**
     * Removes all the slot modifiers on the handler.
     */
    void clearModifiers();

    /**
     * Removes the cached modifiers that appear upon deserialization of the handler.
     */
    void clearCachedModifiers();

    /**
     * Copies all the slot modifiers from another instance to this one.
     *
     * @param other The other instance
     */
    void copyModifiers(ICurioStacksHandler other);

    /**
     * Recalculates the slot modifiers and resizes the handler.
     */
    void update();

    /**
     * Retrieves the NBT data to sync to clients.
     *
     * @return The data represented as a {@link NBTTagCompound}
     */
    NBTTagCompound getSyncTag();

    /**
     * Applies the NBT data synced to clients.
     * <br>
     * Client-side only.
     *
     * @param tag The data represented as a {@link NBTTagCompound}
     */
    void applySyncTag(NBTTagCompound tag);
}
