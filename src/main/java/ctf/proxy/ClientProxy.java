package ctf.proxy;

import ctf.CTF;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		OBJLoader.INSTANCE.addDomain(CTF.MODID);
	}
	
	@Override
	public void init(FMLInitializationEvent event) {}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {}
	
}