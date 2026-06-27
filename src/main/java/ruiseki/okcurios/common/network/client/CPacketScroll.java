package ruiseki.okcurios.common.network.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class CPacketScroll extends PacketCodec {

    @CodecField
    private int windowId = -1;
    @CodecField
    private int index = -1;

    public CPacketScroll() {}

    public CPacketScroll(final int windowId, final int index) {
        this.windowId = windowId;
        this.index = index;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {

    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (player != null) {
            Container container = player.openContainer;
            if (container instanceof CuriosContainer curiosContainer && container.windowId == this.windowId) {
                curiosContainer.scrollToIndex(this.index);
            }
        }
    }
}
