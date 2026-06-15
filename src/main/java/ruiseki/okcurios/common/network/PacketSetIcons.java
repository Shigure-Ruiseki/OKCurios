package ruiseki.okcurios.common.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.ExtendedBuffer;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;

public class PacketSetIcons extends PacketCodec {

    @CodecField
    private int entrySize = 0;
    private Map<String, ResourceLocation> map = new HashMap<>();

    public PacketSetIcons() {}

    public PacketSetIcons(Map<String, ResourceLocation> map) {
        this.map = map;
        this.entrySize = map.size();
    }

    @Override
    public void encode(ExtendedBuffer output) {
        super.encode(output);
        for (Map.Entry<String, ResourceLocation> entry : map.entrySet()) {
            output.writeString(entry.getKey());
            try {
                output.writeResourceLocation(entry.getValue());
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void decode(ExtendedBuffer input) {
        super.decode(input);
        for (int i = 0; i < entrySize; i++) {
            try {
                map.put(input.readString(), input.readResourceLocation());
            } catch (IOException ignore) {}
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        Set<String> slotIds = new HashSet<>();

        if (world != null) {
            CuriosApi.getIconHelper()
                .clearIcons();

            for (Map.Entry<String, ResourceLocation> entry : map.entrySet()) {
                CuriosApi.getIconHelper()
                    .addIcon(entry.getKey(), entry.getValue());
                slotIds.add(entry.getKey());
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
