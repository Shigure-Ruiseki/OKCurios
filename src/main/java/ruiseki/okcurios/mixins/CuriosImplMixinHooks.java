package ruiseki.okcurios.mixins;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.helper.EntityHelpers;
import ruiseki.okcore.helper.ItemStackHelpers;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.SlotAttribute;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.event.CurioAttributeModifierEvent;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICurioItem;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.common.network.server.SPacketBreak;

public class CuriosImplMixinHooks {

    private static final Map<Item, ICurioItem> REGISTRY = new ConcurrentHashMap<>();

    public static void registerCurio(Item item, ICurioItem icurio) {
        REGISTRY.put(item, icurio);
    }

    public static Optional<ICurioItem> getCurioFromRegistry(Item item) {
        return Optional.ofNullable(REGISTRY.get(item));
    }

    public static Optional<ISlotType> getSlot(String id) {
        return Optional.ofNullable(
            CuriosApi.getSlots()
                .get(id));
    }
    //
    // public static ResourceLocation getSlotIcon(String id) {
    // return CuriosSlotManager.INSTANCE.getIcon(id);
    // }
    //
    // public static Map<String, ISlotType> getSlots() {
    // return CuriosSlotManager.INSTANCE.getSlots();
    // }
    //
    // public static Map<String, ISlotType> getPlayerSlots() {
    // return CuriosApi.getEntitySlots(EntityPlayer.class);
    // }
    //
    // public static Map<String, ISlotType> getEntitySlots(Class<? extends Entity> type) {
    // return CuriosEntityManager.INSTANCE.getEntitySlots(type);
    // }
    //
    // public static Map<String, ISlotType> getItemStackSlots(ItemStack stack) {
    // Map<String, ISlotType> result = new HashMap<>();
    // Set<String> ids = TagHelpers.getTags(stack)
    // .stream()
    // .filter(
    // tagKey -> tagKey.location()
    // .getResourceDomain()
    // .equals(Reference.MOD_ID))
    // .map(
    // tagKey -> tagKey.location()
    // .getResourcePath())
    // .collect(Collectors.toSet());
    // Map<String, ISlotType> allSlots = CuriosSlotManager.INSTANCE.getSlots();
    //
    // for (String id : ids) {
    // ISlotType slotType = allSlots.get(id);
    //
    // if (slotType != null) {
    // result.put(id, slotType);
    // } else {
    // result.put(id, new SlotType.Builder(id).build());
    // }
    // }
    // return result;
    // }
    //
    // public static Map<String, ISlotType> getItemStackSlots(ItemStack stack, EntityLivingBase livingEntity) {
    // Map<String, ISlotType> result = new HashMap<>();
    // Set<String> ids = TagHelpers.getTags(stack)
    // .stream()
    // .filter(
    // tagKey -> tagKey.location()
    // .getResourceDomain()
    // .equals(Reference.MOD_ID))
    // .map(
    // tagKey -> tagKey.location()
    // .getResourcePath())
    // .collect(Collectors.toSet());
    // Map<String, ISlotType> entitySlots = getEntitySlots(livingEntity.getClass());
    //
    // for (String id : ids) {
    // ISlotType slotType = entitySlots.get(id);
    //
    // if (slotType != null) {
    // result.put(id, slotType);
    // } else {
    // result.put(id, new SlotType.Builder(id).build());
    // }
    // }
    // return result;
    // }

    public static LazyOptional<ICurio> getCurio(ItemStack stack) {
        return ItemStackHelpers.getCapability(stack, CuriosCapability.ITEM);
    }

    public static LazyOptional<ICuriosItemHandler> getCuriosInventory(EntityLivingBase livingEntity) {
        return EntityHelpers.getCapability(livingEntity, CuriosCapability.INVENTORY);
    }

    // public static boolean isStackValid(SlotContext slotContext, ItemStack stack) {
    // String id = slotContext.identifier();
    // Set<String> slots = getItemStackSlots(stack).keySet();
    // return (!slots.isEmpty() && id.equals("curio")) || slots.contains(id) || slots.contains("curio");
    // }

    public static Multimap<IAttribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid,
        ItemStack stack) {
        Multimap<IAttribute, AttributeModifier> multimap = HashMultimap.create();

        if (stack.getTagCompound() != null && stack.getTagCompound()
            .hasKey("CurioAttributeModifiers", 9)) {
            NBTTagList listnbt = stack.getTagCompound()
                .getTagList("CurioAttributeModifiers", 10);
            String identifier = slotContext.identifier();

            // Sửa về tagCount() cho 1.7.10
            for (int i = 0; i < listnbt.tagCount(); ++i) {
                NBTTagCompound compoundnbt = listnbt.getCompoundTagAt(i);

                if (compoundnbt.getString("Slot")
                    .equals(identifier)) {
                    String attributeName = compoundnbt.getString("AttributeName");
                    UUID id = uuid;

                    if (!attributeName.isEmpty()) {
                        if (compoundnbt.hasKey("UUID")) {
                            id = UUID.fromString(compoundnbt.getString("UUID"));
                        }

                        if (id.getLeastSignificantBits() != 0L && id.getMostSignificantBits() != 0L) {
                            int operation = compoundnbt.getInteger("Operation");
                            double amount = compoundnbt.getDouble("Amount");

                            String slotIdentifier = attributeName;
                            if (slotIdentifier.startsWith("curios:")) {
                                slotIdentifier = slotIdentifier.substring("curios:".length());
                            }

                            if (CuriosApi.getSlot(slotIdentifier)
                                .isPresent()) {
                                CuriosApi.addSlotModifier(multimap, slotIdentifier, id, amount, operation);
                            }
                        }
                    }
                }
            }
        } else {
            multimap = getCurio(stack).map(curio -> curio.getAttributeModifiers(slotContext, uuid))
                .orElse(multimap);
        }
        CurioAttributeModifierEvent evt = new CurioAttributeModifierEvent(stack, slotContext, uuid, multimap);
        MinecraftForge.EVENT_BUS.post(evt);
        return HashMultimap.create(evt.getModifiers());
    }

    public static void addSlotModifier(Multimap<IAttribute, AttributeModifier> map, String identifier, UUID uuid,
        double amount, int operation) {
        map.put(SlotAttribute.getOrCreate(identifier), new AttributeModifier(uuid, identifier, amount, operation));
    }

    public static void addSlotModifier(ItemStack stack, String identifier, String name, UUID uuid, double amount,
        int operation, String slot) {
        addModifier(stack, SlotAttribute.getOrCreate(identifier), name, uuid, amount, operation, slot);
    }

    public static void addModifier(ItemStack stack, IAttribute attribute, String name, UUID uuid, double amount,
        int operation, String slot) {
        if (stack.getTagCompound() == null) {
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
        if (attribute instanceof SlotAttribute wrapper) {
            id = "curios:" + wrapper.getIdentifier();
        }

        if (!id.isEmpty()) {
            compoundtag.setString("AttributeName", id);
        }
        compoundtag.setString("Slot", slot);
        listtag.appendTag(compoundtag);
    }

    public static void broadcastCurioBreakEvent(SlotContext slotContext) {
        OKCurios.instance.getPacketHandler()
            .sendToTrackingAndSelf(
                new SPacketBreak(
                    slotContext.entity()
                        .getEntityId(),
                    slotContext.index(),
                    slotContext.identifier()),
                slotContext.entity());
    }

}
