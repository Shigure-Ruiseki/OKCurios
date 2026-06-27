package ruiseki.okcurios.common.network.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.common.inventory.container.CuriosGuiHandler;
import ruiseki.okcurios.common.network.server.SPacketGrabbedItem;

public class CPacketOpenCurios extends PacketCodec {

    public CPacketOpenCurios() {}

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
            ItemStack mouseStack = player.inventory.getItemStack();

            player.inventory.setItemStack(null);
            player.openGui(
                OKCurios.instance,
                CuriosGuiHandler.CURIOS_GUI_ID,
                player.worldObj,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);

            if (mouseStack != null) {
                player.inventory.setItemStack(mouseStack);
                OKCurios.instance.getPacketHandler()
                    .sendToPlayer(new SPacketGrabbedItem(mouseStack), player);
            }
        }
    }
}
