package ctf.commands;

import ctf.events.FlagEvent;
import ctf.util.Values;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Command for cycling the state of Capture the Flag.
 * Inactive -> Preparation -> Active
 * @author Alec Dorrington
 */
public class CTFCommand extends CommandBase {

	@Override
	public String getName() { return "ctf"; }

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.ctf.usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
		
	    //Inactive -> Preparation
	    if(!Values.getBoolean("ctf:flags_prepared")) {
            FlagEvent.prepare();
            
        //Preparation -> Active
        } else if(!Values.getBoolean("ctf:flags_active")) {
            FlagEvent.start();
            
        //Active -> Inactive
        } else {
            FlagEvent.stop();
        }
	}
	
	@Override
	public int getRequiredPermissionLevel() { return 2; }
}