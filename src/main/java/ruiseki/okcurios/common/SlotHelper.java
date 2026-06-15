package ruiseki.okcurios.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayerMP;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.type.ISlotType;
import ruiseki.okcurios.api.type.helper.ISlotHelper;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;
import ruiseki.okcurios.common.network.sync.PacketSyncOperation;

public class SlotHelper implements ISlotHelper {

    public static final UUID MODIFIER_UUID = UUID.fromString("627c2b3e-cb59-4592-b883-93d39bd01083");

    private final Map<String, ISlotType> idToType = new HashMap<String, ISlotType>();

    @Override
    public void addSlotType(ISlotType slotType) {
        if (slotType != null && slotType.getIdentifier() != null) {
            this.idToType.put(slotType.getIdentifier(), slotType);
        }
    }

    @Override
    public ISlotType getSlotType(String identifier) {
        return this.idToType.get(identifier);
    }

    @Override
    public Collection<ISlotType> getSlotTypes() {
        return Collections.unmodifiableCollection(idToType.values());
    }

    @Override
    public Collection<ISlotType> getSlotTypes(EntityLivingBase livingEntity) {
        return getSlotTypes();
    }

    @Override
    public Set<String> getSlotTypeIds() {
        return Collections.unmodifiableSet(idToType.keySet());
    }

    @Override
    public int getSlotsForType(EntityLivingBase livingEntity, String id) {
        var handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        if (handler != null) {
            var stacksHandler = handler.getStacksHandler(id);
            if (stacksHandler != null) {
                return stacksHandler.getSlots();
            }
        }
        return 0;
    }

    @Override
    public void setSlotsForType(String id, EntityLivingBase livingEntity, int amount) {
        ISlotType type = this.getSlotType(id);
        int baseSlots = (type != null) ? type.getSize() : 0;

        int modifierAmount = amount - baseSlots;

        var curiosHelper = CuriosApi.getCuriosHelper();
        var handler = curiosHelper.getCuriosHandler(livingEntity);

        if (handler != null) {
            Multimap<IAttribute, AttributeModifier> map = HashMultimap.create();
            curiosHelper.addSlotModifier(map, id, MODIFIER_UUID, modifierAmount, 0);

            BaseAttributeMap attributeMap = livingEntity.getAttributeMap();
            for (Map.Entry<IAttribute, AttributeModifier> entry : map.entries()) {
                IAttributeInstance instance = attributeMap.getAttributeInstance(entry.getKey());
                if (instance != null) {
                    instance.removeModifier(entry.getValue());
                    instance.applyModifier(entry.getValue());
                }
            }

            if (livingEntity instanceof EntityPlayerMP player) {
                PacketSyncOperation packet = new PacketSyncOperation(
                    player.getEntityId(),
                    id,
                    PacketSyncOperation.Operation.ADD,
                    modifierAmount);
                OKCurios.instance.getPacketHandler()
                    .sendToTrackingAndSelf(packet, player);

                if (player.openContainer instanceof CuriosContainer) {
                    ((CuriosContainer) player.openContainer).resetSlots();
                }
            }
        }
    }

    @Override
    public void unlockSlotType(String id, EntityLivingBase livingEntity) {
        var handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        ISlotType type = this.getSlotType(id);

        if (handler != null && type != null) {
            handler.unlockSlotType(id, type.getSize(), type.isVisible(), type.hasCosmetic());

            if (livingEntity instanceof EntityPlayerMP player) {
                PacketSyncOperation packet = new PacketSyncOperation(
                    player.getEntityId(),
                    id,
                    PacketSyncOperation.Operation.UNLOCK,
                    type.getSize(),
                    type.isVisible(),
                    type.hasCosmetic());
                OKCurios.instance.getPacketHandler()
                    .sendToTrackingAndSelf(packet, player);

                if (player.openContainer instanceof CuriosContainer) {
                    ((CuriosContainer) player.openContainer).resetSlots();
                }
            }
        }
    }

    @Override
    public void lockSlotType(String id, EntityLivingBase livingEntity) {
        var handler = CuriosApi.getCuriosHelper()
            .getCuriosHandler(livingEntity);
        if (handler != null) {
            handler.lockSlotType(id);

            if (livingEntity instanceof EntityPlayerMP player) {
                PacketSyncOperation packet = new PacketSyncOperation(
                    player.getEntityId(),
                    id,
                    PacketSyncOperation.Operation.LOCK);
                OKCurios.instance.getPacketHandler()
                    .sendToTrackingAndSelf(packet, player);

                if (player.openContainer instanceof CuriosContainer) {
                    ((CuriosContainer) player.openContainer).resetSlots();
                }
            }
        }
    }
}
