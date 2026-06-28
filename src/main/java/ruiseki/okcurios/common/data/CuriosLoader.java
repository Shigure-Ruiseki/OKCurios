package ruiseki.okcurios.common.data;

import java.io.InputStream;

import net.minecraft.util.ResourceLocation;

import ruiseki.okcore.data.loader.DataLoader;
import ruiseki.okcore.data.loader.IDataLoader;
import ruiseki.okcurios.common.data.slot.SlotReader;

@DataLoader
public class CuriosLoader implements IDataLoader {

    @Override
    public String getTargetFolder() {
        return "curios";
    }

    @Override
    public void process(ResourceLocation id, String namespace, String folder, String[] subPaths, String fileName,
        InputStream inputStream) {
        if (subPaths == null || subPaths.length == 0) return;

        String subfolder = subPaths[0];
        if (subfolder.equals("slots")) {
            SlotReader reader = new SlotReader(id, fileName);
        }

        if (subfolder.equals("entities")) {

        }
    }

    @Override
    public boolean isModLoader() {
        return false;
    }

    @Override
    public boolean isWorldLoader() {
        return true;
    }
}
