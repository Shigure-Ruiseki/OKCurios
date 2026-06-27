package ruiseki.okcurios.common.network.server.sync;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.PacketCodec;

public class SPacketSyncData extends PacketCodec {

    @CodecField
    private NBTTagCompound data = new NBTTagCompound();

    public SPacketSyncData() {}

    public SPacketSyncData(final NBTTagCompound data) {
        this.data = data;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        // TODO: Add DataLoader
        // CuriosEntityManager.applySyncPacket(this.data);
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
