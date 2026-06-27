package ruiseki.okcurios.common;

import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.nbt.NBTTagCompound;

public class CuriosHelper {

    public static NBTTagCompound writeAttributeModifier(AttributeModifier modifier) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(
            "UUID",
            modifier.getID()
                .toString());
        tag.setString("Name", modifier.getName());
        tag.setDouble("Amount", modifier.getAmount());
        tag.setInteger("Operation", modifier.getOperation());
        return tag;
    }

    public static AttributeModifier readAttributeModifier(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("UUID")) return null;
        try {
            UUID uuid = UUID.fromString(tag.getString("UUID"));
            String name = tag.getString("Name");
            double amount = tag.getDouble("Amount");
            int operation = tag.getInteger("Operation");
            return new AttributeModifier(uuid, name, amount, operation);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
