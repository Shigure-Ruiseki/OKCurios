package ruiseki.okcurios.common.data.slot;

import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcurios.api.type.ISlotType;

// TODO: Add DataLoader
public class CuriosSlotManager {

    public static CuriosSlotManager INSTANCE = new CuriosSlotManager();
    private Map<String, ISlotType> slots = ImmutableMap.of();
    private Map<String, ResourceLocation> icons = ImmutableMap.of();
    private Map<String, Set<String>> idToMods = ImmutableMap.of();

    public CuriosSlotManager() {}

}
