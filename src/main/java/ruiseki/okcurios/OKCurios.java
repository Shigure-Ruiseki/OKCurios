package ruiseki.okcurios;

import java.util.Map;

import net.minecraft.command.ICommand;
import net.minecraft.creativetab.CreativeTabs;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Maps;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import ruiseki.okcore.command.CommandMod;
import ruiseki.okcore.init.ModBase;
import ruiseki.okcore.proxy.ICommonProxy;
import ruiseki.okcurios.common.capability.CurioInventoryCapability;
import ruiseki.okcurios.common.capability.CurioItemCapability;
import ruiseki.okcurios.common.inventory.container.CuriosGuiHandler;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    dependencies = Reference.DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class OKCurios extends ModBase {

    @SidedProxy(serverSide = Reference.PROXY_COMMON, clientSide = Reference.PROXY_CLIENT)
    public static ICommonProxy proxy;

    @Mod.Instance(Reference.MOD_ID)
    public static OKCurios instance;

    public OKCurios() {
        super(Reference.MOD_ID, Reference.MOD_NAME);
        putGenericReference(REFKEY_MOD_VERSION, Reference.VERSION);

        addInitListeners(new CurioInventoryCapability());
        addInitListeners(new CurioItemCapability());
    }

    @Override
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    protected CommandMod constructBaseCommand() {
        Map<String, ICommand> commands = Maps.newHashMap();
        CommandMod command = new CommandMod(this, commands);
        command.addAlias(Reference.MOD_ID);
        return command;
    }

    @Override
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        super.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new CuriosGuiHandler());
    }

    @Override
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        super.onServerStarting(event);

    }

    @Override
    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        super.onServerStarted(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
        super.onServerStopping(event);
    }

    @Override
    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        super.onServerStopped(event);
    }

    @Override
    public CreativeTabs constructDefaultCreativeTab() {
        return OKCreativeTab.INSTANCE;
    }

    @Override
    public ICommonProxy getProxy() {
        return proxy;
    }

    /**
     * Log a new info message for this mod.
     *
     * @param message The message to show.
     */
    public static void okLog(String message) {
        OKCurios.instance.log(Level.INFO, message);
    }

    /**
     * Log a new message of the given level for this mod.
     *
     * @param level   The level in which the message must be shown.
     * @param message The message to show.
     */
    public static void okLog(Level level, String message) {
        OKCurios.instance.log(level, message);
    }

    public static void okLog(Level level, String message, Object... params) {
        OKCurios.instance.log(level, message, params);
    }
}
