package ruiseki.okcurios.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import ruiseki.okcurios.common.inventory.CurioSlot;

public class RenderButton extends GuiButton {

    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final CurioSlot slot;
    private boolean mouseOver;

    public RenderButton(int id, CurioSlot slot, int x, int y, int width, int height, int xTexStart, int yTexStart,
        ResourceLocation resourceLocation) {
        super(id, x, y, width, height, "");
        this.slot = slot;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.mouseOver = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width
                && mouseY < this.yPosition + this.height;
        }
    }

    public void drawButtonOverlay(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            mc.getTextureManager()
                .bindTexture(this.resourceLocation);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            int textureX = this.xTexStart;

            if (this.slot != null && !this.slot.getRenderStatus()) {
                textureX += 8;
            }

            this.drawTexturedModalRect(
                this.xPosition,
                this.yPosition,
                textureX,
                this.yTexStart,
                this.width,
                this.height);

            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
    }

    public boolean isMouseOver() {
        return this.mouseOver;
    }
}
