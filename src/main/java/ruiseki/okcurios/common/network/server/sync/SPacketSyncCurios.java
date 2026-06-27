package ruiseki.okcurios.common.network.server.sync;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.ExtendedBuffer;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.common.inventory.CurioStacksHandler;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class SPacketSyncCurios extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int entrySize = -1;
    private Map<String, NBTTagCompound> map = new LinkedHashMap<>();

    public SPacketSyncCurios() {}

    public SPacketSyncCurios(int entityId, Map<String, ICurioStacksHandler> map) {
        Map<String, NBTTagCompound> result = new LinkedHashMap<>();

        for (Map.Entry<String, ICurioStacksHandler> entry : map.entrySet()) {
            result.put(
                entry.getKey(),
                entry.getValue()
                    .getSyncTag());
        }
        this.entityId = entityId;
        this.entrySize = map.size();
        this.map = result;
    }

    public SPacketSyncCurios(Map<String, NBTTagCompound> map, int entityId) {
        this.entityId = entityId;
        this.entrySize = map.size();
        this.map = map;
    }

    @Override
    public void encode(ExtendedBuffer output) {
        super.encode(output);
        for (Map.Entry<String, NBTTagCompound> entry : this.map.entrySet()) {
            output.writeString(entry.getKey());
            try {
                output.writeNBTTagCompoundToBuffer(entry.getValue());
            } catch (IOException ignored) {}
        }

    }

    @Override
    public void decode(ExtendedBuffer input) {
        super.decode(input);
        for (int i = 0; i < entrySize; i++) {
            String key = input.readString();
            try {
                this.map.put(key, input.readNBTTagCompoundFromBuffer());
            } catch (IOException ignored) {}
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {

        if (world != null) {
            Entity entity = world.getEntityByID(this.entityId);

            if (entity instanceof EntityLivingBase livingBase) {
                CuriosApi.getCuriosInventory(livingBase)
                    .ifPresent(handler -> {
                        Map<String, ICurioStacksHandler> stacks = new LinkedHashMap<>();

                        for (Map.Entry<String, NBTTagCompound> entry : this.map.entrySet()) {
                            ICurioStacksHandler stacksHandler = new CurioStacksHandler(handler, entry.getKey());
                            stacksHandler.applySyncTag(entry.getValue());
                            stacks.put(entry.getKey(), stacksHandler);
                        }
                        handler.setCurios(stacks);

                        if (entity instanceof EntityPlayer entityPlayer
                            && entityPlayer.openContainer instanceof CuriosContainer curiosContainer) {
                            curiosContainer.resetSlots();
                        }
                    });
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
