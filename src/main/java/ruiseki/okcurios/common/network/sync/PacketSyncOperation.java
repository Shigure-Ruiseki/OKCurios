package ruiseki.okcurios.common.network.sync;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.event.SlotModifiersUpdatedEvent;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.common.SlotHelper;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class PacketSyncOperation extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private String curioId = "";
    @CodecField
    private int operation = -1;
    @CodecField
    private int amount = -1;
    @CodecField
    private boolean visible = false;
    @CodecField
    private boolean cosmetic = false;

    public PacketSyncOperation() {}

    public PacketSyncOperation(int entityId, String curioId, Operation operation) {
        this(entityId, curioId, operation, 0);
    }

    public PacketSyncOperation(int entityId, String curioId, Operation operation, int amount) {
        this(entityId, curioId, operation, amount, true, false);
    }

    public PacketSyncOperation(int entityId, String curioId, Operation operation, int amount, boolean visible,
        boolean cosmetic) {
        this.entityId = entityId;
        this.curioId = curioId;
        this.amount = amount;
        this.operation = operation.ordinal();
        this.visible = visible;
        this.cosmetic = cosmetic;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (world != null) {
            Entity entity = world.getEntityByID(entityId);
            if (entity instanceof EntityLivingBase livingBase) {
                ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                    .getCuriosHandler(livingBase);

                if (handler != null) {
                    Operation op = Operation.fromValue(this.operation);
                    String id = this.curioId;
                    int amount = this.amount;

                    switch (op) {
                        case ADD:
                            var curiosHelper = CuriosApi.getCuriosHelper();
                            Multimap<IAttribute, AttributeModifier> map = HashMultimap.create();
                            curiosHelper.addSlotModifier(map, id, SlotHelper.MODIFIER_UUID, amount, 0);

                            BaseAttributeMap attributeMap = livingBase.getAttributeMap();
                            for (Map.Entry<IAttribute, AttributeModifier> entry : map.entries()) {
                                IAttributeInstance instance = attributeMap.getAttributeInstance(entry.getKey());
                                if (instance != null) {
                                    instance.removeModifier(entry.getValue());
                                    instance.applyModifier(entry.getValue());
                                }
                            }
                            break;
                        case LOCK:
                            handler.lockSlotType(id);
                            break;
                        case UNLOCK:
                            handler.unlockSlotType(id, amount, this.visible, this.cosmetic);
                            break;
                    }

                    MinecraftForge.EVENT_BUS.post(new SlotModifiersUpdatedEvent(livingBase, Sets.newHashSet(id)));

                    if (entity instanceof EntityPlayer entityPlayer) {
                        if (entityPlayer.openContainer instanceof CuriosContainer) {
                            ((CuriosContainer) entityPlayer.openContainer).resetSlots();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {}

    public enum Operation {

        LOCK,
        UNLOCK,
        ADD;

        public static Operation fromValue(int value) {
            try {
                return Operation.values()[value];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Unknown operation value: " + value);
            }
        }
    }
}
