package ruiseki.okcurios.common.data.slot;

import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonElement;

import ruiseki.okcore.data.loader.DataReader;

public class SlotReader extends DataReader<SlotHolder> {

    public SlotReader(ResourceLocation id, String fileName) {
        super(id, fileName);
    }

    @Override
    protected SlotHolder readData(ResourceLocation id, JsonElement root, String resourceName) {
        return null;
    }
}
