package ctf;

import ctf.blocks.Flag;
import ctf.commands.CTFCommand;
import ctf.events.FlagEvent;
import ctf.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

/**
 * Minecraft capture the flag implementation.
 * @author Alec Dorrington
 */
@Mod(modid = CTF.MODID, version = CTF.VERSION)
public class CTF {
	
	/**  */
	public static final String MODID = "ctf";
	
	/**  */
	public static final String VERSION = "1.12.2-0.3.0";
	
	/**  */
	@SidedProxy(clientSide = "ctf.proxy.ClientProxy",
			    serverSide = "ctf.proxy.CommonProxy")
	public static CommonProxy proxy;
	
	/**  */
	@Mod.Instance
	public static CTF INSTANCE;
	
	/**  */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Flag.init();
		proxy.preInit(event);
	}
	
	/**  */
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}
	
	/**  */
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new FlagEvent());
		proxy.postInit(event);
	}
	
	/**  */
	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		event.registerServerCommand(new CTFCommand());
	}
}