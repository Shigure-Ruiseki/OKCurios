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

import baubles.api.IBauble;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.event.capabilities.AttachCapabilitiesEvent;
import ruiseki.okcore.helper.ItemStackHelpers;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotAttribute;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioChangeEvent;
import ruiseki.okcurios.api.event.CurioDropsEvent;
import ruiseki.okcurios.api.event.CurioEquipEvent;
import ruiseki.okcurios.api.event.CurioUnequipEvent;
import ruiseki.okcurios.api.event.DropRulesEvent;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurio.DropRule;
import ruiseki.okcurios.api.type.capability.ICurioItem;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.capability.CurioInventoryCapability;
import ruiseki.okcurios.common.capability.CurioItemCapability;
import ruiseki.okcurios.common.capability.ItemizedCurioCapability;
import ruiseki.okcurios.common.network.server.SPacketSetIcons;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncCurios;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncModifiers;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncStack;
import ruiseki.okcurios.compat.BaubleToCurioCapabilityWrapper;

public class CuriosEventHandler {

    public static boolean dirtyTags = false;

    private static void handleDrops(String identifier, EntityLivingBase livingEntity,
        List<Pair<Predicate<ItemStack>, DropRule>> dropRules, NonNullList<Boolean> renders, IDynamicStackHandler stacks,
        boolean cosmetic, Collection<EntityItem> drops, boolean keepInventory, LivingDropsEvent evt) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            SlotContext slotContext = new SlotContext(
                identifier,
                livingEntity,
                i,
                cosmetic,
                renders.size() > i && renders.get(i));

            if (stack != null) {
                DropRule dropRuleOverride = null;

                for (Pair<Predicate<ItemStack>, DropRule> override : dropRules) {

                    if (override.getLeft()
                        .test(stack)) {
                        dropRuleOverride = override.getRight();
                    }
                }
                DropRule dropRule = dropRuleOverride != null ? dropRuleOverride
                    : CuriosApi.getCurio(stack)
                        .map(curio -> curio.getDropRule(slotContext, evt.source, evt.lootingLevel, evt.recentlyHit))
                        .orElse(DropRule.DEFAULT);

                if (dropRule == DropRule.DEFAULT) {
                    // TODO: Add Data Loader
                    // dropRule = CuriosSlotManager.INSTANCE.getSlot(identifier).map(ISlotType::getDropRule)
                    // .orElse(DropRule.DEFAULT);
                }

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
            Collection<ISlotType> slotTypes = CuriosApi.getPlayerSlots()
                .values();
            Map<String, ResourceLocation> icons = new HashMap<>();
            slotTypes.forEach(type -> icons.put(type.getIdentifier(), type.getIcon()));
            OKCurios.instance.getPacketHandler()
                .sendToPlayer(new SPacketSetIcons(icons), playerMP);
        }
    }

    @SubscribeEvent
    public void attachEntitiesCapabilities(AttachCapabilitiesEvent<Entity> evt) {
        if (evt.getObject() instanceof EntityPlayer entityPlayer) {
            evt.addCapability(CuriosCapability.ID_INVENTORY, CurioInventoryCapability.createProvider(entityPlayer));
        }
    }

    /**
     * Handler for registering item's capabilities implemented through IItemCurio interface.
     */
    @SubscribeEvent
    public void attachStackCapabilities(AttachCapabilitiesEvent<ItemStack> evt) {
        ItemStack stack = evt.getObject();
        if (stack == null || stack.getItem() == null) return;

        if (stack.getItem() instanceof ICurioItem curioItem) {
            if (curioItem.hasCurioCapability(stack)) {
                ItemizedCurioCapability itemizedCapability = new ItemizedCurioCapability(curioItem, stack);
                evt.addCapability(CuriosCapability.ID_ITEM, CurioItemCapability.createProvider(itemizedCapability));
            }
        } else if (stack.getItem() instanceof IBauble bauble) {
            BaubleToCurioCapabilityWrapper baubleWrapper = new BaubleToCurioCapabilityWrapper(stack, bauble);
            evt.addCapability(CuriosCapability.ID_ITEM, CurioItemCapability.createProvider(baubleWrapper));
        }
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent evt) {
        if (evt.entity instanceof EntityPlayerMP playerMP) {
            CuriosApi.getCuriosInventory(playerMP)
                .ifPresent(handler -> {
                    OKCurios.instance.getPacketHandler()
                        .sendToPlayer(new SPacketSyncCurios(playerMP.getEntityId(), handler.getCurios()), playerMP);
                });
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerEvent.StartTracking evt) {
        if (evt.entityPlayer instanceof EntityPlayerMP playerMP && evt.target instanceof EntityLivingBase livingBase) {
            CuriosApi.getCuriosInventory(livingBase)
                .ifPresent(handler -> {
                    OKCurios.instance.getPacketHandler()
                        .sendToPlayer(new SPacketSyncCurios(livingBase.getEntityId(), handler.getCurios()), playerMP);
                });
        }
    }

    @SubscribeEvent
    public void playerClone(PlayerEvent.Clone evt) {
        EntityPlayer player = evt.entityPlayer;
        EntityPlayer oldPlayer = evt.original;

        oldPlayer.isDead = false;

        LazyOptional<ICuriosItemHandler> oldHandler = CuriosApi.getCuriosInventory(oldPlayer);
        LazyOptional<ICuriosItemHandler> newHandler = CuriosApi.getCuriosInventory(player);
        oldHandler.ifPresent(
            oldCurios -> newHandler.ifPresent(newCurios -> newCurios.deserializeNBT(oldCurios.serializeNBT())));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void playerDrops(LivingDropsEvent evt) {
        EntityLivingBase livingEntity = evt.entityLiving;

        CuriosApi.getCuriosInventory(livingEntity)
            .ifPresent(handler -> {
                Collection<EntityItem> drops = evt.drops;
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
                boolean keepInventory = false;
                if (livingEntity instanceof EntityPlayer player) {
                    keepInventory = player.worldObj.getGameRules()
                        .getGameRuleBooleanValue("keepInventory");
                }
                boolean finalKeepInventory = keepInventory;
                curios.forEach((id, stacksHandler) -> {
                    handleDrops(
                        id,
                        livingEntity,
                        dropRules,
                        stacksHandler.getRenders(),
                        stacksHandler.getStacks(),
                        false,
                        curioDrops,
                        finalKeepInventory,
                        evt);
                    handleDrops(
                        id,
                        livingEntity,
                        dropRules,
                        stacksHandler.getRenders(),
                        stacksHandler.getCosmeticStacks(),
                        true,
                        curioDrops,
                        finalKeepInventory,
                        evt);
                });
                if (!MinecraftForge.EVENT_BUS.post(
                    new CurioDropsEvent(
                        livingEntity,
                        handler,
                        evt.source,
                        curioDrops,
                        evt.lootingLevel,
                        evt.recentlyHit))) {
                    drops.addAll(curioDrops);
                }
            });
    }

    @SubscribeEvent
    public void curioRightClick(PlayerInteractEvent evt) {
        if (evt.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;

        EntityPlayer player = evt.entityPlayer;
        ItemStack stack = player.getCurrentEquippedItem();
        CuriosApi.getCurio(stack)
            .ifPresent(
                curio -> CuriosApi.getCuriosInventory(player)
                    .ifPresent(handler -> {
                        Map<String, ICurioStacksHandler> curios = handler.getCurios();
                        Pair<IDynamicStackHandler, SlotContext> firstSlot = null;

                        for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                            IDynamicStackHandler stackHandler = entry.getValue()
                                .getStacks();

                            for (int i = 0; i < stackHandler.getSlots(); i++) {
                                String id = entry.getKey();
                                NonNullList<Boolean> renderStates = entry.getValue()
                                    .getRenders();
                                SlotContext slotContext = new SlotContext(
                                    id,
                                    player,
                                    i,
                                    false,
                                    renderStates.size() > i && renderStates.get(i));
                                CurioEquipEvent equipEvent = new CurioEquipEvent(stack, slotContext);
                                MinecraftForge.EVENT_BUS.post(equipEvent);
                                Event.Result result = equipEvent.getResult();

                                if (result == Event.Result.DENY) {
                                    continue;
                                }

                                if (result == Event.Result.ALLOW
                                    || (CuriosApi.isStackValid(slotContext, stack) && curio.canEquip(slotContext)
                                        && curio.canEquipFromUse(slotContext))) {
                                    ItemStack present = stackHandler.getStackInSlot(i);

                                    if (present == null) {
                                        stackHandler.setStackInSlot(i, stack.copy());
                                        curio.onEquipFromUse(slotContext);

                                        if (!player.capabilities.isCreativeMode) {
                                            int count = stack.stackSize;
                                            ItemStackHelpers.shrink(stack, count);
                                        }
                                        evt.setCanceled(true);
                                        return;
                                    } else if (firstSlot == null) {
                                        CurioUnequipEvent unequipEvent = new CurioUnequipEvent(present, slotContext);
                                        MinecraftForge.EVENT_BUS.post(unequipEvent);
                                        result = unequipEvent.getResult();

                                        if (result == Event.Result.DENY) {
                                            continue;
                                        }

                                        if (result == Event.Result.ALLOW || CuriosApi.getCurio(present)
                                            .map(c -> c.canUnequip(slotContext))
                                            .orElse(true)) {
                                            firstSlot = Pair.of(stackHandler, slotContext);
                                        }
                                    }
                                }
                            }
                        }

                        if (firstSlot != null) {
                            IDynamicStackHandler stackHandler = firstSlot.getLeft();
                            SlotContext slotContext = firstSlot.getRight();
                            int i = slotContext.index();
                            ItemStack present = stackHandler.getStackInSlot(i);
                            stackHandler.setStackInSlot(i, stack.copy());
                            curio.onEquipFromUse(slotContext);
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, present.copy());
                            evt.setCanceled(true);
                        }
                    }));
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END && evt.world instanceof WorldServer server && dirtyTags) {

            List<EntityPlayer> players = server.playerEntities;
            for (EntityPlayer player : players) {
                CuriosApi.getCuriosInventory(player)
                    .ifPresent(handler -> {

                        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios()
                            .entrySet()) {
                            ICurioStacksHandler stacksHandler = entry.getValue();
                            String id = entry.getKey();
                            IDynamicStackHandler stacks = stacksHandler.getStacks();
                            IDynamicStackHandler cosmeticStacks = stacksHandler.getCosmeticStacks();
                            replaceInvalidStacks(player, id, stacks, false, stacksHandler.getRenders());
                            replaceInvalidStacks(player, id, cosmeticStacks, true, stacksHandler.getRenders());
                        }
                    });
            }
            dirtyTags = false;
        }
    }

    private static void replaceInvalidStacks(EntityPlayer player, String id, IDynamicStackHandler stacks,
        boolean cosmetic, NonNullList<Boolean> renders) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            SlotContext slotContext = new SlotContext(id, player, i, cosmetic, renders.size() > i && renders.get(i));

            if (stack != null && !CuriosApi.isStackValid(slotContext, stack)) {
                stacks.setStackInSlot(i, null);
                ItemHandlerHelper.giveItemToPlayer(player, stack);
            }
        }
    }

    @SubscribeEvent
    public void tick(LivingEvent.LivingUpdateEvent evt) {
        EntityLivingBase livingEntity = evt.entityLiving;

        CuriosApi.getCuriosInventory(livingEntity)
            .ifPresent(handler -> {
                handler.clearCachedSlotModifiers();
                handler.handleInvalidStacks();
                Map<String, ICurioStacksHandler> curios = handler.getCurios();

                BaseAttributeMap attributeMap = livingEntity.getAttributeMap();

                for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                    ICurioStacksHandler stacksHandler = entry.getValue();
                    String identifier = entry.getKey();
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    IDynamicStackHandler cosmeticStackHandler = stacksHandler.getCosmeticStacks();

                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        SlotContext slotContext = new SlotContext(
                            identifier,
                            livingEntity,
                            i,
                            false,
                            renderStates.size() > i && renderStates.get(i));
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        LazyOptional<ICurio> currentCurio = CuriosApi.getCurio(stack);
                        final int index = i;

                        if (stack != null && stack.getItem() != null) {
                            stack.getItem()
                                .onUpdate(stack, livingEntity.worldObj, livingEntity, -1, false);
                            currentCurio.ifPresent(curio -> curio.curioTick(slotContext));
                        }

                        if (!livingEntity.worldObj.isRemote) {
                            ItemStack prevStack = stackHandler.getPreviousStackInSlot(i);

                            if (!ItemStack.areItemStacksEqual(stack, prevStack)) {
                                LazyOptional<ICurio> prevCurio = CuriosApi.getCurio(prevStack);
                                syncCurios(
                                    livingEntity,
                                    stack,
                                    currentCurio,
                                    prevCurio,
                                    identifier,
                                    index,
                                    false,
                                    renderStates.size() > index && renderStates.get(index),
                                    SPacketSyncStack.HandlerType.EQUIPMENT);
                                MinecraftForge.EVENT_BUS
                                    .post(new CurioChangeEvent(livingEntity, identifier, i, prevStack, stack));
                                UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());

                                if (prevStack != null) {
                                    Multimap<IAttribute, AttributeModifier> map = CuriosApi
                                        .getAttributeModifiers(slotContext, uuid, prevStack);
                                    Multimap<String, AttributeModifier> slots = HashMultimap.create();
                                    Set<SlotAttribute> toRemove = new HashSet<>();

                                    for (IAttribute attribute : map.keySet()) {

                                        if (attribute instanceof SlotAttribute wrapper) {
                                            slots.putAll(wrapper.getIdentifier(), map.get(attribute));
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
                                    prevCurio.ifPresent(curio -> curio.onUnequip(slotContext, stack));
                                }

                                if (stack != null) {
                                    Multimap<IAttribute, AttributeModifier> map = CuriosApi
                                        .getAttributeModifiers(slotContext, uuid, stack);
                                    Multimap<String, AttributeModifier> slots = HashMultimap.create();
                                    Set<SlotAttribute> toRemove = new HashSet<>();

                                    for (IAttribute attribute : map.keySet()) {

                                        if (attribute instanceof SlotAttribute wrapper) {
                                            slots.putAll(wrapper.getIdentifier(), map.get(attribute));
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
                                    currentCurio.ifPresent(curio -> curio.onEquip(slotContext, prevStack));

                                    if (livingEntity instanceof EntityPlayer entityPlayer) {
                                        // TODO: Add Trigger
                                        // EquipCurioTrigger.INSTANCE.trigger(
                                        // (ServerPlayer) livingEntity,
                                        // stack,
                                        // (ServerLevel) livingEntity.level(),
                                        // livingEntity.getX(),
                                        // livingEntity.getY(),
                                        // livingEntity.getZ());
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
                                    CuriosApi.getCurio(cosmeticStack),
                                    CuriosApi.getCurio(prevCosmeticStack),
                                    identifier,
                                    index,
                                    true,
                                    true,
                                    SPacketSyncStack.HandlerType.COSMETIC);
                                cosmeticStackHandler.setPreviousStackInSlot(index, cosmeticStack.copy());
                            }
                            Set<ICurioStacksHandler> updates = handler.getUpdatingInventories();

                            if (!updates.isEmpty()) {
                                OKCurios.instance.getPacketHandler()
                                    .sendToTrackingAndSelf(
                                        new SPacketSyncModifiers(livingEntity.getEntityId(), updates),
                                        livingEntity);

                                updates.clear();
                            }
                        }
                    }
                }
            });
    }

    private static void syncCurios(EntityLivingBase livingEntity, ItemStack stack, LazyOptional<ICurio> currentCurio,
        LazyOptional<ICurio> prevCurio, String identifier, int index, boolean cosmetic, boolean visible,
        SPacketSyncStack.HandlerType type) {
        SlotContext slotContext = new SlotContext(identifier, livingEntity, index, cosmetic, visible);
        boolean syncable = currentCurio.map(curio -> curio.canSync(slotContext))
            .orElse(false)
            || prevCurio.map(curio -> curio.canSync(slotContext))
                .orElse(false);
        NBTTagCompound syncTag = syncable ? currentCurio.map(curio -> {
            NBTTagCompound tag = curio.serializeNBT(slotContext);
            return tag != null ? tag : new NBTTagCompound();
        })
            .orElse(new NBTTagCompound()) : new NBTTagCompound();

        OKCurios.instance.getPacketHandler()
            .sendToTrackingAndSelf(
                new SPacketSyncStack(livingEntity.getEntityId(), identifier, index, stack, type, syncTag),
                livingEntity);
    }
}
