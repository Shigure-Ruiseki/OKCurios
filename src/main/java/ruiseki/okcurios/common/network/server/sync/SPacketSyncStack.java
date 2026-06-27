package ruiseki.okcurios.common.network.server.sync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;

public class SPacketSyncStack extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int slotId = -1;
    @CodecField
    private String curioId = "";
    @CodecField
    private ItemStack stack = null;
    @CodecField
    private int handlerType = -1;
    @CodecField
    private NBTTagCompound compound = new NBTTagCompound();

    public SPacketSyncStack() {}

    public SPacketSyncStack(int entityId, String curioId, int slotId, ItemStack stack, HandlerType handlerType,
        NBTTagCompound data) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.stack = (stack != null) ? stack.copy() : null;
        this.curioId = curioId;
        this.handlerType = handlerType.ordinal();
        this.compound = data;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (world != null) {
            Entity entity = world.getEntityByID(entityId);
            if (entity instanceof EntityLivingBase livingEntity) {
                CuriosApi.getCuriosInventory(livingEntity)
                    .ifPresent(
                        handler -> handler.getStacksHandler(this.curioId)
                            .ifPresent(stacksHandler -> {
                                ItemStack stack = this.stack;
                                NBTTagCompound compound = this.compound;
                                int slot = this.slotId;
                                boolean cosmetic = HandlerType.fromValue(this.handlerType) == HandlerType.COSMETIC;

                                if (stack != null && stack.hasTagCompound() && compound != null) {
                                    NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                                    CuriosApi.getCurio(stack)
                                        .ifPresent(
                                            curio -> curio.deserializeNBT(
                                                new SlotContext(
                                                    this.curioId,
                                                    livingEntity,
                                                    slot,
                                                    cosmetic,
                                                    renderStates.size() > slot && renderStates.get(slot)),
                                                compound));
                                }

                                if (cosmetic) {
                                    stacksHandler.getCosmeticStacks()
                                        .setStackInSlot(slot, stack);
                                } else {
                                    stacksHandler.getStacks()
                                        .setStackInSlot(slot, stack);
                                }
                            }));
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }

    public enum HandlerType {

        EQUIPMENT,
        COSMETIC;

        public static HandlerType fromValue(int value) {
            try {
                return HandlerType.values()[value];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Unknown handler value: " + value);
            }
        }
    }
}
