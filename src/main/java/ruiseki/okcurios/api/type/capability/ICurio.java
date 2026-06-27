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

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcurios.api.SlotContext;

public interface ICurio {

    /**
     * Gets the ItemStack associated with the instance
     *
     * @return The ItemStack associated with the instance
     */
    ItemStack getStack();

    /**
     * Called every tick on both client and server while the ItemStack is equipped.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     */
    default void curioTick(SlotContext slotContext) {

    }

    /**
     * Called when the ItemStack is equipped into a slot or its data changes.
     *
     * @param slotContext Context about the slot that the ItemStack was just unequipped from
     * @param prevStack   The previous ItemStack in the slot
     */
    default void onEquip(SlotContext slotContext, ItemStack prevStack) {

    }

    /**
     * Called when the ItemStack is unequipped from a slot or its data changes.
     *
     * @param slotContext Context about the slot that the ItemStack was just unequipped from
     * @param newStack    The new ItemStack in the slot
     */
    default void onUnequip(SlotContext slotContext, ItemStack newStack) {

    }

    /**
     * Determines if the ItemStack can be equipped into a slot.
     *
     * @param slotContext Context about the slot that the ItemStack is attempting to equip into
     * @return True if the ItemStack can be equipped/put in, false if not
     */
    default boolean canEquip(SlotContext slotContext) {
        return true;
    }

    /**
     * Determines if the ItemStack can be unequipped from a slot.
     *
     * @param slotContext Context about the slot that the ItemStack is attempting to unequip from
     * @return True if the ItemStack can be unequipped/taken out, false if not
     */
    default boolean canUnequip(SlotContext slotContext) {
        return true;
    }

    /**
     * Retrieves a list of tooltips when displaying curio slot information. By default, this will be a
     * list of each slot identifier, translated and in gold text, associated with the curio.
     * <br>
     * If overriding, make sure the user has some indication which slots are associated with the
     * curio.
     *
     * @param tooltips A list of {@link String} with every slot valid for this curio
     * @return A list of ITextComponent to display as curio slot information
     */
    default List<String> getSlotsTooltip(List<String> tooltips) {
        return tooltips;
    }

    /**
     * Retrieves a map of attribute modifiers for the curio.
     * <br>
     * Note that only the identifier is guaranteed to be present in the slot context. For instances
     * where the ItemStack may not be in a curio slot, such as when retrieving item tooltips, the
     * index is -1 and the wearer may be null.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @param uuid        Slot-unique UUID
     * @return A map of attribute modifiers to apply
     */
    default Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
        return HashMultimap.create();
    }

    /**
     * Called server-side when the ItemStack is equipped by using it (i.e. from the hotbar), after
     * calling {@link ICurio#canEquipFromUse(SlotContext)}.
     * <br>
     * Default implementation plays the equip sound from {@link ICurio#getEquipSound(SlotContext)}.
     * This can be overridden to avoid that, but it is advised to always play something as an auditory
     * feedback for players.
     *
     * @param slotContext Context about the slot that the ItemStack was just equipped into
     */
    default void onEquipFromUse(SlotContext slotContext) {

    }

    /**
     * Retrieves the equip sound information for the given slot context.
     *
     * @param slotContext Context about the slot that the ItemStack was just equipped into
     * @return {@link SoundInfo} containing information about the sound event, volume, and pitch
     */
    @NotNull
    default SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo("item.armor.equip.generic", 1.0f, 1.0f);
    }

    /**
     * Determines if the ItemStack can be automatically equipped into the first available slot when
     * used.
     *
     * @param slotContext Context about the slot that the ItemStack is attempting to equip into
     * @return True to enable auto-equipping when the item is used, false to disable
     */
    default boolean canEquipFromUse(SlotContext slotContext) {
        return false;
    }

    /**
     * Called when rendering break animations and sounds client-side when a worn curio item is
     * broken.
     *
     * @param slotContext Context about the slot that the ItemStack broke in
     */
    default void curioBreak(SlotContext slotContext) {
        playBreakAnimation(getStack(), slotContext.entity());
    }

    /**
     * Compares the current ItemStack and the previous ItemStack in the slot to detect any changes and
     * returns true if the change should be synced to all tracking clients. Note that this check
     * occurs every tick so implementations need to code their own timers for other intervals.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @return True to sync the ItemStack change to all tracking clients, false to do nothing
     */
    default boolean canSync(SlotContext slotContext) {
        return false;
    }

    /**
     * Gets a tag that is used to sync extra curio data from the server to the client. Only used when
     * {@link ICurio#canSync(SlotContext)} returns true.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @return Data to be sent to the client
     */
    default NBTTagCompound serializeNBT(SlotContext slotContext) {
        return new NBTTagCompound();
    }

    /**
     * Used client-side to read data tags created by {@link ICurio#serializeNBT(SlotContext)} received from the
     * server.
     *
     * @param slotContext Context about the slot that the ItemStack is in
     * @param compound    Data received from the server
     */
    default void deserializeNBT(SlotContext slotContext, NBTTagCompound compound) {}

    /**
     * Determines if the ItemStack should drop on death and persist through respawn. This will persist
     * the ItemStack in the curio slot to the respawned player if applicable.
     *
     * @param slotContext  Context about the slot that the ItemStack is attempting to equip into
     * @param source       The damage source that killed the wearer and triggered the drop
     * @param lootingLevel The level of looting that triggered the drop
     * @param recentlyHit  Whether or not the wearer was recently hit
     * @return The {@link DropRule} that applies to this curio
     */
    @NotNull
    default DropRule getDropRule(SlotContext slotContext, DamageSource source, int lootingLevel, boolean recentlyHit) {
        return DropRule.DEFAULT;
    }

    /**
     * Retrieves a list of tooltips when displaying curio attribute modifier information returned by
     * {@link ICurio#getAttributeModifiers(SlotContext, UUID)}. By default, this will display a list
     * similar to the vanilla attribute modifier tooltips.
     *
     * @param tooltips A list of {@link String} with the attribute modifier information
     * @return A list of ITextComponent to display as curio attribute modifier information
     */
    default List<String> getAttributesTooltip(List<String> tooltips) {
        return tooltips;
    }

    // /**
    // * Get the amount of bonus Fortune levels that are provided by curio.
    // * Default implementation returns level of Fortune enchantment on ItemStack.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @param lootContext Context for the loot drops
    // * @return Amount of additional Fortune levels that will be applied when mining
    // */
    // default int getFortuneLevel(SlotContext slotContext, @Nullable LootContext lootContext) {
    // return getFortuneBonus(slotContext.identifier(), slotContext.entity(), getStack(),
    // slotContext.index());
    // }
    //
    // /**
    // * Get the amount of bonus Looting levels that are provided by curio.
    // * Default implementation returns level of Looting enchantment on ItemStack.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @param source Damage source that triggers the looting
    // * @param target The target that drops the loot
    // * @param baseLooting The original looting level before bonuses
    // * @return Amount of additional Looting levels that will be applied in LootingLevelEvent
    // */
    // default int getLootingLevel(SlotContext slotContext, DamageSource source, LivingEntity target,
    // int baseLooting) {
    // return getLootingBonus(slotContext.identifier(), slotContext.entity(), getStack(),
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
    // default boolean makesPiglinsNeutral(SlotContext slotContext) {
    // return this.getStack().makesPiglinsNeutral(slotContext.entity());
    // }
    //
    // /**
    // * Determines whether wearing the curio will allow the user to walk on powder snow, in the same manner as
    // * wearing leather boots in vanilla.
    // *
    // * @param slotContext Context about the slot that the ItemStack is in
    // * @return True if the user can walk on powder snow, false otherwise
    // */
    // default boolean canWalkOnPowderedSnow(SlotContext slotContext) {
    // return this.getStack().canWalkOnPowderedSnow(slotContext.entity());
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
    // default boolean isEnderMask(SlotContext slotContext, EnderMan enderMan) {
    //
    // if (slotContext.entity() instanceof Player player) {
    // return this.getStack().isEnderMask(player, enderMan);
    // } else {
    // return false;
    // }
    // }

    enum DropRule {
        DEFAULT,
        ALWAYS_DROP,
        ALWAYS_KEEP,
        DESTROY
    }

    public final record SoundInfo(String soundName, float volume, float pitch) {}

    /*
     * Copy of vanilla implementation for breaking items client-side
     */
    static void playBreakAnimation(ItemStack stack, EntityLivingBase livingEntity) {
        if (stack != null && stack.getItem() != null) {
            livingEntity.worldObj.playSoundEffect(
                livingEntity.posX,
                livingEntity.posY,
                livingEntity.posZ,
                "random.break",
                0.8F,
                0.8F + livingEntity.worldObj.rand.nextFloat() * 0.4F);

            for (int i = 0; i < 5; ++i) {
                Vec3 vec3d = Vec3.createVectorHelper(
                    (livingEntity.getRNG()
                        .nextFloat() - 0.5D) * 0.1D,
                    Math.random() * 0.1D + 0.1D,
                    0.0D);
                vec3d.rotateAroundX(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
                vec3d.rotateAroundY(-livingEntity.rotationYaw * ((float) Math.PI / 180F));
                double d0 = (-livingEntity.getRNG()
                    .nextFloat()) * 0.6D - 0.3D;

                Vec3 vec3d1 = Vec3.createVectorHelper(
                    (livingEntity.getRNG()
                        .nextFloat() - 0.5D) * 0.3D,
                    d0,
                    0.6D);
                vec3d1.rotateAroundX(-livingEntity.rotationPitch * ((float) Math.PI / 180F));
                vec3d1.rotateAroundY(-livingEntity.rotationYaw * ((float) Math.PI / 180F));

                double px = vec3d1.xCoord + livingEntity.posX;
                double py = vec3d1.yCoord + livingEntity.posY + livingEntity.getEyeHeight();
                double pz = vec3d1.zCoord + livingEntity.posZ;

                livingEntity.worldObj.spawnParticle(
                    "iconcrack_" + stack.getItem()
                        .hashCode() + "_" + stack.getItemDamage(),
                    px,
                    py,
                    pz,
                    vec3d.xCoord,
                    vec3d.yCoord + 0.05D,
                    vec3d.zCoord);
            }
        }
    }

    public final class RenderHelpers {

        public static void translateIfSneaking(final EntityLivingBase livingEntity) {
            if (livingEntity.isSneaking()) {
                GL11.glTranslatef(0.0f, 0.2f, 0.0f);
            }
        }

        public static void rotateIfSneaking(final EntityLivingBase livingEntity) {
            if (livingEntity.isSneaking()) {
                GL11.glRotatef(90.0F / (float) Math.PI, 1.0F, 0.0F, 0.0F);
            }
        }

        public static void followHeadRotations(final EntityLivingBase livingEntity, ModelRenderer... renderers) {
            Render render = RenderManager.instance.getEntityRenderObject(livingEntity);

            if (render instanceof RenderLiving livingRenderer) {
                Object model = livingRenderer.mainModel;

                if (model instanceof ModelBiped bipedModel) {
                    for (ModelRenderer renderer : renderers) {
                        renderer.rotateAngleX = bipedModel.bipedHead.rotateAngleX;
                        renderer.rotateAngleY = bipedModel.bipedHead.rotateAngleY;
                        renderer.rotateAngleZ = bipedModel.bipedHead.rotateAngleZ;
                    }
                }
            }
        }

        public static void followBodyRotations(final EntityLivingBase livingEntity, final ModelBiped... models) {
            Render render = RenderManager.instance.getEntityRenderObject(livingEntity);

            if (render instanceof RenderLiving livingRenderer) {
                Object entityModel = livingRenderer.mainModel;

                if (entityModel instanceof ModelBiped target) {
                    for (ModelBiped model : models) {
                        model.onGround = target.onGround;
                        model.isRiding = target.isRiding;
                        model.isChild = target.isChild;
                        model.aimedBow = target.aimedBow;
                        model.isSneak = target.isSneak;
                        model.heldItemLeft = target.heldItemLeft;
                        model.heldItemRight = target.heldItemRight;
                    }
                }
            }
        }
    }
}
