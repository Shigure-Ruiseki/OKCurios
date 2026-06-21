package ruiseki.okcurios.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttribute;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcore.helper.EntityHelpers;
import ruiseki.okcore.helper.ItemStackHelpers;
import ruiseki.okcore.item.CombinedInvWrapper;
import ruiseki.okcore.item.IItemHandlerModifiable;
import ruiseki.okcurios.Reference;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.SlotResult;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.helper.ICuriosHelper;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class CuriosHelper implements ICuriosHelper {

    private static final Map<String, SlotAttributeWrapper> SLOT_ATTRIBUTES = new HashMap<String, SlotAttributeWrapper>();

    public interface CurioBrokenConsumer {

        void accept(String id, int index, EntityLivingBase wearer);
    }

    private static CurioBrokenConsumer brokenCurioConsumer;

    @Override
    public ICurio getCurio(ItemStack stack) {
        return ItemStackHelpers.getCapability(stack, CuriosCapability.ITEM, ForgeDirection.SOUTH);
    }

    @Override
    public ICuriosItemHandler getCuriosHandler(final EntityLivingBase livingEntity) {
        return EntityHelpers.getCapability(livingEntity, CuriosCapability.INVENTORY, ForgeDirection.UNKNOWN);
    }

    @Override
    public Set<String> getCurioTags(Item item) {
        Set<String> tags = new HashSet<String>();
        if (item == null) return tags;

        int[] oreIds = OreDictionary.getOreIDs(new ItemStack(item));
        for (int id : oreIds) {
            String oreName = OreDictionary.getOreName(id);
            if (oreName.startsWith(Reference.PREFIX_MOD)) {
                tags.add(oreName.substring(Reference.PREFIX_MOD.length()));
            }
        }
        return tags;
    }

    @Override
    public IItemHandlerModifiable getEquippedCurios(EntityLivingBase livingEntity) {
        ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        if (handler == null) return null;
        Map<String, ICurioStacksHandler> curios = handler.getCurios();
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
    public void setEquippedCurio(EntityLivingBase livingEntity, String identifier, int index, ItemStack stack) {
        ICuriosItemHandler handler = getCuriosHandler(livingEntity);
        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            ICurioStacksHandler stacksHandler = curios.get(identifier);

            if (stacksHandler != null) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                if (index < stackHandler.getSlots()) {
                    stackHandler.setStackInSlot(index, stack);
                }
            }
        }
    }

    @Override
    public SlotResult findFirstCurio(@NotNull EntityLivingBase livingEntity, Item item) {
        return findFirstCurio(livingEntity, (stack) -> stack.getItem() == item);
    }

    @Override
    public SlotResult findFirstCurio(EntityLivingBase livingEntity, Predicate<ItemStack> filter) {
        ICuriosItemHandler handler = getCuriosHandler(livingEntity);
        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();

            for (String id : curios.keySet()) {
                ICurioStacksHandler stacksHandler = curios.get(id);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack != null && filter.test(stack)) {
                        return new SlotResult(new SlotContext(id, livingEntity, i), stack);
                    }
                }
            }
            return null;
        }
        return null;
    }

    @Override
    public List<SlotResult> findCurios(@NotNull EntityLivingBase livingEntity, Item item) {
        return findCurios(livingEntity, (stack) -> stack.getItem() == item);
    }

    @Override
    public List<SlotResult> findCurios(EntityLivingBase livingEntity, Predicate<ItemStack> filter) {
        List<SlotResult> result = new ArrayList<>();
        ICuriosItemHandler handler = getCuriosHandler(livingEntity);
        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (String id : curios.keySet()) {
                ICurioStacksHandler stacksHandler = curios.get(id);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                for (int i = 0; i < stackHandler.getSlots(); i++) {
                    ItemStack stack = stackHandler.getStackInSlot(i);
                    if (stack != null && filter.test(stack)) {
                        result.add(new SlotResult(new SlotContext(id, livingEntity, i), stack));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<SlotResult> findCurios(EntityLivingBase livingEntity, String... identifiers) {
        List<SlotResult> result = new ArrayList<>();
        Set<String> ids = Arrays.stream(identifiers)
            .collect(Collectors.toSet());
        ICuriosItemHandler handler = getCuriosHandler(livingEntity);
        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            for (String id : ids) {
                if (ids.contains(id)) {
                    ICurioStacksHandler stacksHandler = curios.get(id);
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = stackHandler.getStackInSlot(i);
                        if (stack != null) {
                            result.add(new SlotResult(new SlotContext(id, livingEntity, i), stack));
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public SlotResult findCurio(EntityLivingBase livingEntity, String identifier, int index) {
        ICuriosItemHandler handler = getCuriosHandler(livingEntity);
        if (handler != null) {
            Map<String, ICurioStacksHandler> curios = handler.getCurios();
            ICurioStacksHandler stacksHandler = curios.get(identifier);
            if (stacksHandler != null) {
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (index < stackHandler.getSlots()) {
                    ItemStack stack = stackHandler.getStackInSlot(index);
                    if (stack != null) {
                        return new SlotResult(new SlotContext(identifier, livingEntity, index), stack);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
        ItemStack stack) {
        Multimap<IAttribute, AttributeModifier> multimap = HashMultimap.create();
        if (stack != null && stack.hasTagCompound()
            && stack.getTagCompound()
                .hasKey("CurioAttributeModifiers", 9)) {
            NBTTagList listnbt = stack.getTagCompound()
                .getTagList("CurioAttributeModifiers", 10);
            String identifier = slotContext.id();

            for (int i = 0; i < listnbt.tagCount(); ++i) {
                NBTTagCompound compoundnbt = listnbt.getCompoundTagAt(i);

                if (compoundnbt.getString("Slot")
                    .equals(identifier)) {
                    String attrName = compoundnbt.getString("AttributeName");
                    UUID id = uuid;

                    if (!attrName.isEmpty()) {
                        if (compoundnbt.hasKey("UUID")) {
                            id = UUID.fromString(compoundnbt.getString("UUID"));
                        }

                        if (id.getLeastSignificantBits() != 0L && id.getMostSignificantBits() != 0L) {
                            int operation = compoundnbt.getInteger("Operation");
                            double amount = compoundnbt.getDouble("Amount");
                            String name = compoundnbt.getString("Name");

                            if (attrName.startsWith(Reference.PREFIX_MOD)) {
                                String identifier1 = attrName.substring(Reference.PREFIX_MOD.length());
                                if (CuriosApi.getSlotHelper()
                                    .getSlotType(identifier1) != null) {
                                    this.addSlotModifier(multimap, identifier1, id, amount, operation);
                                }
                            } else {
                                IAttribute attribute = getAttributeByNameLegacy(attrName);
                                if (attribute != null) {
                                    multimap.put(attribute, new AttributeModifier(id, name, amount, operation));
                                }
                            }
                        }
                    }
                }
            }
            return multimap;
        }

        ICurio curio = getCurio(stack);
        if (curio != null) {
            return curio.getAttributeModifiers(slotContext, uuid);
        }

        return HashMultimap.create();
    }

    private IAttribute getAttributeByNameLegacy(String name) {
        return null;
    }

    @Override
    public void addSlotModifier(Multimap<IAttribute, AttributeModifier> map, String identifier, UUID uuid,
        double amount, int operation) {
        map.put(getOrCreateSlotAttribute(identifier), new AttributeModifier(uuid, identifier, amount, operation));
    }

    @Override
    public void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid, double amount,
        int operation, String slot) {
        addModifier(stack, getOrCreateSlotAttribute(identifier), name, uuid, amount, operation, slot);
    }

    @Override
    public void addModifier(ItemStack stack, IAttribute attribute, String name, UUID uuid, double amount, int operation,
        String slot) {
        if (stack == null) return;

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tag = stack.getTagCompound();

        if (!tag.hasKey("CurioAttributeModifiers", 9)) {
            tag.setTag("CurioAttributeModifiers", new NBTTagList());
        }
        NBTTagList listtag = tag.getTagList("CurioAttributeModifiers", 10);
        NBTTagCompound compoundtag = new NBTTagCompound();
        compoundtag.setString("Name", name);
        compoundtag.setDouble("Amount", amount);
        compoundtag.setInteger("Operation", operation);

        if (uuid != null) {
            compoundtag.setString("UUID", uuid.toString());
        }
        String id = "";

        if (attribute instanceof SlotAttributeWrapper wrapper) {
            id = "curios:" + wrapper.identifier;
        } else {
            id = attribute.getAttributeUnlocalizedName();
        }

        if (!id.isEmpty()) {
            compoundtag.setString("AttributeName", id);
        }
        compoundtag.setString("Slot", slot);
        listtag.appendTag(compoundtag);
    }

    @Override
    public boolean isStackValid(SlotContext slotContext, ItemStack stack) {
        if (stack == null) return false;
        String id = slotContext.id();
        Set<String> tags = getCurioTags(stack.getItem());
        return tags.contains(id);
    }

    @Override
    public void onBrokenCurio(String id, int index, EntityLivingBase wearer) {
        if (brokenCurioConsumer != null) {
            brokenCurioConsumer.accept(id, index, wearer);
        }
    }

    @Override
    public void setBrokenCurioConsumer(CurioBrokenConsumer consumer) {
        if (brokenCurioConsumer == null) {
            brokenCurioConsumer = consumer;
        }
    }

    public static SlotAttributeWrapper getOrCreateSlotAttribute(String identifier) {
        SlotAttributeWrapper wrapper = SLOT_ATTRIBUTES.get(identifier);
        if (wrapper == null) {
            wrapper = new SlotAttributeWrapper(identifier);
            SLOT_ATTRIBUTES.put(identifier, wrapper);
        }
        return wrapper;
    }

    public static NBTTagCompound writeAttributeModifier(AttributeModifier modifier) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(
            "UUID",
            modifier.getID()
                .toString());
        tag.setString("Name", modifier.getName());
        tag.setDouble("Amount", modifier.getAmount());
        tag.setInteger("Operation", modifier.getOperation());
        return tag;
    }

    public static AttributeModifier readAttributeModifier(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("UUID")) return null;
        try {
            UUID uuid = UUID.fromString(tag.getString("UUID"));
            String name = tag.getString("Name");
            double amount = tag.getDouble("Amount");
            int operation = tag.getInteger("Operation");
            return new AttributeModifier(uuid, name, amount, operation);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static class SlotAttributeWrapper extends BaseAttribute {

        public final String identifier;

        private SlotAttributeWrapper(String identifier) {
            super(Reference.MOD_ID + ".slot." + identifier, 0.0D);
            this.identifier = identifier;
        }

        @Override
        public double clampValue(double value) {
            return 0;
        }
    }
}
