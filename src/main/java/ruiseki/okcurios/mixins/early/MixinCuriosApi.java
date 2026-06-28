package ruiseki.okcurios.mixins.early;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Multimap;

import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurioItem;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.common.capability.CurioItemCapability;
import ruiseki.okcurios.mixins.CuriosImplMixinHooks;

@Mixin(value = CuriosApi.class, remap = false)
public class MixinCuriosApi {

    @Inject(at = @At("HEAD"), method = "registerCurio", cancellable = true)
    private static void curios$registerCurio(Item item, ICurioItem icurio, CallbackInfo ci) {
        CuriosImplMixinHooks.registerCurio(item, icurio);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "getSlot", cancellable = true)
    private static void curios$getSlot(String id, CallbackInfoReturnable<Optional<ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getSlot(id));
    }

    @Inject(at = @At("HEAD"), method = "getSlotIcon", cancellable = true)
    private static void curios$getSlotIcon(String id, CallbackInfoReturnable<ResourceLocation> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getSlotIcon(id));
    }

    @Inject(at = @At("HEAD"), method = "getSlots", cancellable = true)
    private static void curios$getSlots(CallbackInfoReturnable<Map<String, ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getSlots());
    }

    @Inject(at = @At("HEAD"), method = "getPlayerSlots", cancellable = true)
    private static void curios$getPlayerSlots(CallbackInfoReturnable<Map<String, ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getPlayerSlots());
    }

    @Inject(at = @At("HEAD"), method = "getEntitySlots", cancellable = true)
    private static void curios$getEntitySlots(Class<? extends Entity> type,
        CallbackInfoReturnable<Map<String, ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getEntitySlots(type));
    }

    @Inject(
        at = @At("HEAD"),
        method = "getItemStackSlots(Lnet/minecraft/item/ItemStack;)Ljava/util/Map;",
        cancellable = true)
    private static void curios$getItemStackSlots(ItemStack stack, CallbackInfoReturnable<Map<String, ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getItemStackSlots(stack));
    }

    @Inject(
        at = @At("HEAD"),
        method = "getItemStackSlots(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;)Ljava/util/Map;",
        cancellable = true)
    private static void curios$getItemStackSlots(ItemStack stack, EntityLivingBase livingEntity,
        CallbackInfoReturnable<Map<String, ISlotType>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getItemStackSlots(stack, livingEntity));
    }

    @Inject(at = @At("HEAD"), method = "getCurio", cancellable = true)
    private static void curios$getCurio(ItemStack stack, CallbackInfoReturnable<LazyOptional<ICurio>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getCurio(stack));
    }

    @Inject(at = @At("HEAD"), method = "createCurioProvider", cancellable = true)
    private static void curios$createCurio(ICurio curio, CallbackInfoReturnable<ICapabilityProvider> cir) {
        cir.setReturnValue(CurioItemCapability.createProvider(curio));
    }

    @Inject(at = @At("HEAD"), method = "getCuriosInventory", cancellable = true)
    private static void curios$getCuriosInventory(EntityLivingBase livingEntity,
        CallbackInfoReturnable<LazyOptional<ICuriosItemHandler>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getCuriosInventory(livingEntity));
    }

    @Inject(at = @At("HEAD"), method = "isStackValid", cancellable = true)
    private static void curios$isStackValid(SlotContext slotContext, ItemStack stack,
        CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.isStackValid(slotContext, stack));
    }

    @Inject(at = @At("HEAD"), method = "getAttributeModifiers", cancellable = true)
    private static void curios$getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack,
        CallbackInfoReturnable<Multimap<IAttribute, AttributeModifier>> cir) {
        cir.setReturnValue(CuriosImplMixinHooks.getAttributeModifiers(slotContext, uuid, stack));
    }

    @Inject(
        at = @At("HEAD"),
        method = "addSlotModifier(Lcom/google/common/collect/Multimap;Ljava/lang/String;Ljava/util/UUID;DI)V",
        cancellable = true,
        remap = false)
    private static void curios$addSlotModifierToMap(Multimap<IAttribute, AttributeModifier> map, String identifier,
        UUID uuid, double amount, int operation, CallbackInfo ci) {
        CuriosImplMixinHooks.addSlotModifier(map, identifier, uuid, amount, operation);
        ci.cancel();
    }

    @Inject(
        at = @At("HEAD"),
        method = "addModifier(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ai/attributes/IAttribute;Ljava/lang/String;Ljava/util/UUID;DILjava/lang/String;)V",
        cancellable = true,
        remap = false)
    private static void curios$addModifier(ItemStack stack, IAttribute attribute, String name, UUID uuid, double amount,
        int operation, String slot, CallbackInfo ci) {
        CuriosImplMixinHooks.addModifier(stack, attribute, name, uuid, amount, operation, slot);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "broadcastCurioBreakEvent", cancellable = true)
    private static void curios$broadcastCurioBreakEvent(SlotContext slotContext, CallbackInfo ci) {
        CuriosImplMixinHooks.broadcastCurioBreakEvent(slotContext);
        ci.cancel();
    }
}
