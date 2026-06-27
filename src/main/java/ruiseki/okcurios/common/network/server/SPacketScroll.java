package ruiseki.okcurios.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.client.gui.CuriosScreen;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class SPacketScroll extends PacketCodec {

    @CodecField
    private int windowId = -1;
    @CodecField
    private int index = -1;

    public SPacketScroll() {}

    public SPacketScroll(final int windowId, final int index) {
        this.windowId = windowId;
        this.index = index;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (world != null) {
            Minecraft mc = Minecraft.getMinecraft();
            if (player != null) {
                Container container = player.openContainer;
                if (container instanceof CuriosContainer curiosContainer && container.windowId == this.windowId) {
                    curiosContainer.scrollToIndex(this.index);
                }
            }
            if (mc.currentScreen instanceof CuriosScreen curiosScreen) {
                curiosScreen.updateRenderButtons();
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
