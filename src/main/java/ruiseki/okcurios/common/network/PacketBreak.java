package ruiseki.okcurios.common.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;

public class PacketBreak extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int slotId = -1;
    @CodecField
    private String curioId = "";

    public PacketBreak() {}

    public PacketBreak(int entityId, int slotId, String curioId) {
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
            if (entity instanceof EntityLivingBase livingBase) {
                ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                    .getCuriosHandler(livingBase);
                if (handler != null) {
                    ItemStack stack = handler.getStacksHandler(curioId)
                        .getStacks()
                        .getStackInSlot(slotId);
                    ICurio curio = CuriosApi.getCuriosHelper()
                        .getCurio(stack);
                    if (curio != null) {
                        curio.curioBreak(stack, livingBase);
                    }

                    if (curio == null) {
                        ICurio.playBreakAnimation(stack, player);
                    }
                }
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
