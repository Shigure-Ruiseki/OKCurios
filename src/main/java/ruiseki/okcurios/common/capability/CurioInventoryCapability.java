package ruiseki.okcurios.common.capability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityManager;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.capabilities.ICapabilitySerializable;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.init.IInitListener;
import ruiseki.okcore.item.CombinedInvWrapper;
import ruiseki.okcore.item.IItemHandlerModifiable;
import ruiseki.okcore.item.ItemStackHandler;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotAttribute;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.SlotResult;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step != Step.PREINIT) return;
        CapabilityManager.INSTANCE.register(ICuriosItemHandler.class);
    }

    public static ICapabilityProvider createProvider(final EntityPlayer playerEntity) {
        return new Provider(playerEntity);
    }

    public static class CurioInventoryWrapper implements ICuriosItemHandler {

        Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
        NonNullList<ItemStack> invalidStacks = NonNullList.create();
        EntityLivingBase wearer;
        Set<ICurioStacksHandler> updates = new HashSet<>();

        public CurioInventoryWrapper(final EntityLivingBase livingEntity) {
            this.wearer = livingEntity;
            this.reset();
        }

        @Override
        public void reset() {
            if (this.wearer != null) {
                this.curios.clear();
                this.invalidStacks.clear();

                if (!this.wearer.worldObj.isRemote) {
                    SortedSet<ISlotType> sorted = new TreeSet<>(
                        CuriosApi.getEntitySlots(this.wearer.getClass())
                            .values());

                    for (ISlotType slotType : sorted) {
                        this.curios.put(
                            slotType.getIdentifier(),
                            new CurioStacksHandler(
                                this,
                                slotType.getIdentifier(),
                                slotType.getSize(),
                                slotType.useNativeGui(),
                                slotType.hasCosmetic(),
                                slotType.canToggleRendering(),
                                slotType.getDropRule()));
                    }
                } else {
                    // TODO: Add Data Loader
                    // Map<String, Integer> slots = CuriosEntityManager.INSTANCE.getClientSlots(this.wearer.getClass());
                    //
                    // for (Map.Entry<String, Integer> entry : slots.entrySet()) {
                    // this.curios.put(
                    // entry.getKey(),
                    // new CurioStacksHandler(
                    // this,
                    // entry.getKey(),
                    // entry.getValue(),
                    // true,
                    // true,
                    // true,
                    // ICurio.DropRule.DEFAULT));
                    // }
                }
            }
        }

        @Override
        public int getSlots() {
            int totalSlots = 0;

            for (ICurioStacksHandler stacks : this.curios.values()) {
                totalSlots += stacks.getSlots();
            }
            return totalSlots;
        }

        @Override
        public int getVisibleSlots() {
            int totalSlots = 0;

            for (ICurioStacksHandler stacks : this.curios.values()) {

                if (stacks.isVisible()) {
                    totalSlots += stacks.getSlots();
                }
            }
            return totalSlots;
        }

        @Override
        public Optional<ICurioStacksHandler> getStacksHandler(String identifier) {
            return Optional.ofNullable(this.curios.get(identifier));
        }

        @Override
        public IItemHandlerModifiable getEquippedCurios() {
            Map<String, ICurioStacksHandler> curios = this.getCurios();
            IItemHandlerModifiable[] itemHandlers = new IItemHandlerModifiable[curios.size()];
            int index = 0;

            for (ICurioStacksHandler stacksHandler : curios.values()) {

                if (index < itemHandlers.length) {
                    itemHandlers[index] = stacksHandler.getStacks();
                    index++;
                }
            }
            return new CombinedInvWrapper(itemHandlers);
        }

        @Override
        public void setEquippedCurio(String identifier, int index, ItemStack stack) {
            Map<String, ICurioStacksHandler> curios = this.getCurios();
            ICurioStacksHandler stacksHandler = curios.get(identifier);

            if (stacksHandler != null) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (index < stackHandler.getSlots()) {
                    stackHandler.setStackInSlot(index, stack);
                }
            }
        }

        @Override
        public Optional<SlotResult> findFirstCurio(Item item) {
            return findFirstCurio(stack -> stack.getItem() == item);
        }

        @Override
        public Optional<SlotResult> findFirstCurio(Predicate<ItemStack> filter) {
            Map<String, ICurioStacksHandler> curios = this.getCurios();

            for (String id : curios.keySet()) {
                ICurioStacksHandler stacksHandler = curios.get(id);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (stack != null && filter.test(stack)) {
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        return Optional.of(
                            new SlotResult(
                                new SlotContext(
                                    id,
                                    this.wearer,
                                    i,
                                    false,
                                    renderStates.size() > i && renderStates.get(i)),
                                stack));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public List<SlotResult> findCurios(Item item) {
            return findCurios(stack -> stack.getItem() == item);
        }

        @Override
        public List<SlotResult> findCurios(Predicate<ItemStack> filter) {
            List<SlotResult> result = new ArrayList<>();
            Map<String, ICurioStacksHandler> curios = this.getCurios();

            for (String id : curios.keySet()) {
                ICurioStacksHandler stacksHandler = curios.get(id);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);

                    if (stack != null && filter.test(stack)) {
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        result.add(
                            new SlotResult(
                                new SlotContext(
                                    id,
                                    this.wearer,
                                    i,
                                    false,
                                    renderStates.size() > i && renderStates.get(i)),
                                stack));
                    }
                }
            }
            return result;
        }

        @Override
        public List<SlotResult> findCurios(String... identifiers) {
            List<SlotResult> result = new ArrayList<>();
            Set<String> ids = new HashSet<>(List.of(identifiers));
            Map<String, ICurioStacksHandler> curios = this.getCurios();

            for (String id : curios.keySet()) {

                if (ids.contains(id)) {
                    ICurioStacksHandler stacksHandler = curios.get(id);
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);

                        if (stack != null) {
                            NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                            result.add(
                                new SlotResult(
                                    new SlotContext(
                                        id,
                                        this.wearer,
                                        i,
                                        false,
                                        renderStates.size() > i && renderStates.get(i)),
                                    stack));
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public Optional<SlotResult> findCurio(String identifier, int index) {
            Map<String, ICurioStacksHandler> curios = this.getCurios();
            ICurioStacksHandler stacksHandler = curios.get(identifier);

            if (stacksHandler != null) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (index < stackHandler.getSlots()) {
                    ItemStack stack = stackHandler.getStackInSlot(index);

                    if (stack != null) {
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        return Optional.of(
                            new SlotResult(
                                new SlotContext(
                                    identifier,
                                    this.wearer,
                                    index,
                                    false,
                                    renderStates.size() > index && renderStates.get(index)),
                                stack));
                    }
                }
            }
            return Optional.empty();
        }

        @Override
        public Map<String, ICurioStacksHandler> getCurios() {
            return Collections.unmodifiableMap(this.curios);
        }

        @Override
        public void setCurios(Map<String, ICurioStacksHandler> curios) {
            this.curios = curios;
        }

        @Override
        public EntityLivingBase getWearer() {
            return this.wearer;
        }

        @Override
        public void loseInvalidStack(ItemStack stack) {
            this.invalidStacks.add(stack);
        }

        @Override
        public void handleInvalidStacks() {

            if (this.wearer != null && !this.invalidStacks.isEmpty()) {

                if (this.wearer instanceof EntityPlayer player) {
                    this.invalidStacks.forEach(drop -> ItemHandlerHelpers.giveItemToPlayer(player, drop));
                } else {
                    this.invalidStacks.forEach(drop -> {
                        EntityItem ent = this.wearer.entityDropItem(drop, 1.0F);
                        Random rand = this.wearer.getRNG();

                        if (ent != null) {
                            ent.motionX += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                            ent.motionY += rand.nextFloat() * 0.05F;
                            ent.motionZ += (rand.nextFloat() - rand.nextFloat()) * 0.1F;
                        }
                    });
                }
                this.invalidStacks = NonNullList.create();
            }
        }

        @Override
        public NBTTagList saveInventory(boolean clear) {
            NBTTagList taglist = new NBTTagList();

            for (Map.Entry<String, ICurioStacksHandler> entry : curios.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                ICurioStacksHandler stacksHandler = entry.getValue();
                IDynamicStackHandler stacks = stacksHandler.getStacks();
                IDynamicStackHandler cosmetics = stacksHandler.getCosmeticStacks();
                tag.setTag("Stacks", stacks.serializeNBT());
                tag.setTag("Cosmetics", cosmetics.serializeNBT());
                tag.setString("Identifier", entry.getKey());
                taglist.appendTag(tag);

                if (clear) {

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        stacks.setStackInSlot(i, null);
                    }

                    for (int i = 0; i < cosmetics.getSlots(); i++) {
                        cosmetics.setStackInSlot(i, null);
                    }
                }
            }
            return taglist;
        }

        @Override
        public void loadInventory(NBTTagList data) {
            if (data != null) {
                for (int i = 0; i < data.tagCount(); i++) {
                    NBTTagCompound tag = data.getCompoundTagAt(i);
                    String identifier = tag.getString("Identifier");
                    ICurioStacksHandler stacksHandler = curios.get(identifier);

                    if (stacksHandler != null) {
                        NBTTagCompound stacksData = tag.getCompoundTag("Stacks");
                        IDynamicStackHandler stacks = stacksHandler.getStacks();

                        if (stacksData != null && !stacksData.hasNoTags()) {
                            ItemStackHandler loaded = new ItemStackHandler(stacks.getSlots());
                            loaded.deserializeNBT(stacksData);
                            loadStacks(stacksHandler, loaded, stacks);
                        }

                        NBTTagCompound cosmeticData = tag.getCompoundTag("Cosmetics");
                        if (cosmeticData != null && !cosmeticData.hasNoTags()) {
                            ItemStackHandler loaded = new ItemStackHandler(
                                stacksHandler.getCosmeticStacks()
                                    .getSlots());
                            loaded.deserializeNBT(cosmeticData);
                            stacks = stacksHandler.getCosmeticStacks();
                            loadStacks(stacksHandler, loaded, stacks);
                        }
                    }
                }
            }
        }

        @Override
        public Set<ICurioStacksHandler> getUpdatingInventories() {
            return this.updates;
        }

        @Override
        public void addTransientSlotModifiers(Multimap<String, AttributeModifier> modifiers) {
            for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap()
                .entrySet()) {
                String id = entry.getKey();
                ICurioStacksHandler stacksHandler = this.curios.get(id);
                if (stacksHandler != null) {
                    for (AttributeModifier attributeModifier : entry.getValue()) {
                        stacksHandler.addTransientModifier(attributeModifier);
                    }
                }
            }
        }

        private void loadStacks(ICurioStacksHandler stacksHandler, ItemStackHandler loaded,
            IDynamicStackHandler stacks) {
            for (int j = 0; j < stacksHandler.getSlots() && j < loaded.getSlots(); j++) {
                ItemStack stack = stacks.getStackInSlot(j);
                ItemStack loadedStack = loaded.getStackInSlot(j);

                if (stack == null) {
                    stacks.setStackInSlot(j, loadedStack);
                } else {
                    this.loseInvalidStack(stack);
                }
            }
        }

        @Override
        public void addPermanentSlotModifiers(Multimap<String, AttributeModifier> modifiers) {
            for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap()
                .entrySet()) {
                String id = entry.getKey();
                ICurioStacksHandler stacksHandler = this.curios.get(id);
                if (stacksHandler != null) {
                    for (AttributeModifier attributeModifier : entry.getValue()) {
                        stacksHandler.addPermanentModifier(attributeModifier);
                    }
                }
            }
        }

        @Override
        public void removeSlotModifiers(Multimap<String, AttributeModifier> modifiers) {
            for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap()
                .entrySet()) {
                String id = entry.getKey();
                ICurioStacksHandler stacksHandler = this.curios.get(id);
                if (stacksHandler != null) {
                    for (AttributeModifier attributeModifier : entry.getValue()) {
                        stacksHandler.removeModifier(attributeModifier.getID());
                    }
                }
            }
        }

        @Override
        public void clearSlotModifiers() {
            for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
                entry.getValue()
                    .clearModifiers();
            }
        }

        @Override
        public void clearCachedSlotModifiers() {
            Multimap<String, AttributeModifier> slots = HashMultimap.create();

            for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
                ICurioStacksHandler stacksHandler = entry.getValue();
                Set<AttributeModifier> modifiers = stacksHandler.getCachedModifiers();

                if (!modifiers.isEmpty()) {
                    IDynamicStackHandler stacks = stacksHandler.getStacks();
                    NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                    String id = entry.getKey();

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);

                        if (stack != null) {
                            SlotContext slotContext = new SlotContext(
                                id,
                                this.getWearer(),
                                i,
                                false,
                                renderStates.size() > i && renderStates.get(i));
                            UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
                            Multimap<IAttribute, AttributeModifier> map = CuriosApi
                                .getAttributeModifiers(slotContext, uuid, stack);

                            for (IAttribute attribute : map.keySet()) {
                                if (attribute instanceof SlotAttribute wrapper) {
                                    slots.putAll(wrapper.getIdentifier(), map.get(attribute));
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<String, Collection<AttributeModifier>> entry : slots.asMap()
                .entrySet()) {
                String id = entry.getKey();
                ICurioStacksHandler stacksHandler = this.curios.get(id);

                if (stacksHandler != null) {
                    for (AttributeModifier attributeModifier : entry.getValue()) {
                        stacksHandler.getCachedModifiers()
                            .remove(attributeModifier);
                    }
                    stacksHandler.clearCachedModifiers();
                }
            }

            for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
                if (!slots.asMap()
                    .containsKey(entry.getKey())) {
                    entry.getValue()
                        .clearCachedModifiers();
                }
            }
        }

        @Override
        public Multimap<String, AttributeModifier> getModifiers() {
            Multimap<String, AttributeModifier> result = HashMultimap.create();
            for (Map.Entry<String, ICurioStacksHandler> entry : this.curios.entrySet()) {
                result.putAll(
                    entry.getKey(),
                    entry.getValue()
                        .getModifiers()
                        .values());
            }
            return result;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList taglist = new NBTTagList();

            for (Map.Entry<String, ICurioStacksHandler> entry : this.getCurios()
                .entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setTag(
                    "StacksHandler",
                    entry.getValue()
                        .serializeNBT());
                tag.setString("Identifier", entry.getKey());
                taglist.appendTag(tag);
            }
            compound.setTag("Curios", taglist);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compoundNBT) {
            NBTTagList tagList = compoundNBT.getTagList("Curios", Constants.NBT.TAG_LIST);
            EntityLivingBase livingEntity = this.getWearer();

            if (tagList.tagCount() > 0) {
                Map<String, ICurioStacksHandler> curios = new LinkedHashMap<>();
                SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<>();
                SortedSet<ISlotType> sorted = new TreeSet<>(
                    CuriosApi.getEntitySlots(this.wearer.getClass())
                        .values());

                for (ISlotType slotType : sorted) {
                    sortedCurios.put(
                        slotType,
                        new CurioStacksHandler(
                            this,
                            slotType.getIdentifier(),
                            slotType.getSize(),
                            slotType.useNativeGui(),
                            slotType.hasCosmetic(),
                            slotType.canToggleRendering(),
                            slotType.getDropRule()));
                }

                for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound tag = tagList.getCompoundTagAt(i);
                    String identifier = tag.getString("Identifier");
                    CurioStacksHandler prevStacksHandler = new CurioStacksHandler(this, identifier);
                    prevStacksHandler.deserializeNBT(tag.getCompoundTag("StacksHandler"));

                    Optional<ISlotType> optionalType = Optional.ofNullable(
                        CuriosApi.getEntitySlots(this.wearer.getClass())
                            .get(identifier));
                    optionalType.ifPresent(type -> {
                        CurioStacksHandler newStacksHandler = new CurioStacksHandler(
                            this,
                            type.getIdentifier(),
                            type.getSize(),
                            type.useNativeGui(),
                            type.hasCosmetic(),
                            type.canToggleRendering(),
                            type.getDropRule());
                        newStacksHandler.copyModifiers(prevStacksHandler);
                        int index = 0;

                        while (index < newStacksHandler.getSlots() && index < prevStacksHandler.getSlots()) {
                            ItemStack prevStack = prevStacksHandler.getStacks()
                                .getStackInSlot(index);
                            NonNullList<Boolean> renderStates = newStacksHandler.getRenders();
                            SlotContext slotContext = new SlotContext(
                                identifier,
                                livingEntity,
                                index,
                                false,
                                renderStates.size() > index && renderStates.get(index));

                            if (prevStack != null) {

                                if (CuriosApi.isStackValid(slotContext, prevStack)) {
                                    newStacksHandler.getStacks()
                                        .setStackInSlot(index, prevStack);
                                } else {
                                    this.loseInvalidStack(prevStack);
                                }
                            }
                            ItemStack prevCosmetic = prevStacksHandler.getCosmeticStacks()
                                .getStackInSlot(index);
                            slotContext = new SlotContext(identifier, livingEntity, index, true, true);

                            if (prevCosmetic != null) {

                                if (CuriosApi.isStackValid(slotContext, prevCosmetic)) {
                                    newStacksHandler.getCosmeticStacks()
                                        .setStackInSlot(
                                            index,
                                            prevStacksHandler.getCosmeticStacks()
                                                .getStackInSlot(index));
                                } else {
                                    this.loseInvalidStack(prevCosmetic);
                                }
                            }
                            index++;
                        }

                        while (index < prevStacksHandler.getSlots()) {
                            this.loseInvalidStack(
                                prevStacksHandler.getStacks()
                                    .getStackInSlot(index));
                            this.loseInvalidStack(
                                prevStacksHandler.getCosmeticStacks()
                                    .getStackInSlot(index));
                            index++;
                        }
                        sortedCurios.put(type, newStacksHandler);

                        for (int j = 0; j < newStacksHandler.getRenders()
                            .size() && j
                                < prevStacksHandler.getRenders()
                                    .size(); j++) {
                            newStacksHandler.getRenders()
                                .set(
                                    j,
                                    prevStacksHandler.getRenders()
                                        .get(j));
                        }
                    });

                    if (optionalType.isEmpty()) {
                        IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
                        IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

                        for (int j = 0; j < stackHandler.getSlots(); j++) {
                            ItemStack stack = stackHandler.getStackInSlot(j);

                            if (stack != null) {
                                this.loseInvalidStack(stack);
                            }

                            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);

                            if (cosmeticStack != null) {
                                this.loseInvalidStack(cosmeticStack);
                            }
                        }
                    }
                }
                sortedCurios.forEach((slotType, stacksHandler) -> curios.put(slotType.getIdentifier(), stacksHandler));
                this.setCurios(curios);

            }
        }
    }

    public static class Provider implements ICapabilitySerializable {

        final LazyOptional<ICuriosItemHandler> optional;
        final ICuriosItemHandler handler;
        final EntityLivingBase wearer;

        Provider(final EntityPlayer wearer) {
            this.wearer = wearer;
            this.handler = new CurioInventoryWrapper(this.wearer);
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, ForgeDirection facing) {

            if (!this.wearer.worldObj.isRemote && CuriosApi.getEntitySlots(this.wearer.getClass())
                .isEmpty()) {
                return LazyOptional.empty();
            }
            // TODO: Add Data Loader
            // if (this.wearer.worldObj.isRemote &&
            // !CuriosEntityManager.INSTANCE.hasSlots(this.wearer.getClass())) {
            // return LazyOptional.empty();
            // }
            return CuriosCapability.INVENTORY.orEmpty(capability, this.optional);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbtTagCompound) {
            this.handler.deserializeNBT(nbtTagCompound);
        }
    }
}
