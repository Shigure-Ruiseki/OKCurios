package ruiseki.okcurios.common.network.server;

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

public class SPacketSetIcons extends PacketCodec {

    @CodecField
    private int entrySize = 0;
    private Map<String, ResourceLocation> map = new HashMap<>();

    public SPacketSetIcons() {}

    public SPacketSetIcons(Map<String, ResourceLocation> map) {
        this.map = map;
        this.entrySize = map.size();
    }

    @Override
    public void encode(ExtendedBuffer output) {
        super.encode(output);
        for (Map.Entry<String, ResourceLocation> entry : map.entrySet()) {
            output.writeString(entry.getKey());
            output.writeResourceLocation(entry.getValue());
        }
    }

    @Override
    public void decode(ExtendedBuffer input) {
        super.decode(input);
        for (int i = 0; i < entrySize; i++) {
            map.put(input.readString(), input.readResourceLocation());
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
            Map<String, ResourceLocation> icons = new HashMap<>();

            for (Map.Entry<String, ResourceLocation> entry : this.map.entrySet()) {
                icons.put(entry.getKey(), entry.getValue());
                slotIds.add(entry.getKey());
            }
            // TODO: Add Data Loader
            // CuriosSlotManager.INSTANCE.setIcons(icons);
        }
        // TODO: Add Command
        // CurioArgumentType.slotIds = slotIds;
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
