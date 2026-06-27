package ruiseki.okcurios.api.event;

import java.util.Collection;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import cpw.mods.fml.common.eventhandler.Event;
import ruiseki.okcurios.api.SlotContext;

/**
 * This event is fired when the attributes for a curio ItemStack are being calculated.
 * <br>
 * Attributes are calculated on the server when equipping and unequipping a curio to add and remove attributes
 * respectively, both must be consistent.
 * <br>
 * Attributes are calculated on the client when rendering an item's tooltip to show relevant attributes.
 * <br>
 * Note that this event is fired regardless of if the stack has NBT overriding attributes or not.
 * If your attribute should be ignored when attributes are overridden, you can check for the presence of the
 * CurioAttributeModifiers tag.
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
public class CurioAttributeModifierEvent extends Event {

    private final ItemStack stack;
    private final SlotContext slotContext;
    private final UUID uuid;
    private final Multimap<IAttribute, AttributeModifier> originalModifiers;
    private Multimap<IAttribute, AttributeModifier> unmodifiableModifiers;
    @Nullable
    private Multimap<IAttribute, AttributeModifier> modifiableModifiers;

    public CurioAttributeModifierEvent(ItemStack stack, SlotContext slotContext, UUID uuid,
        Multimap<IAttribute, AttributeModifier> modifiers) {
        this.stack = stack;
        this.slotContext = slotContext;
        this.unmodifiableModifiers = this.originalModifiers = modifiers;
        this.uuid = uuid;
    }

    /**
     * Returns an unmodifiable view of the attribute multimap. Use other methods from this event to modify the
     * attributes map.
     * Note that adding attributes based on existing attributes may lead to inconsistent results between the tooltip
     * (client)
     * and the actual attributes (server) if the listener order is different. Using {@link #getOriginalModifiers()}
     * instead will give more consistent results.
     */
    public Multimap<IAttribute, AttributeModifier> getModifiers() {
        return this.unmodifiableModifiers;
    }

    /**
     * Returns the attribute map before any changes from other event listeners was made.
     */
    public Multimap<IAttribute, AttributeModifier> getOriginalModifiers() {
        return this.originalModifiers;
    }

    /**
     * Gets a modifiable map instance, creating it if the current map is currently unmodifiable
     */
    private Multimap<IAttribute, AttributeModifier> getModifiableMap() {

        if (this.modifiableModifiers == null) {
            this.modifiableModifiers = HashMultimap.create(this.originalModifiers);
            this.unmodifiableModifiers = Multimaps.unmodifiableMultimap(this.modifiableModifiers);
        }
        return this.modifiableModifiers;
    }

    /**
     * Adds a new attribute modifier to the given stack.
     * Modifier must have a consistent UUID for consistency between equipping and unequipping items.
     * Modifier name should clearly identify the mod that added the modifier.
     *
     * @param attribute Attribute
     * @param modifier  Modifier instance.
     * @return True if the attribute was added, false if it was already present
     */
    public boolean addModifier(IAttribute attribute, AttributeModifier modifier) {
        return getModifiableMap().put(attribute, modifier);
    }

    /**
     * Removes a single modifier for the given attribute
     *
     * @param attribute Attribute
     * @param modifier  Modifier instance
     * @return True if an attribute was removed, false if no change
     */
    public boolean removeModifier(IAttribute attribute, AttributeModifier modifier) {
        return getModifiableMap().remove(attribute, modifier);
    }

    /**
     * Removes all modifiers for the given attribute
     *
     * @param attribute Attribute
     * @return Collection of removed modifiers
     */
    public Collection<AttributeModifier> removeAttribute(IAttribute attribute) {
        return getModifiableMap().removeAll(attribute);
    }

    /**
     * Removes all modifiers for all attributes
     */
    public void clearModifiers() {
        getModifiableMap().clear();
    }

    /**
     * Gets the slot context containing this stack
     */
    public SlotContext getSlotContext() {
        return this.slotContext;
    }

    /**
     * Gets the item stack instance
     */
    public ItemStack getItemStack() {
        return this.stack;
    }

    /**
     * Gets a slot-unique UUID for attribute modifiers
     */
    public UUID getUuid() {
        return this.uuid;
    }
}
