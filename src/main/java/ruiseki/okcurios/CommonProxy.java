package ruiseki.okcurios;

import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.FMLCommonHandler;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.network.PacketHandler;
import ruiseki.okcore.proxy.CommonProxyComponent;
import ruiseki.okcurios.client.gui.GuiEventHandler;
import ruiseki.okcurios.common.event.CuriosEventHandler;
import ruiseki.okcurios.common.network.PacketBreak;
import ruiseki.okcurios.common.network.PacketGrabbedItem;
import ruiseki.okcurios.common.network.PacketOpenCurios;
import ruiseki.okcurios.common.network.PacketOpenVanilla;
import ruiseki.okcurios.common.network.PacketScroll;
import ruiseki.okcurios.common.network.PacketSetIcons;
import ruiseki.okcurios.common.network.sync.PacketSyncCurios;
import ruiseki.okcurios.common.network.sync.PacketSyncModifiers;
import ruiseki.okcurios.common.network.sync.PacketSyncOperation;
import ruiseki.okcurios.common.network.sync.PacketSyncStack;

public class CommonProxy extends CommonProxyComponent {

    @Override
    public ModBase getMod() {
        return OKCurios.instance;
    }

    @Override
    public void registerPacketHandlers(PacketHandler packetHandler) {
        packetHandler.register(PacketSyncCurios.class);
        packetHandler.register(PacketSyncModifiers.class);
        packetHandler.register(PacketSyncStack.class);
        packetHandler.register(PacketSyncOperation.class);

        packetHandler.register(PacketGrabbedItem.class);
        packetHandler.register(PacketOpenVanilla.class);
        packetHandler.register(PacketScroll.class);
        packetHandler.register(PacketSetIcons.class);
        packetHandler.register(PacketBreak.class);
        packetHandler.register(PacketOpenCurios.class);
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
