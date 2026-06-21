package ruiseki.okcurios.common.slottype;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcurios.Reference;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.common.slottype.SlotType.Builder;

public class SlotTypeManager {

    private static final Map<String, Builder> imcBuilders = new HashMap<String, Builder>();
    private static final Map<String, Builder> configBuilders = new HashMap<String, Builder>();
    private static final Map<String, Set<String>> idsToMods = new HashMap<String, Set<String>>();

    public static Map<String, Set<String>> getIdsToMods() {
        return ImmutableMap.copyOf(idsToMods);
    }

    public static void buildSlotTypes() {
        Map<String, Builder> builders = !configBuilders.isEmpty() ? configBuilders : imcBuilders;

        String[] defaultPresets = { "ring", "necklace", "belt", "body" };
        for (String id : defaultPresets) {
            if (!builders.containsKey(id)) {
                Builder builder = new Builder(id);
                applyDefaultPreset(id, builder);
                builders.put(id, builder);
            }
        }

        for (Builder builder : builders.values()) {
            CuriosApi.getSlotHelper()
                .addSlotType(builder.build());
        }
    }

    private static void applyDefaultPreset(String id, Builder builder) {
        if ("ring".equals(id)) {
            builder.size(4);
            builder.priority(100);
            builder.icon(new ResourceLocation(Reference.MOD_ID, "textures/slot/empty_ring_slot.png"));
        } else if ("necklace".equals(id)) {
            builder.size(4);
            builder.priority(200);
            builder.icon(new ResourceLocation(Reference.MOD_ID, "textures/slot/empty_necklace_slot.png"));
        } else if ("belt".equals(id)) {
            builder.size(4);
            builder.priority(300);
            builder.icon(new ResourceLocation(Reference.MOD_ID, "textures/slot/empty_belt_slot.png"));
        }
        if ("body".equals(id)) {
            builder.size(4);
            builder.priority(100);
            builder.icon(new ResourceLocation(Reference.MOD_ID, "textures/slot/empty_body_slot.png"));
        }
    }
}
