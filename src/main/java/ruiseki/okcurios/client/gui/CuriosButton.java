package ruiseki.okcurios.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.common.inventory.container.CuriosGuiHandler;
import ruiseki.okcurios.common.network.client.CPacketOpenCurios;
import ruiseki.okcurios.common.network.client.CPacketOpenVanilla;

public class CuriosButton extends GuiButton {

    private final GuiContainer parentGui;
    private final int textureOffsetX;
    private final int textureOffsetY;
    private final ResourceLocation texture;

    public CuriosButton(int id, GuiContainer parentGui, int x, int y, int width, int height, int textureOffsetX,
        int textureOffsetY, ResourceLocation texture) {
        super(id, x, y, width, height, "");
        this.parentGui = parentGui;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        this.texture = texture;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            boolean isCreative = parentGui instanceof GuiContainerCreative;

            if (isCreative) {
                GuiContainerCreative creativeGui = (GuiContainerCreative) parentGui;
                boolean isInventoryTab = creativeGui.func_147056_g() == CreativeTabs.tabInventory.getTabIndex();
                this.enabled = isInventoryTab;
                if (!isInventoryTab) {
                    return;
                }
            }

            mc.getTextureManager()
                .bindTexture(this.texture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;

            int u = this.textureOffsetX;
            int v = this.textureOffsetY;

            if (this.field_146123_n) {
                v += this.height;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, u, v, this.width, this.height);
        }
    }

    public void onPressed(Minecraft mc) {
        if (mc.thePlayer == null) return;

        ItemStack mouseStack = mc.thePlayer.inventory.getItemStack();

        if (parentGui instanceof CuriosScreen) {
            GuiInventory vanillaInventory = new GuiInventory(mc.thePlayer);

            mc.thePlayer.inventory.setItemStack(null);
            mc.displayGuiScreen(vanillaInventory);
            mc.thePlayer.inventory.setItemStack(mouseStack);

            OKCurios.instance.getPacketHandler()
                .sendToServer(new CPacketOpenVanilla());

        } else if (parentGui instanceof GuiInventory || parentGui instanceof GuiContainerCreative) {
            mc.thePlayer.inventory.setItemStack(null);

            mc.thePlayer.openGui(
                OKCurios.instance,
                CuriosGuiHandler.CURIOS_GUI_ID,
                mc.theWorld,
                (int) mc.thePlayer.posX,
                (int) mc.thePlayer.posY,
                (int) mc.thePlayer.posZ);

            mc.thePlayer.inventory.setItemStack(mouseStack);

            OKCurios.instance.getPacketHandler()
                .sendToServer(new CPacketOpenCurios());
        }
    }
}
