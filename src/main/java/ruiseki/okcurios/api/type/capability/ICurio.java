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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcore.persist.nbt.INBTSerializable;
import ruiseki.okcurios.api.SlotContext;

public interface ICurio extends INBTSerializable {

    /*
     * Implementation for breaking items client-side
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

    default void curioTick(String identifier, int index, EntityLivingBase livingEntity) {}

    default void curioAnimate(String identifier, int index, EntityLivingBase livingEntity) {}

    default void onEquip(SlotContext slotContext, ItemStack prevStack) {}

    default void onUnequip(SlotContext slotContext, ItemStack newStack) {}

    default boolean canEquip(String identifier, EntityLivingBase livingEntity) {
        return true;
    }

    default boolean canUnequip(String identifier, EntityLivingBase livingEntity) {
        return true;
    }

    default List<String> getTagsTooltip(List<String> tagTooltips) {
        return tagTooltips;
    }

    default Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
        return HashMultimap.create();
    }

    default void onEquipFromUse(SlotContext slotContext) {
        SoundInfo soundInfo = getEquipSound(slotContext);
        if (slotContext.wearer() != null && soundInfo != null) {
            slotContext.wearer().worldObj
                .playSoundAtEntity(slotContext.wearer(), soundInfo.soundName(), soundInfo.volume(), soundInfo.pitch());
        }
    }

    default SoundInfo getEquipSound(SlotContext slotContext) {
        return new SoundInfo("item.armor.equip.generic", 1.0f, 1.0f);
    }

    default boolean canEquipFromUse(SlotContext slotContext) {
        return false;
    }

    default void curioBreak(ItemStack stack, EntityLivingBase livingEntity) {
        playBreakAnimation(stack, livingEntity);
    }

    default boolean canSync(String identifier, int index, EntityLivingBase livingEntity) {
        return false;
    }

    @Override
    default NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    default void deserializeNBT(NBTTagCompound nbt) {}

    default DropRule getDropRule(EntityLivingBase livingEntity) {
        return DropRule.DEFAULT;
    }

    default boolean showAttributesTooltip(String identifier) {
        return true;
    }

    default int getFortuneBonus(String identifier, EntityLivingBase livingEntity, ItemStack curio, int index) {
        return EnchantmentHelper.getEnchantmentLevel(35, curio);
    }

    default int getLootingBonus(String identifier, EntityLivingBase livingEntity, ItemStack curio, int index) {
        return EnchantmentHelper.getEnchantmentLevel(21, curio);
    }

    default boolean canRender(String identifier, int index, EntityLivingBase livingEntity) {
        return false;
    }

    default void render(String identifier, int index, EntityLivingBase livingEntity, float limbSwing,
        float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {}

    enum DropRule {
        DEFAULT,
        ALWAYS_DROP,
        ALWAYS_KEEP,
        DESTROY
    }

    public final record SoundInfo(String soundName, float volume, float pitch) {}

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
