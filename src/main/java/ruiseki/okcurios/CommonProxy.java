package ruiseki.okcurios;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okcurios.client.gui.GuiEventHandler;
import ruiseki.okcurios.common.event.CuriosEventHandler;
import ruiseki.okcurios.common.network.client.CPacketOpenCurios;
import ruiseki.okcurios.common.network.client.CPacketOpenVanilla;
import ruiseki.okcurios.common.network.client.CPacketScroll;
import ruiseki.okcurios.common.network.server.SPacketBreak;
import ruiseki.okcurios.common.network.server.SPacketGrabbedItem;
import ruiseki.okcurios.common.network.server.SPacketScroll;
import ruiseki.okcurios.common.network.server.SPacketSetIcons;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncCurios;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncData;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncModifiers;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncRender;
import ruiseki.okcurios.common.network.server.sync.SPacketSyncStack;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKCurios.instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        packetHandler.register(SPacketSyncCurios.class);
        packetHandler.register(SPacketSyncModifiers.class);
        packetHandler.register(SPacketSyncData.class);
        packetHandler.register(SPacketSyncRender.class);
        packetHandler.register(SPacketSyncStack.class);

        packetHandler.register(SPacketGrabbedItem.class);
        packetHandler.register(SPacketScroll.class);
        packetHandler.register(SPacketSetIcons.class);
        packetHandler.register(SPacketBreak.class);

        packetHandler.register(CPacketOpenCurios.class);
        packetHandler.register(CPacketOpenVanilla.class);
        packetHandler.register(CPacketScroll.class);
    }

    @Override
    public void registerEventHooks() {
        super.registerEventHooks();
        MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
        CuriosEventHandler curiosEventHandler = new CuriosEventHandler();
        MinecraftForge.EVENT_BUS.register(curiosEventHandler);
        FMLCommonHandler.instance()
            .bus()
            .register(curiosEventHandler);

    }
}
