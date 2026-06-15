package ruiseki.okcurios.common.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;
import ruiseki.okcurios.client.gui.CuriosScreen;

public class CuriosGuiHandler implements IGuiHandler {

    public static final int CURIOS_GUI_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CURIOS_GUI_ID) {
            return new CuriosContainer(player.inventory, world.isRemote, player);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == CURIOS_GUI_ID) {
            return new CuriosScreen(new CuriosContainer(player.inventory, world.isRemote, player), player);
        }
        return null;
    }
}
