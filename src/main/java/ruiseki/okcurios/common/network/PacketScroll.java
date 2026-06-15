package ruiseki.okcurios.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.client.gui.CuriosScreen;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class PacketScroll extends PacketCodec {

    @CodecField
    private int windowId = -1;
    @CodecField
    private int index = -1;

    public PacketScroll() {}

    public PacketScroll(final int windowId, final int index) {
        this.windowId = windowId;
        this.index = index;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (world != null) {
            if (player != null) {
                Container container = player.openContainer;
                if (container instanceof CuriosContainer && container.windowId == this.windowId) {
                    ((CuriosContainer) container).scrollToIndex(this.index);
                }
            }
            GuiScreen screen = mc.currentScreen;
            if (screen instanceof CuriosScreen) {
                ((CuriosScreen) screen).updateRenderButtons();
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {
        if (player != null) {
            Container container = player.openContainer;
            if (container instanceof CuriosContainer && container.windowId == this.windowId) {
                ((CuriosContainer) container).scrollToIndex(this.index);
            }
        }
    }
}
