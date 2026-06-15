package ruiseki.okcurios.client.gui;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiScreenEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEventHandler {

    private static final int CURIOS_BUTTON_ID = 157;
    private static Method isNEIHiddenMethod;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onInventoryGuiInit(GuiScreenEvent.InitGuiEvent.Post evt) {
        GuiScreen screen = evt.gui;

        if (screen instanceof GuiInventory || screen instanceof GuiContainerCreative
            || screen instanceof CuriosScreen) {
            boolean isCreative = screen instanceof GuiContainerCreative;

            int xSize = 176;
            int ySize = 166;

            int guiLeft = (screen.width - xSize) / 2;
            int guiTop = (screen.height - ySize) / 2;

            if (screen.mc.thePlayer != null && !screen.mc.thePlayer.getActivePotionEffects()
                .isEmpty() && isNeiHidden()) {
                guiLeft = 160 + (screen.width - xSize - 200) / 2;
            }

            int size = isCreative ? 10 : 14;
            int textureOffsetX = isCreative ? 64 : 50;

            int buttonX = guiLeft + (isCreative ? 26 : 80);
            int buttonY = guiTop + (isCreative ? 68 : 83);

            CuriosButton curiosButton = new CuriosButton(
                CURIOS_BUTTON_ID,
                (net.minecraft.client.gui.inventory.GuiContainer) screen,
                buttonX,
                buttonY,
                size,
                size,
                textureOffsetX,
                0,
                CuriosScreen.CURIO_INVENTORY);

            evt.buttonList.add(curiosButton);
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Post evt) {
        if (evt.button != null && evt.button.id == CURIOS_BUTTON_ID && evt.button instanceof CuriosButton) {
            ((CuriosButton) evt.button).onPressed(Minecraft.getMinecraft());

            Minecraft.getMinecraft()
                .getSoundHandler()
                .playSound(
                    net.minecraft.client.audio.PositionedSoundRecord
                        .func_147674_a(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
        }
    }

    private boolean isNeiHidden() {
        boolean hidden = true;
        try {
            if (isNEIHiddenMethod == null) {
                Class<?> neiConfigClass = Class.forName("codechicken.nei.NEIClientConfig");
                isNEIHiddenMethod = neiConfigClass.getMethod("isHidden");
            }
            hidden = (Boolean) isNEIHiddenMethod.invoke(null);
        } catch (Exception ignored) {}
        return hidden;
    }
}
