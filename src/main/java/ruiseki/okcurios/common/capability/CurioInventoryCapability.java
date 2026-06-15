package ruiseki.okcurios.common.capability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityManager;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.capabilities.ICapabilitySerializable;
import ruiseki.okcore.init.IInitListener;
import ruiseki.okcore.item.ItemStackHandler;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.helper.ICuriosHelper;
import ruiseki.okcurios.api.type.helper.ISlotHelper;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.CuriosHelper;
import ruiseki.okcurios.common.inventory.CurioStacksHandler;

public class CurioInventoryCapability implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step != Step.PREINIT) return;
        CapabilityManager.INSTANCE.register(ICuriosItemHandler.class, new Capability.IStorage<ICuriosItemHandler>() {

            @Override
            public NBTBase writeNBT(Capability<ICuriosItemHandler> capability, ICuriosItemHandler instance,
                ForgeDirection side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<ICuriosItemHandler> capability, ICuriosItemHandler instance,
                ForgeDirection side, NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }, CurioInventoryWrapper::new);
    }

    public static ICapabilityProvider createProvider(final EntityPlayer playerEntity) {
        return new Provider(playerEntity);
    }

    public static class CurioInventoryWrapper implements ICuriosItemHandler {

        Map<String, ICurioStacksHandler> curios = new LinkedHashMap<String, ICurioStacksHandler>();
        Set<String> locked = new HashSet<String>();
        List<ItemStack> invalidStacks = new ArrayList<ItemStack>();
        EntityPlayer wearer;
        Set<String> toLock = new HashSet<String>();
        List<UnlockState> toUnlock = new ArrayList<UnlockState>();
        int fortuneBonus = 0;
        int lootingBonus = 0;
        Set<ICurioStacksHandler> updates = new HashSet<ICurioStacksHandler>();

        CurioInventoryWrapper() {
            this(null);
        }

        CurioInventoryWrapper(final EntityPlayer playerEntity) {
            this.wearer = playerEntity;
            this.reset();
        }

        @Override
        public void reset() {
            ISlotHelper slotHelper = CuriosApi.getSlotHelper();

            if (slotHelper != null && this.wearer != null && !this.wearer.worldObj.isRemote) {
                this.locked.clear();
                this.curios.clear();
                this.invalidStacks.clear();
                SortedSet<ISlotType> sorted = new TreeSet<ISlotType>(slotHelper.getSlotTypes(this.wearer));

                for (ISlotType slotType : sorted) {
                    this.curios.put(
                        slotType.getIdentifier(),
                        new CurioStacksHandler(
                            this,
                            slotType.getIdentifier(),
                            slotType.getSize(),
                            0,
                            slotType.isVisible(),
                            slotType.hasCosmetic()));
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
        public Set<String> getLockedSlots() {
            return Collections.unmodifiableSet(this.locked);
        }

        @Override
        public ICurioStacksHandler getStacksHandler(String identifier) {
            return this.curios.get(identifier);
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
        public void unlockSlotType(String identifier, int amount, boolean visible, boolean cosmetic) {
            this.toUnlock.add(new UnlockState(identifier, amount, visible, cosmetic));
        }

        @Override
        public void lockSlotType(String identifier) {
            this.toLock.add(identifier);
        }

        @Override
        public void processSlots() {
            for (String id : this.toLock) {
                ICurioStacksHandler stackHandler = this.getStacksHandler(id);
                if (stackHandler != null) {
                    this.curios.remove(id);
                    this.locked.add(id);
                    this.loseStacks(stackHandler.getStacks(), id, stackHandler.getSlots());
                }
            }
            for (UnlockState state : this.toUnlock) {
                if (!this.curios.containsKey(state.identifier)) {
                    this.curios.put(
                        state.identifier,
                        new CurioStacksHandler(this, state.identifier, state.amount, 0, state.visible, state.cosmetic));
                }
                this.locked.remove(state.identifier);
            }
            this.toLock.clear();
            this.toUnlock.clear();
        }

        @Override
        public EntityLivingBase getWearer() {
            return this.wearer;
        }

        @Override
        public void loseInvalidStack(ItemStack stack) {
            if (stack != null) {
                this.invalidStacks.add(stack);
            }
        }

        @Override
        public void handleInvalidStacks() {
            if (this.wearer != null && !this.invalidStacks.isEmpty()) {
                for (ItemStack drop : this.invalidStacks) {
                    if (!this.wearer.inventory.addItemStackToInventory(drop)) {
                        this.wearer.dropPlayerItemWithRandomChoice(drop, false);
                    }
                }
                this.invalidStacks.clear();
            }
        }

        private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {
            if (this.wearer != null && !this.wearer.worldObj.isRemote) {
                List<ItemStack> drops = new ArrayList<ItemStack>();

                for (int i = Math.max(0, stackHandler.getSlots() - amount); i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    drops.add(stack);
                    SlotContext slotContext = new SlotContext(identifier, this.wearer, i);

                    if (stack != null) {
                        UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
                        Multimap<IAttribute, AttributeModifier> modifiers = CuriosApi.getCuriosHelper()
                            .getAttributeModifiers(slotContext, uuid, stack);

                        if (this.wearer.getAttributeMap() != null && modifiers != null && !modifiers.isEmpty()) {
                            Multimap<String, AttributeModifier> convertedModifiers = HashMultimap.create();
                            for (Map.Entry<IAttribute, AttributeModifier> entry : modifiers.entries()) {
                                IAttribute attribute = entry.getKey();
                                AttributeModifier modifier = entry.getValue();

                                if (attribute != null && modifier != null) {
                                    String attributeName = attribute.getAttributeUnlocalizedName();
                                    convertedModifiers.put(attributeName, modifier);
                                }
                            }

                            this.wearer.getAttributeMap()
                                .removeAttributeModifiers(convertedModifiers);
                        }
                        ICurio curio = CuriosApi.getCuriosHelper()
                            .getCurio(stack);
                        if (curio != null) {
                            curio.onUnequip(slotContext, null);
                        }
                    }
                    stackHandler.setStackInSlot(i, null);
                }

                for (ItemStack drop : drops) {
                    if (drop != null) {
                        if (!this.wearer.inventory.addItemStackToInventory(drop)) {
                            this.wearer.dropPlayerItemWithRandomChoice(drop, false);
                        }
                    }
                }
            }
        }

        public static class UnlockState {

            final String identifier;
            final int amount;
            final boolean visible;
            final boolean cosmetic;

            UnlockState(String identifier, int amount, boolean visible, boolean cosmetic) {
                this.identifier = identifier;
                this.amount = amount;
                this.visible = visible;
                this.cosmetic = cosmetic;
            }
        }

        @Override
        public int getFortuneBonus() {
            return this.fortuneBonus;
        }

        @Override
        public int getLootingBonus() {
            return this.lootingBonus;
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
        public void setEnchantmentBonuses(int fortune, int looting) {
            this.fortuneBonus = fortune;
            this.lootingBonus = looting;
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
                    String id = entry.getKey();

                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);

                        if (stack != null) {
                            SlotContext slotContext = new SlotContext(id, this.getWearer(), i);
                            UUID uuid = UUID.nameUUIDFromBytes((id + i).getBytes());
                            Multimap<IAttribute, AttributeModifier> map = CuriosApi.getCuriosHelper()
                                .getAttributeModifiers(slotContext, uuid, stack);

                            for (IAttribute attribute : map.keySet()) {
                                if (attribute instanceof CuriosHelper.SlotAttributeWrapper) {
                                    slots.putAll(
                                        ((CuriosHelper.SlotAttributeWrapper) attribute).identifier,
                                        map.get(attribute));
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

            NBTTagList lockedList = new NBTTagList();
            for (String identifier : this.getLockedSlots()) {
                lockedList.appendTag(new NBTTagString(identifier));
            }
            compound.setTag("Locked", lockedList);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compoundNBT) {
            NBTTagList tagList = compoundNBT.getTagList("Curios", 10); // 10 đại diện cho COMPOUND trong 1.7.10
            NBTTagList lockedList = compoundNBT.getTagList("Locked", 8); // 8 đại diện cho STRING trong 1.7.10
            ISlotHelper slotHelper = CuriosApi.getSlotHelper();
            ICuriosHelper curiosHelper = CuriosApi.getCuriosHelper();
            EntityLivingBase livingEntity = this.getWearer();

            if (tagList.tagCount() > 0 && slotHelper != null) {
                Map<String, ICurioStacksHandler> curiosMap = new LinkedHashMap<String, ICurioStacksHandler>();
                SortedMap<ISlotType, ICurioStacksHandler> sortedCurios = new TreeMap<ISlotType, ICurioStacksHandler>();
                SortedSet<ISlotType> sorted = new TreeSet<ISlotType>(
                    CuriosApi.getSlotHelper()
                        .getSlotTypes(this.wearer));

                for (ISlotType slotType : sorted) {
                    sortedCurios.put(
                        slotType,
                        new CurioStacksHandler(
                            this,
                            slotType.getIdentifier(),
                            slotType.getSize(),
                            0,
                            slotType.isVisible(),
                            slotType.hasCosmetic()));
                }

                for (int i = 0; i < tagList.tagCount(); i++) {
                    NBTTagCompound tag = tagList.getCompoundTagAt(i);
                    String identifier = tag.getString("Identifier");
                    CurioStacksHandler prevStacksHandler = new CurioStacksHandler(this, identifier);
                    prevStacksHandler.deserializeNBT(tag.getCompoundTag("StacksHandler"));

                    ISlotType slotType = CuriosApi.getSlotHelper()
                        .getSlotType(identifier);
                    if (slotType != null) {
                        CurioStacksHandler newStacksHandler = new CurioStacksHandler(
                            this,
                            slotType.getIdentifier(),
                            slotType.getSize(),
                            prevStacksHandler.getSizeShift(),
                            slotType.isVisible(),
                            slotType.hasCosmetic());
                        newStacksHandler.copyModifiers(prevStacksHandler);
                        int index = 0;

                        while (index < newStacksHandler.getSlots() && index < prevStacksHandler.getSlots()) {
                            ItemStack prevStack = prevStacksHandler.getStacks()
                                .getStackInSlot(index);
                            SlotContext slotContext = new SlotContext(identifier, livingEntity, index);

                            if (prevStack != null) {
                                if (curiosHelper.isStackValid(slotContext, prevStack)) {
                                    newStacksHandler.getStacks()
                                        .setStackInSlot(index, prevStack);
                                } else {
                                    this.loseInvalidStack(prevStack);
                                }
                            }

                            ItemStack prevCosmetic = prevStacksHandler.getCosmeticStacks()
                                .getStackInSlot(index);
                            if (prevCosmetic != null) {
                                if (curiosHelper.isStackValid(slotContext, prevCosmetic)) {
                                    newStacksHandler.getCosmeticStacks()
                                        .setStackInSlot(index, prevCosmetic);
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
                        sortedCurios.put(slotType, newStacksHandler);

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
                    } else {
                        // Nếu slot cũ không còn tồn tại, giải phóng toàn bộ Item chứa trong đó
                        IDynamicStackHandler stackHandler = prevStacksHandler.getStacks();
                        IDynamicStackHandler cosmeticStackHandler = prevStacksHandler.getCosmeticStacks();

                        for (int j = 0; j < stackHandler.getSlots(); j++) {
                            ItemStack stack = stackHandler.getStackInSlot(j);
                            if (stack != null) this.loseInvalidStack(stack);

                            ItemStack cosmeticStack = cosmeticStackHandler.getStackInSlot(j);
                            if (cosmeticStack != null) this.loseInvalidStack(cosmeticStack);
                        }
                    }
                }

                for (Map.Entry<ISlotType, ICurioStacksHandler> entry : sortedCurios.entrySet()) {
                    curiosMap.put(
                        entry.getKey()
                            .getIdentifier(),
                        entry.getValue());
                }
                this.setCurios(curiosMap);

                for (int k = 0; k < lockedList.tagCount(); k++) {
                    this.lockSlotType(lockedList.getStringTagAt(k));
                }
            }
        }
    }

    public static class Provider implements ICapabilitySerializable {

        final ICuriosItemHandler handler;

        Provider(final EntityPlayer playerEntity) {
            this.handler = new CurioInventoryWrapper(playerEntity);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbtTagCompound) {
            this.handler.deserializeNBT(nbtTagCompound);
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, ForgeDirection facing) {
            return capability == CuriosCapability.INVENTORY;
        }

        @Override
        public @Nullable <T> T getCapability(@NotNull Capability<T> capability, ForgeDirection facing) {
            return hasCapability(capability, facing) ? (T) this.handler : null;
        }
    }
}
