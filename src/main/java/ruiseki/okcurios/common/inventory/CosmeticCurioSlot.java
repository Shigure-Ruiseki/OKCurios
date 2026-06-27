package ruiseki.okcurios.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcurios.Reference;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;

public class CosmeticCurioSlot extends CurioSlot {

    public CosmeticCurioSlot(EntityPlayer player, IDynamicStackHandler handler, int index, String identifier,
        int xPosition, int yPosition) {
        super(player, handler, index, identifier, xPosition, yPosition, NonNullList.create(), true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void bindClientDirectTexture() {
        this.setBackgroundIconTexture(new ResourceLocation(Reference.MOD_ID, "item/empty_cosmetic_slot"));
    }

    @Override
    public boolean getRenderStatus() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getSlotName() {
        return LangHelpers.localize("curios.cosmetic") + " " + super.getSlotName();
    }
}
