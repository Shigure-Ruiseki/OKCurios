package ruiseki.okcurios.common.network.server;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.SlotContext;
import ruiseki.okcurios.api.type.capability.ICurio;

public class SPacketBreak extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int slotId = -1;
    @CodecField
    private String curioId = "";

    public SPacketBreak() {}

    public SPacketBreak(int entityId, int slotId, String curioId) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.curioId = curioId;
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
                            .ifPresent(stacks -> {
                                ItemStack stack = stacks.getStacks()
                                    .getStackInSlot(this.slotId);
                                LazyOptional<ICurio> possibleCurio = CuriosApi.getCurio(stack);
                                NonNullList<Boolean> renderStates = stacks.getRenders();
                                possibleCurio.ifPresent(
                                    curio -> curio.curioBreak(
                                        new SlotContext(
                                            this.curioId,
                                            livingEntity,
                                            this.slotId,
                                            false,
                                            renderStates.size() > this.slotId && renderStates.get(this.slotId))));

                                if (!possibleCurio.isPresent()) {
                                    ICurio.playBreakAnimation(stack, livingEntity);
                                }
                            }));
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
