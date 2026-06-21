package ruiseki.okcurios.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.Reference;
import ruiseki.okcurios.common.inventory.CosmeticCurioSlot;
import ruiseki.okcurios.common.inventory.CurioSlot;
import ruiseki.okcurios.common.inventory.container.CuriosContainer;
import ruiseki.okcurios.common.network.PacketScroll;

public class CuriosScreen extends GuiContainer {

    public static final ResourceLocation CURIO_INVENTORY = new ResourceLocation(
        Reference.MOD_ID,
        "textures/gui/inventory.png");
    private static final ResourceLocation INVENTORY_BACKGROUND = new ResourceLocation(
        "textures/gui/container/inventory.png");
    private static final ResourceLocation CREATIVE_INVENTORY_TABS = new ResourceLocation(
        "textures/gui/container/creative_inventory/tabs.png");

    private static float currentScroll = 0.0F;
    public boolean hasScrollBar;
    private boolean isScrolling;
    private boolean isRenderButtonHovered;

    private final EntityPlayer player;

    public CuriosScreen(Container container, EntityPlayer player) {
        super(container);
        this.player = player;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        CuriosContainer container = (CuriosContainer) this.inventorySlots;
        if (container.curiosHandler != null) {
            this.hasScrollBar = container.curiosHandler.getVisibleSlots() > 8;
            if (this.hasScrollBar) {
                container.scrollTo(currentScroll);
            }
        }

        this.updateRenderButtons();
    }

    public void updateRenderButtons() {
        this.buttonList.removeIf(button -> button instanceof RenderButton);

        CuriosContainer container = (CuriosContainer) this.inventorySlots;
        int idCounter = 1;

        for (Slot slot : container.inventorySlots) {

            if (slot instanceof CurioSlot && !(slot instanceof CosmeticCurioSlot)) {
                int buttonX = this.guiLeft + slot.xDisplayPosition + 11;
                int buttonY = this.guiTop + slot.yDisplayPosition - 3;

                this.buttonList.add(
                    new RenderButton(idCounter++, (CurioSlot) slot, buttonX, buttonY, 8, 8, 75, 0, CURIO_INVENTORY));
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    private boolean inScrollBar(int mouseX, int mouseY) {
        int k = this.guiLeft - 34;
        int l = this.guiTop + 12;
        int i1 = k + 14;
        int j1 = l + 139;

        CuriosContainer container = (CuriosContainer) this.inventorySlots;
        if (container.hasCosmeticColumn()) {
            i1 -= 19;
            k -= 19;
        }
        return mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        boolean isHovered = false;
        for (Object obj : this.buttonList) {
            if (obj instanceof RenderButton button) {
                button.drawButtonOverlay(this.mc, mouseX, mouseY);
                if (button.isMouseOver()) {
                    isHovered = true;
                }
            }
        }
        this.isRenderButtonHovered = isHovered;

        if (this.player.inventory.getItemStack() == null) {
            Slot slot = this.getSlotAtPosition(mouseX, mouseY);

            if (this.isRenderButtonHovered) {
                this.drawCreativeTabHoveringText(LangHelpers.localize("gui.curios.toggle"), mouseX, mouseY);
            } else if (slot instanceof CurioSlot slotCurio && !slot.getHasStack()) {
                this.drawCreativeTabHoveringText(slotCurio.getSlotName(), mouseX, mouseY);
            }
        }
    }

    private Slot getSlotAtPosition(int x, int y) {
        for (int i = 0; i < this.inventorySlots.inventorySlots.size(); ++i) {
            Slot slot = (Slot) this.inventorySlots.inventorySlots.get(i);
            if (this.isMouseOverSlot(slot, x, y)) {
                return slot;
            }
        }
        return null;
    }

    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return this.func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String titleStr = LangHelpers.localize("container.crafting");
        this.fontRendererObj.drawString(titleStr, 97, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager()
            .bindTexture(INVENTORY_BACKGROUND);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        GuiInventory.func_147046_a(
            i + 51,
            j + 75,
            30,
            (float) (i + 51) - mouseX,
            (float) (j + 75 - 50) - mouseY,
            this.mc.thePlayer);

        CuriosContainer container = (CuriosContainer) this.inventorySlots;

        if (container.curiosHandler == null) return;

        int slotCount = container.curiosHandler.getVisibleSlots();

        if (slotCount > 0) {
            this.mc.getTextureManager()
                .bindTexture(CURIO_INVENTORY);

            int xOffset = -26;
            int width = 27;
            int xTexOffset = 0;

            if (container.hasCosmeticColumn()) {
                xTexOffset = 92;
                width = 46;
                xOffset -= 19;
            }

            int renderCount = Math.min(slotCount, 8);
            int upperHeight = 7 + renderCount * 18;

            this.drawTexturedModalRect(i + xOffset, j + 4, xTexOffset, 0, width, upperHeight);

            if (slotCount <= 8) {
                this.drawTexturedModalRect(i + xOffset, j + 4 + upperHeight, xTexOffset, 151, width, 7);
            } else {
                this.drawTexturedModalRect(i + xOffset - 16, j + 4, 27, 0, 23, 158);
                this.mc.getTextureManager()
                    .bindTexture(CREATIVE_INVENTORY_TABS);
                this.drawTexturedModalRect(i + xOffset - 8, j + 12 + (int) (127.0F * currentScroll), 232, 0, 12, 15);
            }

            this.mc.getTextureManager()
                .bindTexture(CURIO_INVENTORY);
            for (Slot slot : container.inventorySlots) {
                if (slot instanceof CurioSlot || slot instanceof CosmeticCurioSlot) {
                    int slotX = this.guiLeft + slot.xDisplayPosition - 1;
                    int slotY = this.guiTop + slot.yDisplayPosition - 1;

                    this.drawTexturedModalRect(slotX, slotY, 138, 0, 18, 18);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.inScrollBar(mouseX, mouseY)) {
            this.isScrolling = ((CuriosContainer) this.inventorySlots).canScroll();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (state == 0) {
            this.isScrolling = false;
        }
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.isScrolling) {
            int i = this.guiTop + 8;
            int j = i + 148;
            currentScroll = ((float) mouseY - i - 7.5F) / (j - i - 15.0F);
            currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);

            CuriosContainer container = (CuriosContainer) this.inventorySlots;
            container.scrollToIndex((int) (currentScroll * (container.curiosHandler.getVisibleSlots() - 8) + 0.5D));

            OKCurios.instance.getPacketHandler()
                .sendToServer(new PacketScroll(container.windowId, container.lastScrollIndex));
            return;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            CuriosContainer container = (CuriosContainer) this.inventorySlots;
            if (container.canScroll()) {
                int slots = container.curiosHandler.getVisibleSlots();
                currentScroll = (float) (currentScroll - (Integer.signum(wheel) / (float) slots));
                currentScroll = MathHelper.clamp_float(currentScroll, 0.0F, 1.0F);
                int index = (int) (currentScroll * (slots - 8) + 0.5D);
                container.scrollToIndex(index);
                OKCurios.instance.getPacketHandler()
                    .sendToServer(new PacketScroll(container.windowId, index));
            }
        }
    }
}
