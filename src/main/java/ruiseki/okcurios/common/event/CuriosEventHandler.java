package ruiseki.okcurios.common.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.apache.commons.lang3.tuple.Pair;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.okcore.event.AttachCapabilitiesEvent;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioChangeEvent;
import ruiseki.okcurios.api.event.CurioDropsEvent;
import ruiseki.okcurios.api.event.CurioEquipEvent;
import ruiseki.okcurios.api.event.DropRulesEvent;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurio.DropRule;
import ruiseki.okcurios.api.type.capability.ICurioItem;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.helper.ICuriosHelper;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.CuriosHelper;
import ruiseki.okcurios.common.capability.CurioInventoryCapability;
import ruiseki.okcurios.common.capability.CurioItemCapability;
import ruiseki.okcurios.common.capability.ItemizedCurioCapability;
import ruiseki.okcurios.common.network.PacketSetIcons;
import ruiseki.okcurios.common.network.sync.PacketSyncCurios;
import ruiseki.okcurios.common.network.sync.PacketSyncModifiers;
import ruiseki.okcurios.common.network.sync.PacketSyncStack;

public class CuriosEventHandler {

    public static boolean dirtyTags = false;

    private static void handleDrops(EntityLivingBase livingEntity, List<Pair<Predicate<ItemStack>, DropRule>> dropRules,
        IDynamicStackHandler stacks, Collection<EntityItem> drops, boolean keepInventory) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);

            if (stack != null) {
                DropRule dropRuleOverride = null;

                for (Pair<Predicate<ItemStack>, DropRule> override : dropRules) {

                    if (override.getLeft()
                        .test(stack)) {
                        dropRuleOverride = override.getRight();
                    }
                }
                DropRule dropRule = dropRuleOverride != null ? dropRuleOverride
                    : CuriosApi.getCuriosHelper()
                        .getCurio(stack)
                        .getDropRule(livingEntity);

                if ((dropRule == DropRule.DEFAULT && keepInventory) || dropRule == DropRule.ALWAYS_KEEP) {
                    continue;
                }

                stacks.setStackInSlot(i, null);
            }
        }
    }

    private static EntityItem getDroppedItem(ItemStack droppedItem, EntityLivingBase livingEntity) {
        double d0 = livingEntity.posY - 0.30000001192092896D + livingEntity.getEyeHeight();
        EntityItem entityitem = new EntityItem(
            livingEntity.worldObj,
            livingEntity.posX,
            d0,
            livingEntity.posZ,
            droppedItem);
        entityitem.delayBeforeCanPickup = 40;
        float f = livingEntity.worldObj.rand.nextFloat() * 0.5F;
        float f1 = livingEntity.worldObj.rand.nextFloat() * ((float) Math.PI * 2F);
        entityitem.motionX = (double) (-MathHelper.sin(f1) * f);
        entityitem.motionY = 0.20000000298023224D;
        entityitem.motionZ = (double) (MathHelper.cos(f1) * f);
        return entityitem;
    }

    @SubscribeEvent
    public void playerLoggedIn(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent evt) {
        EntityPlayer playerEntity = evt.player;

        if (playerEntity instanceof EntityPlayerMP playerMP) {
            Collection<ISlotType> slotTypes = CuriosApi.getSlotHelper()
                .getSlotTypes();
            Map<String, ResourceLocation> icons = new HashMap<>();
            slotTypes.forEach(type -> icons.put(type.getIdentifier(), type.getIcon()));
            OKCurios.instance.getPacketHandler()
                .sendToPlayer(new PacketSetIcons(icons), playerMP);
        }
    }

    @SubscribeEvent
    public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getType() == Entity.class) {
            if (evt.getObject() instanceof EntityPlayer entityPlayer) {
                evt.addCapability(CuriosCapability.ID_INVENTORY, CurioInventoryCapability.createProvider(entityPlayer));
            }
        }
    }

    /**
     * Handler for registering item's capabilities implemented through IItemCurio interface.
     */
    @SubscribeEvent
    public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
        if (evt.getType() == ItemStack.class) {
            ItemStack stack = (ItemStack) evt.getObject();
            if (stack.getItem() instanceof ICurioItem curioItem) {
                if (curioItem.hasCurioCapability(stack)) {
                    ItemizedCurioCapability itemizedCapability = new ItemizedCurioCapability(curioItem, stack);
                    evt.addCapability(CuriosCapability.ID_ITEM, CurioItemCapability.createProvider(itemizedCapability));
                }
            }
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent evt) {
        if (evt.entity instanceof EntityPlayerMP playerMP) {
            ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                .getCuriosHandler(playerMP);
            if (handler != null) {
                OKCurios.instance.getPacketHandler()
                    .sendToPlayer(new PacketSyncCurios(playerMP.getEntityId(), handler.getCurios()), playerMP);
            }
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerEvent.StartTracking evt) {
        if (evt.entityPlayer instanceof EntityPlayerMP playerMP && evt.target instanceof EntityLivingBase livingBase) {
            ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                .getCuriosHandler(livingBase);
            if (handler != null) {
                OKCurios.instance.getPacketHandler()
                    .sendToPlayer(new PacketSyncCurios(livingBase.getEntityId(), handler.getCurios()), playerMP);
            }
        }
    }

    @SubscribeEvent
    public void playerClone(PlayerEvent.Clone evt) {
        EntityPlayer player = evt.entityPlayer;
        EntityPlayer oldPlayer = evt.original;

        oldPlayer.isDead = false;

        ICuriosItemHandler oldHandler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(oldPlayer);
        ICuriosItemHandler newHandler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(player);

        if (oldHandler != null && newHandler != null) {
            NBTTagCompound nbt = oldHandler.serializeNBT();
            newHandler.deserializeNBT(nbt);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerDrops(LivingDropsEvent evt) {
        EntityLivingBase livingEntity = evt.entityLiving;

        ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        if (handler != null) {
            ArrayList<EntityItem> drops = evt.drops;
            Collection<EntityItem> curioDrops = new ArrayList<>();
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            DropRulesEvent dropRulesEvent = new DropRulesEvent(
                livingEntity,
                handler,
                evt.source,
                evt.lootingLevel,
                evt.recentlyHit);
            MinecraftForge.EVENT_BUS.post(dropRulesEvent);
            List<Pair<Predicate<ItemStack>, DropRule>> dropRules = dropRulesEvent.getOverrides();

            boolean keepInventory = livingEntity.worldObj.getGameRules()
                .getGameRuleBooleanValue("keepInventory");

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();

                handleDrops(livingEntity, dropRules, stacksHandler.getStacks(), curioDrops, keepInventory);
                handleDrops(livingEntity, dropRules, stacksHandler.getCosmeticStacks(), curioDrops, keepInventory);
            }

            CurioDropsEvent curioDropsEvent = new CurioDropsEvent(
                livingEntity,
                handler,
                evt.source,
                curioDrops,
                evt.lootingLevel,
                evt.recentlyHit);

            if (!MinecraftForge.EVENT_BUS.post(curioDropsEvent)) {
                drops.addAll(curioDrops);
            }
        }
    }

    @SubscribeEvent
    public void curioRightClick(PlayerInteractEvent evt) {
        if (evt.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;

        EntityPlayer player = evt.entityPlayer;
        ItemStack stack = player.getCurrentEquippedItem();

        if (stack == null) return;

        var curiosHelper = CuriosApi.getCuriosHelper();
        ICurio curio = curiosHelper.getCurio(stack);
        ICuriosItemHandler handler = curiosHelper.getCuriosHandler(player);

        if (curio != null && handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                IDynamicStackHandler stackHandler = entry.getValue()
                    .getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    String id = entry.getKey();

                    SlotContext slotContext = new SlotContext(id, player, i);
                    CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
                    net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(equipEvent);
                    Event.Result result = equipEvent.getResult();

                    if (result == Event.Result.DENY) {
                        continue;
                    }

                    if (result == Event.Result.ALLOW
                        || (curiosHelper.isStackValid(slotContext, stack) && curio.canEquip(id, player)
                            && curio.canEquipFromUse(slotContext))) {

                        ItemStack present = stackHandler.getStackInSlot(i);

                        if (present == null) {
                            stackHandler.setStackInSlot(i, stack.copy());
                            curio.onEquipFromUse(slotContext);

                            if (!player.capabilities.isCreativeMode) {
                                int count = stack.stackSize;
                                stack.stackSize -= count;

                                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                            }

                            evt.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END && evt.world instanceof WorldServer server && dirtyTags) {

            List<?> players = server.playerEntities;
            ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();

            for (Object obj : players) {
                if (obj instanceof EntityPlayerMP player) {
                    ICuriosItemHandler handler = curiosHelper.getCuriosHandler(player);

                    if (handler != null) {
                        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios()
                            .entrySet()) {
                            ICurioStacksHandler stacksHandler = entry.getValue();
                            String id = entry.getKey();

                            IDynamicStackHandler stacks = stacksHandler.getStacks();
                            IDynamicStackHandler cosmeticStacks = stacksHandler.getCosmeticStacks();

                            replaceInvalidStacks(curiosHelper, player, id, stacks);
                            replaceInvalidStacks(curiosHelper, player, id, cosmeticStacks);
                        }
                    }
                }
            }
            dirtyTags = false;
        }
    }

    private static void replaceInvalidStacks(ICuriosHelper curiosHelper, EntityPlayerMP player, String id,
        IDynamicStackHandler stacks) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            SlotContext slotContext = new SlotContext(id, player, i);

            if (stack != null && !curiosHelper.isStackValid(slotContext, stack)) {
                stacks.setStackInSlot(i, null);
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
    }

    @SubscribeEvent
    public void tick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase livingEntity = evt.entityLiving;

        ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        if (handler != null) {
            handler.clearCachedSlotModifiers();
            handler.handleInvalidStacks();
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            int totalFortuneBonus = 0;
            int totalLootingBonus = 0;

            BaseAttributeMap attributeMap = livingEntity.getAttributeMap();

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();
                String identifier = entry.getKey();
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    SlotContext slotContext = new SlotContext(identifier, livingEntity, i);
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    ICurio currentCurio = CuriosApi.getCuriosHelper()
                        .getCurio(stack);

                    if (stack != null && stack.getItem() != null) {
                        stack.getItem()
                            .onUpdate(stack, livingEntity.worldObj, livingEntity, -1, false);

                        if (currentCurio != null) {
                            currentCurio.curioTick(identifier, i, livingEntity);
                            if (livingEntity.worldObj.isRemote) {
                                currentCurio.curioAnimate(identifier, i, livingEntity);
                            }

                            totalFortuneBonus += currentCurio.getFortuneBonus(identifier, livingEntity, stack, i);
                            totalLootingBonus += currentCurio.getLootingBonus(identifier, livingEntity, stack, i);
                        }
                    }

                    if (!livingEntity.worldObj.isRemote) {
                        ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

                        if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
                            ICurio prevCurio = CuriosApi.getCuriosHelper()
                                .getCurio(prevStack);

                            syncCurios(
                                livingEntity,
                                stack,
                                currentCurio,
                                prevCurio,
                                identifier,
                                i,
                                PacketSyncStack.HandlerType.EQUIPMENT);

                            MinecraftForge.EVENT_BUS
                                .post(new CurioChangeEvent(livingEntity, identifier, i, prevStack, stack));
                            UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());

                            if (prevStack != null) {
                                Multimap<IAttribute, AttributeModifier> map = CuriosApi.getCuriosHelper()
                                    .getAttributeModifiers(slotContext, uuid, prevStack);
                                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                                for (IAttribute attribute : map.keySet()) {
                                    if (attribute instanceof CuriosHelper.SlotAttributeWrapper) {
                                        CuriosHelper.SlotAttributeWrapper wrapper = (CuriosHelper.SlotAttributeWrapper) attribute;
                                        slots.putAll(wrapper.identifier, map.get(attribute));
                                        toRemove.add(wrapper);
                                    }
                                }

                                for (IAttribute attribute : toRemove) {
                                    map.removeAll(attribute);
                                }

                                if (attributeMap != null) {
                                    Multimap<String, AttributeModifier> convertedModifiers = HashMultimap.create();
                                    for (Map.Entry<IAttribute, AttributeModifier> entry1 : map.entries()) {
                                        IAttribute attribute = entry1.getKey();
                                        AttributeModifier modifier = entry1.getValue();

                                        if (attribute != null && modifier != null) {
                                            String attributeName = attribute.getAttributeUnlocalizedName();
                                            convertedModifiers.put(attributeName, modifier);
                                        }
                                    }
                                    attributeMap.removeAttributeModifiers(convertedModifiers);
                                }
                                handler.removeSlotModifiers(slots);

                                if (prevCurio != null) {
                                    prevCurio.onUnequip(slotContext, stack);
                                }
                            }

                            if (stack != null) {
                                Multimap<IAttribute, AttributeModifier> map = CuriosApi.getCuriosHelper()
                                    .getAttributeModifiers(slotContext, uuid, stack);
                                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<>();

                                for (IAttribute attribute : map.keySet()) {
                                    if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                                        slots.putAll(wrapper.identifier, map.get(attribute));
                                        toRemove.add(wrapper);
                                    }
                                }

                                for (IAttribute attribute : toRemove) {
                                    map.removeAll(attribute);
                                }

                                if (attributeMap != null) {
                                    Multimap<String, AttributeModifier> convertedModifiers = HashMultimap.create();
                                    for (Map.Entry<IAttribute, AttributeModifier> entry1 : map.entries()) {
                                        IAttribute attribute = entry1.getKey();
                                        AttributeModifier modifier = entry1.getValue();

                                        if (attribute != null && modifier != null) {
                                            String attributeName = attribute.getAttributeUnlocalizedName();
                                            convertedModifiers.put(attributeName, modifier);
                                        }
                                    }
                                    attributeMap.removeAttributeModifiers(convertedModifiers);
                                }
                                handler.addTransientSlotModifiers(slots);

                                if (currentCurio != null) {
                                    currentCurio.onEquip(slotContext, prevStack);
                                }

                                if (livingEntity instanceof EntityPlayerMP) {
                                    // EquipCurioTrigger.INSTANCE.trigger((ServerPlayerEntity) livingEntity, stack,
                                    // (ServerWorld) livingEntity.world, livingEntity.getPosX(),
                                    // livingEntity.getPosY(), livingEntity.getPosZ());
                                }
                            }
                            stackHandler.setPreviousStackInSlot(i, stack.copy());
                        }

                        ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(i);
                        ItemStack prevCosmeticStack = cosmeticStackHandler.getPreviousStackInSlot(i);

                        if (!ItemStack.areItemStacksEqual(cosmeticStack, prevCosmeticStack)) {
                            syncCurios(
                                livingEntity,
                                cosmeticStack,
                                CuriosApi.getCuriosHelper()
                                    .getCurio(cosmeticStack),
                                CuriosApi.getCuriosHelper()
                                    .getCurio(prevCosmeticStack),
                                identifier,
                                i,
                                PacketSyncStack.HandlerType.COSMETIC);
                            cosmeticStackHandler
                                .setPreviousStackInSlot(i, cosmeticStack != null ? cosmeticStack.copy() : null);
                        }
                    }
                }
            }
            handler.processSlots();

            handler.setEnchantmentBonuses(totalFortuneBonus, totalLootingBonus);

            if (!livingEntity.worldObj.isRemote) {
                Set<ICurioStacksHandler> updates = handler.getUpdatingInventories();

                if (!updates.isEmpty()) {
                    PacketSyncModifiers packet = new PacketSyncModifiers(livingEntity.getEntityId(), updates);

                    OKCurios.instance.getPacketHandler()
                        .sendToAllAround(
                            packet,
                            new NetworkRegistry.TargetPoint(
                                livingEntity.dimension,
                                livingEntity.posX,
                                livingEntity.posY,
                                livingEntity.posZ,
                                64.0D));

                    // Gửi riêng cho chính chủ nếu là Player
                    if (livingEntity instanceof EntityPlayerMP) {
                        OKCurios.instance.getPacketHandler()
                            .sendToPlayer(packet, (EntityPlayerMP) livingEntity);
                    }
                    updates.clear();
                }
            }
        }
    }

    private static void syncCurios(EntityLivingBase livingEntity, ItemStack stack, ICurio currentCurio,
        ICurio prevCurio, String identifier, int index, PacketSyncStack.HandlerType type) {
        boolean syncable = (currentCurio != null && currentCurio.canSync(identifier, index, livingEntity))
            || (prevCurio != null && prevCurio.canSync(identifier, index, livingEntity));

        NBTTagCompound syncTag = new NBTTagCompound();

        if (syncable && currentCurio != null) {
            NBTTagCompound tag = currentCurio.serializeNBT();
            if (tag != null) {
                syncTag = tag;
            }
        }

        PacketSyncStack packet = new PacketSyncStack(
            livingEntity.getEntityId(),
            identifier,
            index,
            stack,
            type,
            syncTag);

        OKCurios.instance.getPacketHandler()
            .sendToAllAround(
                packet,
                new NetworkRegistry.TargetPoint(
                    livingEntity.dimension,
                    livingEntity.posX,
                    livingEntity.posY,
                    livingEntity.posZ,
                    64.0D));

        if (livingEntity instanceof EntityPlayerMP playerMP) {
            OKCurios.instance.getPacketHandler()
                .sendToPlayer(packet, playerMP);
        }
    }
}
