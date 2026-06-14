package ruiseki.okcurios.api;

import net.minecraft.entity.EntityLivingBase;

public record SlotContext(String id, EntityLivingBase wearer, int index) {

    public SlotContext() {
        this("", null, -1);
    }

    public SlotContext(String id) {
        this(id, null, -1);
    }

    public SlotContext(String id, EntityLivingBase wearer) {
        this(id, wearer, -1);
    }
}
