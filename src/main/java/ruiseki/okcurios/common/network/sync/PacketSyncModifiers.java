package ruiseki.okcurios.common.network.sync;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import ruiseki.okcore.network.CodecField;
import ruiseki.okcore.network.ExtendedBuffer;
import ruiseki.okcore.network.PacketCodec;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.event.SlotModifiersUpdatedEvent;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;

public class PacketSyncModifiers extends PacketCodec {

    @CodecField
    private int entityId = -1;
    @CodecField
    private int entrySize = -1;
    private Map<String, NBTTagCompound> updates = new LinkedHashMap<>();

    public PacketSyncModifiers() {}

    public PacketSyncModifiers(int entityId, Set<ICurioStacksHandler> updates) {
        Map<String, NBTTagCompound> result = new LinkedHashMap<>();

        for (ICurioStacksHandler stacksHandler : updates) {
            result.put(stacksHandler.getIdentifier(), stacksHandler.getSyncTag());
        }
        this.entityId = entityId;
        this.entrySize = result.size();
        this.updates = result;
    }

    @Override
    public void encode(ExtendedBuffer output) {
        super.encode(output);

        for (Map.Entry<String, NBTTagCompound> entry : updates.entrySet()) {
            output.writeString(entry.getKey());
            try {
                output.writeNBTTagCompoundToBuffer(entry.getValue());
            } catch (IOException ignore) {}
        }
    }

    @Override
    public void decode(ExtendedBuffer input) {
        super.decode(input);
        for (int i = 0; i < entrySize; i++) {
            String key = input.readString();
            try {
                updates.put(key, input.readNBTTagCompoundFromBuffer());
            } catch (IOException ignore) {}
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void actionClient(World world, EntityPlayer player) {
        if (world != null) {
            Entity entity = world.getEntityByID(entityId);
            if (entity instanceof EntityLivingBase livingBase) {
                ICuriosItemHandler handler = CuriosApi.getCuriosHelper()
                    .getCuriosHandler(livingBase);
                if (handler != null) {
                    Map<String, ICurioStacksHandler> curios = handler.getCurios();

                    for (Map.Entry<String, NBTTagCompound> entry : this.updates.entrySet()) {
                        String id = entry.getKey();
                        ICurioStacksHandler stacksHandler = curios.get(id);

                        if (stacksHandler != null) {
                            stacksHandler.applySyncTag(entry.getValue());
                        }
                    }

                    if (!this.updates.isEmpty()) {
                        MinecraftForge.EVENT_BUS.post(new SlotModifiersUpdatedEvent(livingBase, this.updates.keySet()));
                    }

                    if (entity instanceof EntityPlayer entityPlayer) {
                        if (entityPlayer.openContainer instanceof CuriosContainer) {
                            ((CuriosContainer) player.openContainer).resetSlots();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void actionServer(World world, EntityPlayerMP player) {

    }
}
