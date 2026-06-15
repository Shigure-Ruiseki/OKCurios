package ruiseki.okcurios.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.OKCurios;

public class PacketOpenVanilla extends PacketCodec {

    public PacketOpenVanilla() {}

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
            player.openContainer.onContainerClosed(player);
            player.openContainer = player.inventoryContainer;
            if (mouseStack != null) {
                player.inventory.setItemStack(mouseStack);
                OKCurios.instance.getPacketHandler()
                    .sendToPlayer(new PacketGrabbedItem(mouseStack), player);
            }
        }
    }
}
