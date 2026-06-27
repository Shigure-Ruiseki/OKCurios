package ruiseki.okcurios.common.network.server.sync;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;

public class SPacketSyncRender extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int slotId = -1;
    @CodecField
    private String curioId = "";
    @CodecField
    private boolean value = false;

    public SPacketSyncRender() {}

    public SPacketSyncRender(final int entityId, final int slotId, final String curioId, final boolean value) {
        this.entityId = entityId;
        this.slotId = slotId;
        this.curioId = curioId;
        this.value = value;
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
                                int index = this.slotId;
                                NonNullList<Boolean> renderStatuses = stacksHandler.getRenders();
                                if (renderStatuses.size() > index) {
                                    renderStatuses.set(index, this.value);
                                }
                            }));
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
