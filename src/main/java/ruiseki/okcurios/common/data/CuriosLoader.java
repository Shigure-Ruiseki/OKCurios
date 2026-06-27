package ruiseki.okcurios.common.data;

import java.io.InputStream;

import net.minecraft.util.ResourceLocation;

import ruiseki.okcore.data.loader.DataLoader;
import ruiseki.okcore.data.loader.IDataLoader;

@DataLoader
public class CuriosLoader implements IDataLoader {

    @Override
    public String getTargetFolder() {
        return "curios";
    }

    @Override
    public void process(ResourceLocation id, String namespace, String folder, String[] subPaths, String fileName,
        InputStream inputStream) {

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
