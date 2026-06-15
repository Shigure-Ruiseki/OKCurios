package ruiseki.okcurios.common.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.SlotModifiersUpdatedEvent;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.CuriosHelper;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class CurioStacksHandler implements ICurioStacksHandler {

    private static final UUID LEGACY_UUID = UUID.fromString("0b0eabbd-4220-4e9f-bafb-34100da2bd7e");

    private final ICuriosItemHandler itemHandler;
    private final String identifier;
    private final Map<UUID, AttributeModifier> modifiers = new HashMap<UUID, AttributeModifier>();
    private final Set<AttributeModifier> persistentModifiers = new HashSet<AttributeModifier>();
    private final Set<AttributeModifier> cachedModifiers = new HashSet<AttributeModifier>();
    private final Multimap<Integer, AttributeModifier> modifiersByOperation = HashMultimap.create();

    private int baseSize;
    private IDynamicStackHandler stackHandler;
    private IDynamicStackHandler cosmeticStackHandler;
    private boolean visible;
    private boolean cosmetic;
    private List<Boolean> renderHandler;
    private boolean update;

    public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier) {
        this(itemHandler, identifier, 1, 0, true, false);
    }

    public CurioStacksHandler(ICuriosItemHandler itemHandler, String identifier, int size, int shift, boolean visible,
        boolean cosmetic) {
        this.baseSize = size;
        this.visible = visible;
        this.cosmetic = cosmetic;
        this.itemHandler = itemHandler;
        this.identifier = identifier;

        this.stackHandler = new DynamicStackHandler(size);
        this.cosmeticStackHandler = new DynamicStackHandler(size);

        this.renderHandler = new ArrayList<Boolean>(size);
        for (int i = 0; i < size; i++) {
            this.renderHandler.add(true);
        }
    }

    @Override
    public IDynamicStackHandler getStacks() {
        this.update();
        return this.stackHandler;
    }

    @Override
    public IDynamicStackHandler getCosmeticStacks() {
        this.update();
        return this.cosmeticStackHandler;
    }

    @Override
    public List<Boolean> getRenders() {
        this.update();
        return this.renderHandler;
    }

    @Override
    public int getSlots() {
        this.update();
        return this.stackHandler.getSlots();
    }

    public int getSizeShift() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public boolean hasCosmetic() {
        return this.cosmetic;
    }

    private void addLegacyChange(int shift) {
        AttributeModifier mod = this.getModifiers()
            .get(LEGACY_UUID);
        int current = mod != null ? (int) mod.getAmount() : 0;
        current += shift;

        AttributeModifier newModifier = new AttributeModifier(LEGACY_UUID, "legacy", current, 0);
        this.modifiers.put(newModifier.getID(), newModifier);

        Collection<AttributeModifier> modifiersCollection = this.getModifiersByOperation(newModifier.getOperation());
        modifiersCollection.remove(newModifier);
        modifiersCollection.add(newModifier);
        this.persistentModifiers.remove(newModifier);
        this.persistentModifiers.add(newModifier);
        this.flagUpdate();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compoundNBT = new NBTTagCompound();
        compoundNBT.setInteger("SavedBaseSize", this.baseSize);
        compoundNBT.setTag("Stacks", this.stackHandler.serializeNBT());
        compoundNBT.setTag("Cosmetics", this.cosmeticStackHandler.serializeNBT());

        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < this.renderHandler.size(); i++) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Slot", i);
            tag.setBoolean("Render", this.renderHandler.get(i));
            nbtTagList.appendTag(tag);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Renders", nbtTagList);
        nbt.setInteger("Size", this.renderHandler.size());
        compoundNBT.setTag("Renders", nbt);
        compoundNBT.setBoolean("HasCosmetic", this.cosmetic);
        compoundNBT.setBoolean("Visible", this.visible);

        if (!this.persistentModifiers.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (AttributeModifier attributeModifier : this.persistentModifiers) {
                list.appendTag(CuriosHelper.writeAttributeModifier(attributeModifier));
            }
            compoundNBT.setTag("PersistentModifiers", list);
        }

        if (!this.modifiers.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<UUID, AttributeModifier> entry : this.modifiers.entrySet()) {
                AttributeModifier modifier = entry.getValue();
                if (!this.persistentModifiers.contains(modifier)) {
                    list.appendTag(CuriosHelper.writeAttributeModifier(modifier));
                }
            }
            compoundNBT.setTag("CachedModifiers", list);
        }
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("SavedBaseSize")) {
            this.baseSize = nbt.getInteger("SavedBaseSize");
        }

        if (nbt.hasKey("Stacks")) {
            this.stackHandler.deserializeNBT(nbt.getCompoundTag("Stacks"));
        }

        if (nbt.hasKey("Cosmetics")) {
            this.cosmeticStackHandler.deserializeNBT(nbt.getCompoundTag("Cosmetics"));
        }

        if (nbt.hasKey("Renders")) {
            NBTTagCompound tag = nbt.getCompoundTag("Renders");
            int size = tag.hasKey("Size", 99) ? tag.getInteger("Size") : this.stackHandler.getSlots();

            this.renderHandler = new ArrayList<Boolean>(size);
            for (int i = 0; i < size; i++) {
                this.renderHandler.add(true);
            }

            NBTTagList tagList = tag.getTagList("Renders", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tags = tagList.getCompoundTagAt(i);
                int slot = tags.getInteger("Slot");

                if (slot >= 0 && slot < this.renderHandler.size()) {
                    this.renderHandler.set(slot, tags.getBoolean("Render"));
                }
            }
        }

        if (nbt.hasKey("SizeShift")) {
            int sizeShift = nbt.getInteger("SizeShift");
            if (sizeShift != 0) {
                this.addLegacyChange(sizeShift);
            }
        }
        this.cosmetic = nbt.hasKey("HasCosmetic") ? nbt.getBoolean("HasCosmetic") : this.cosmetic;
        this.visible = nbt.hasKey("Visible") ? nbt.getBoolean("Visible") : this.visible;

        if (nbt.hasKey("PersistentModifiers", 9)) {
            NBTTagList list = nbt.getTagList("PersistentModifiers", 10);
            for (int i = 0; i < list.tagCount(); ++i) {
                AttributeModifier attributeModifier = CuriosHelper.readAttributeModifier(list.getCompoundTagAt(i));
                if (attributeModifier != null) {
                    this.addPermanentModifier(attributeModifier);
                }
            }
        }

        if (nbt.hasKey("CachedModifiers", 9)) {
            NBTTagList list = nbt.getTagList("CachedModifiers", 10);
            for (int i = 0; i < list.tagCount(); ++i) {
                AttributeModifier attributeModifier = CuriosHelper.readAttributeModifier(list.getCompoundTagAt(i));
                if (attributeModifier != null) {
                    this.cachedModifiers.add(attributeModifier);
                    this.addTransientModifier(attributeModifier);
                }
            }
        }
        this.update();
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public NBTTagCompound getSyncTag() {
        NBTTagCompound compoundNBT = new NBTTagCompound();
        compoundNBT.setTag("Stacks", this.stackHandler.serializeNBT());
        compoundNBT.setTag("Cosmetics", this.cosmeticStackHandler.serializeNBT());

        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < this.renderHandler.size(); i++) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("Slot", i);
            tag.setBoolean("Render", this.renderHandler.get(i));
            nbtTagList.appendTag(tag);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Renders", nbtTagList);
        nbt.setInteger("Size", this.renderHandler.size());
        compoundNBT.setTag("Renders", nbt);
        compoundNBT.setBoolean("HasCosmetic", this.cosmetic);
        compoundNBT.setBoolean("Visible", this.visible);
        compoundNBT.setInteger("BaseSize", this.baseSize);

        if (!this.modifiers.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<UUID, AttributeModifier> modifier : this.modifiers.entrySet()) {
                list.appendTag(CuriosHelper.writeAttributeModifier(modifier.getValue()));
            }
            compoundNBT.setTag("Modifiers", list);
        }
        return compoundNBT;
    }

    @Override
    public void applySyncTag(NBTTagCompound tag) {
        if (tag.hasKey("BaseSize")) {
            this.baseSize = tag.getInteger("BaseSize");
        }

        if (tag.hasKey("Stacks")) {
            this.stackHandler.deserializeNBT(tag.getCompoundTag("Stacks"));
        }

        if (tag.hasKey("Cosmetics")) {
            this.cosmeticStackHandler.deserializeNBT(tag.getCompoundTag("Cosmetics"));
        }

        if (tag.hasKey("Renders")) {
            NBTTagCompound compoundNBT = tag.getCompoundTag("Renders");
            int size = compoundNBT.hasKey("Size", 99) ? compoundNBT.getInteger("Size") : this.stackHandler.getSlots();

            this.renderHandler = new ArrayList<Boolean>(size);
            for (int i = 0; i < size; i++) {
                this.renderHandler.add(true);
            }

            NBTTagList tagList = compoundNBT.getTagList("Renders", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tags = tagList.getCompoundTagAt(i);
                int slot = tags.getInteger("Slot");

                if (slot >= 0 && slot < this.renderHandler.size()) {
                    this.renderHandler.set(slot, tags.getBoolean("Render"));
                }
            }
        }

        if (tag.hasKey("SizeShift")) {
            int sizeShift = tag.getInteger("SizeShift");
            if (sizeShift != 0) {
                this.addLegacyChange(sizeShift);
            }
        }
        this.cosmetic = tag.hasKey("HasCosmetic") ? tag.getBoolean("HasCosmetic") : this.cosmetic;
        this.visible = tag.hasKey("Visible") ? tag.getBoolean("Visible") : this.visible;
        this.modifiers.clear();
        this.persistentModifiers.clear();
        this.modifiersByOperation.clear();

        if (tag.hasKey("Modifiers", 9)) {
            NBTTagList list = tag.getTagList("Modifiers", 10);
            for (int i = 0; i < list.tagCount(); ++i) {
                AttributeModifier attributeModifier = CuriosHelper.readAttributeModifier(list.getCompoundTagAt(i));
                if (attributeModifier != null) {
                    this.addTransientModifier(attributeModifier);
                }
            }
        }
        this.flagUpdate();
        this.update();
    }

    @Override
    public void copyModifiers(ICurioStacksHandler other) {
        this.modifiers.clear();
        this.cachedModifiers.clear();
        this.modifiersByOperation.clear();
        this.persistentModifiers.clear();

        for (Map.Entry<UUID, AttributeModifier> entry : other.getModifiers()
            .entrySet()) {
            this.addTransientModifier(entry.getValue());
        }
        this.cachedModifiers.addAll(other.getCachedModifiers());

        for (AttributeModifier persistentModifier : other.getPermanentModifiers()) {
            this.addPermanentModifier(persistentModifier);
        }
        this.update();
    }

    @Override
    public Map<UUID, AttributeModifier> getModifiers() {
        return this.modifiers;
    }

    @Override
    public Set<AttributeModifier> getPermanentModifiers() {
        return this.persistentModifiers;
    }

    @Override
    public Set<AttributeModifier> getCachedModifiers() {
        return this.cachedModifiers;
    }

    @Override
    public Collection<AttributeModifier> getModifiersByOperation(int operation) {
        return this.modifiersByOperation.get(operation);
    }

    @Override
    public void addTransientModifier(AttributeModifier modifier) {
        this.modifiers.put(modifier.getID(), modifier);
        this.getModifiersByOperation(modifier.getOperation())
            .add(modifier);
        this.flagUpdate();
    }

    @Override
    public void addPermanentModifier(AttributeModifier modifier) {
        this.addTransientModifier(modifier);
        this.persistentModifiers.add(modifier);
    }

    @Override
    public void removeModifier(UUID uuid) {
        AttributeModifier modifier = this.modifiers.remove(uuid);
        if (modifier != null) {
            this.persistentModifiers.remove(modifier);
            this.getModifiersByOperation(modifier.getOperation())
                .remove(modifier);
            this.flagUpdate();
        }
    }

    private void flagUpdate() {
        this.update = true;
        if (this.itemHandler != null) {
            this.itemHandler.getUpdatingInventories()
                .remove(this);
            this.itemHandler.getUpdatingInventories()
                .add(this);
        }
    }

    @Override
    public void clearModifiers() {
        Set<UUID> ids = new HashSet<UUID>(this.modifiers.keySet());
        for (UUID id : ids) {
            this.removeModifier(id);
        }
    }

    @Override
    public void clearCachedModifiers() {
        for (AttributeModifier cachedModifier : this.cachedModifiers) {
            this.removeModifier(cachedModifier.getID());
        }
        this.cachedModifiers.clear();
        this.flagUpdate();
    }

    @Override
    public void update() {
        if (this.update) {
            this.update = false;
            double calculatedBaseSize = this.baseSize;

            for (AttributeModifier mod : this.getModifiersByOperation(0)) {
                calculatedBaseSize += mod.getAmount();
            }
            double size = calculatedBaseSize;

            for (AttributeModifier mod : this.getModifiersByOperation(1)) {
                size += this.baseSize * mod.getAmount();
            }

            for (AttributeModifier mod : this.getModifiersByOperation(2)) {
                size *= mod.getAmount();
            }

            if ((int) size != this.getSlots()) {
                this.resize((int) size);

                if (this.itemHandler != null && this.itemHandler.getWearer() != null) {
                    MinecraftForge.EVENT_BUS.post(
                        new SlotModifiersUpdatedEvent(this.itemHandler.getWearer(), Sets.newHashSet(this.identifier)));

                    if (this.itemHandler.getWearer() instanceof EntityPlayer player) {
                        if (player.openContainer instanceof CuriosContainer) {
                            ((CuriosContainer) player.openContainer).resetSlots();
                        }
                    }
                }
            }
        }
    }

    private void resize(int newSize) {
        int currentSize = this.getSlots();
        if (currentSize != newSize) {
            int change = newSize - currentSize;

            if (currentSize > newSize) {
                change = change * -1;
                this.loseStacks(this.stackHandler, identifier, change);
                this.stackHandler.shrink(change);
                this.cosmeticStackHandler.shrink(change);

                List<Boolean> newList = new ArrayList<Boolean>(newSize);
                for (int i = 0; i < newSize; i++) {
                    newList.add(i < this.renderHandler.size() ? this.renderHandler.get(i) : true);
                }
                this.renderHandler = newList;
            } else {
                this.stackHandler.grow(change);
                this.cosmeticStackHandler.grow(change);

                List<Boolean> newList = new ArrayList<Boolean>(newSize);
                for (int i = 0; i < newSize; i++) {
                    newList.add(i < this.renderHandler.size() ? this.renderHandler.get(i) : true);
                }
                this.renderHandler = newList;
            }
        }
    }

    private void loseStacks(IDynamicStackHandler stackHandler, String identifier, int amount) {
        if (this.itemHandler == null) {
            return;
        }
        List<ItemStack> drops = new ArrayList<ItemStack>();

        for (int i = Math.max(0, stackHandler.getSlots() - amount); i < stackHandler.getSlots(); i++) {
            ItemStack stack = stackHandler.getStackInSlot(i);
            drops.add(stack);
            EntityLivingBase entity = this.itemHandler.getWearer();
            SlotContext slotContext = new SlotContext(identifier, entity, i);

            if (stack != null) {
                UUID uuid = UUID.nameUUIDFromBytes((identifier + i).getBytes());
                Multimap<IAttribute, AttributeModifier> map = CuriosApi.getCuriosHelper()
                    .getAttributeModifiers(slotContext, uuid, stack);
                Multimap<String, AttributeModifier> slots = HashMultimap.create();
                Set<CuriosHelper.SlotAttributeWrapper> toRemove = new HashSet<CuriosHelper.SlotAttributeWrapper>();

                for (IAttribute attribute : map.keySet()) {
                    if (attribute instanceof CuriosHelper.SlotAttributeWrapper wrapper) {
                        slots.putAll(wrapper.identifier, map.get(attribute));
                        toRemove.add(wrapper);
                    }
                }

                for (IAttribute attribute : toRemove) {
                    map.removeAll(attribute);
                }

                if (this.itemHandler.getWearer() != null && this.itemHandler.getWearer()
                    .getAttributeMap() != null) {
                    Multimap<String, AttributeModifier> convertedModifiers = HashMultimap.create();
                    for (Map.Entry<IAttribute, AttributeModifier> entry1 : map.entries()) {
                        IAttribute attribute = entry1.getKey();
                        AttributeModifier modifier = entry1.getValue();

                        if (attribute != null && modifier != null) {
                            String attributeName = attribute.getAttributeUnlocalizedName();
                            convertedModifiers.put(attributeName, modifier);
                        }
                    }
                    this.itemHandler.getWearer()
                        .getAttributeMap()
                        .removeAttributeModifiers(convertedModifiers);
                }
                this.itemHandler.removeSlotModifiers(slots);

                ICurio curio = CuriosApi.getCuriosHelper()
                    .getCurio(stack);
                if (curio != null) {
                    curio.onUnequip(slotContext, null);
                }
            }
            stackHandler.setStackInSlot(i, null);
        }

        for (ItemStack drop : drops) {
            this.itemHandler.loseInvalidStack(drop);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurioStacksHandler that = (CurioStacksHandler) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }
}
