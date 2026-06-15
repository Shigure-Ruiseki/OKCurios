package ruiseki.okcurios.common.network.sync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;

public class PacketSyncStack extends PacketCodec {

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
    private NBTTagCompound compound = null;

    public PacketSyncStack() {}

    public PacketSyncStack(int entityId, String curioId, int slotId, ItemStack stack, HandlerType handlerType,
        NBTTagCompound data) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.stack = stack.copy();
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
            if (entity instanceof EntityLivingBase livingBase) {
                ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                    .getCuriosHandler(livingBase);
                if (handler != null) {
                    ICurioStacksHandler stacksHandler = handler.getStacksHandler(this.curioId);
                    if (stacksHandler != null) {
                        ItemStack stack = this.stack;
                        NBTTagCompound compound = this.compound;
                        int slot = this.slotId;

                        if (stack.hasTagCompound()) {
                            ICurio curio = CuriosApi.getCuriosHelper()
                                .getCurio(stack);
                            curio.deserializeNBT(compound);
                        }

                        if (HandlerType.fromValue(handlerType) == HandlerType.COSMETIC) {
                            stacksHandler.getCosmeticStacks()
                                .setStackInSlot(slot, stack);
                        } else {
                            stacksHandler.getStacks()
                                .setStackInSlot(slot, stack);
                        }
                    }
                }
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
