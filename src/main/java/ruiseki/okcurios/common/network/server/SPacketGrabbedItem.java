package ruiseki.okcurios.common.network.server;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class SPacketGrabbedItem extends PacketCodec {

    @CodecField
    private ItemStack stack = null;

    public SPacketGrabbedItem() {}

    public SPacketGrabbedItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (player != null) {
            player.inventory.setItemStack(this.stack);
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
