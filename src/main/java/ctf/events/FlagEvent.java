package ctf.events;

import static ctf.util.Colour.*;
import static ctf.util.MathUtils.*;
import static ctf.util.Messenger.*;
import static ctf.util.Teams.*;
import static ctf.util.Values.*;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import ctf.blocks.Flag;
import ctf.util.Colour;
import ctf.util.Messenger;
import ctf.util.Values;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Controls the server side logic of the flag event.
 * @author Alec
 */
public class FlagEvent {
	
	/** The maximum number of captures per flag per team per flag event. */
	private static final int MAX_CAPTURES = 2;
	
	/** The score reward for each time you capture a flag. */
	private static final int CAPTURE_REWARD = 20;
	
	/** The score penalty for each time your flag is captured. */
	private static final int CAPTURE_PENALTY = 10;
	
	/** The maximum distance from spawn of the centre of the flags. */
	private static final int FURTHEST_CENTRE = 500;
	
	/** The minimum radius with which flags are distributed around the centre. */
	private static final int MIN_RADIUS = 100;
	
	/** The maximum radius with which flags are distributed around the centre. */
	private static final int MAX_RADIUS = 500;
	
	/**
	 * Prepare for a new flags event. Spawns the flags and tells each team where their flag is.
	 * Intended for use under the '/f purge' command.
	 */
	public static void prepare() {
		
		//Place the flags in the world.
		spawnFlags();
		
		for(Team team : getTeams()) {
			
			//Inform each team of the location of their own flag.
			Colour teamColour = fromFormatter(team.getColor());
			BlockPos pos = getPosition("ctf:flag_position(" + teamColour.UNLOCALISED_NAME + ")");
			tellTeam(team, "Your flag has been deployed at " + posStr(pos) + ".");
		}
		//Register and announce the flags as having been prepared.
		setBoolean("ctf:flags_prepared", true);
		announce("Capture the Flag will begin soon.");
	}
	
	/**
	 * Start the flags event. Enables capturing and announces the positions of all the flags.
	 * Intended for use under the '/f purge' command.
	 */
	public static void start() {
		
		for(Team team : getTeams()) {
			
			//Inform everyone of the position of each flag.
			Colour teamColour = fromFormatter(team.getColor());
			BlockPos pos = getPosition("ctf:flag_position(" + teamColour.UNLOCALISED_NAME + ")");
			announce("The &" + teamColour.DISPLAY_NAME + " Flag& has been discovered at "
					+ posStr(pos) + ".", teamColour);
		}
		//Register and announce the flags event as being active.
		setBoolean("ctf:flags_active", true);
		announce("Capture the Flag has begun.");
	}
	
	/**
	 * Stop the current flags event. Removes all flags from the world.
	 * Intended for use under the '/f purge' command.
	 */
	public static void stop() {
		
		//Remove all flags from the game.
		removeFlags();
		
		//Reset the capture counts for the next purge.
		for(Team team1 : getTeams()) {
			for(Team team2 : getTeams()) {
				if(!team1.isSameTeam(team2)) {
					
					String colour1 = fromFormatter(team1.getColor()).UNLOCALISED_NAME;
					String colour2 = fromFormatter(team2.getColor()).UNLOCALISED_NAME;
					Values.setInteger("ctf:num_captures(" + colour1 + "," + colour2 + ")", 0);
				}
			}
		}
		
		//Register and announce the flags event as having ended.
		setBoolean("ctf:flags_prepared", false);
		setBoolean("ctf:flags_active", false);
		announce("Capture the Flag has ended.");
	}
	
	/**
	 * Places a flag for each existing team in the overworld.
	 * Constructs a circle with a random centre position, radius and angle.
	 * Evenly places the flags in a random order around the circumference.
	 */
	private static void spawnFlags() {
		
		World world = DimensionManager.getWorld(0);
		BlockPos spawn = world.getSpawnPoint();
		
		//Get all the team colours.
		List<Colour> orderedTeams = getTeams().stream().map(t ->
				Colour.fromFormatter(t.getColor())).collect(toList());
		
		//Randomise the order of the teams.
		List<Colour> teams = new ArrayList<>();
		while(!orderedTeams.isEmpty()) {
			teams.add(orderedTeams.remove((int) (Math.random() * orderedTeams.size())));
		}
		
		//Determine a random centre within an acceptable range of spawn.
		int xc = spawn.getX() + randomRange(-FURTHEST_CENTRE, FURTHEST_CENTRE);
		int zc = spawn.getZ() + randomRange(-FURTHEST_CENTRE, FURTHEST_CENTRE);
		
		//Determine a random radius and starting angle.
		int radius = randomRange(MIN_RADIUS, MAX_RADIUS);
		float angle = randomRange(0.0F, 2.0F * (float) Math.PI);
		
		for(Colour team : teams) {
			
			//Calculate the position of this flag.
			int x = (int) (radius * Math.cos(angle)) + xc;
			int z = (int) (radius * Math.sin(angle)) + zc;
			
			//Place the flag in the world.
			spawnFlag(world, team, x, z);
			
			//Increment the angle for spawning the next flag.
			angle += 2.0F * (float) Math.PI / teams.size();
		}
	}
	
	/**
	 * Spawns the given colour flag in the given world at the given x, z position.
	 * @param world the world in which to spawn the flag.
	 * @param colour the colour of flag to spawn.
	 * @param x the preferred x position at which to place the flag.
	 * @param z the preferred z position at which to place the flag.
	 */
	private static void spawnFlag(World world, Colour colour, int x, int z) {
		
		//Place the flag in the world.
		BlockPos pos = Flag.add(world, new BlockPos(x, 255, z), colour);
		
		//Register the flag as having been placed where it is.
		setPosition("ctf:flag_home(" + colour.UNLOCALISED_NAME + ")", pos);
		setPosition("ctf:flag_position(" + colour.UNLOCALISED_NAME + ")", pos);
		setInteger("ctf:flag_dimension(" + colour.UNLOCALISED_NAME + ")", 0);
		setBoolean("ctf:flag_in_world(" + colour.UNLOCALISED_NAME + ")", true);
	}
	
	/**
	 * Remove all flags from the world and players.
	 * For use after a purge has ended.
	 */
	private static void removeFlags() {
		
		//Remove all flags from the world.
		for(Team team : getTeams()) {
			
			Colour colour = fromFormatter(team.getColor());
			
			//If this colour flag is placed in the world.
			if(getBoolean("ctf:flag_in_world(" + colour.UNLOCALISED_NAME + ")")) {
				
				//Remove the flag from the world.
				BlockPos position = getPosition("ctf:flag_position(" + colour.UNLOCALISED_NAME + ")");
				int dimension = getInteger("ctf:flag_dimension(" + colour.UNLOCALISED_NAME + ")");
				Flag.remove(DimensionManager.getWorld(dimension), position);
				setBoolean("ctf:flag_in_world(" + colour.UNLOCALISED_NAME + ")", false);
			}
		}
		
		//Remove all flags from players.
		World world = DimensionManager.getWorld(0);
		for(EntityPlayer player : world.getPlayers(EntityPlayer.class, p -> true)) {
			Values.setBoolean("ctf:has_flag(" + player.getName() + ")", false);
		}
	}
	
	/**
	 * Called by Flag on the server when the flag is left or right clicked.
	 * @param flagColour the colour of the flag.
	 * @param player the player who clicked it.
	 * @param world the world in which the flag exists.
	 * @param position the position of the flag in the world.
	 */
	public static void interact(Colour flagColour, EntityPlayer player,
			World world, BlockPos position) {
	    
	    //Ensure the player is on a team.
        if(player.getTeam() == null) {
            tellPlayer(player, "You must join a team to participate.");
        }
		
		Team flagTeam = getTeam(flagColour);
		Team playerTeam = player.getTeam();
		Colour playerColour = fromFormatter(playerTeam.getColor());
		    
		//When a player interacts with an enemy flag.
		if(flagColour != playerColour) {
			interactEnemy(flagColour, flagTeam, player, playerColour, playerTeam, world, position);
		
		//When a player interacts with a friendly flag.
		} else {
			interactFriendly(flagColour, flagTeam, player, world, position);
		}
	}
	
	/**
	 * Called when a player interacts with an enemy flag.
	 * @param flagColour the colour of the flag.
	 * @param flagTeam the team who owns the flag.
	 * @param player the player who interacts with the flag.
	 * @param playerColour the team colour of the player.
	 * @param playerTeam the team of the player.
	 * @param world the world in which the flag exists.
	 * @param position the position of the flag in the world.
	 */
	@SuppressWarnings("unused")
    private static void interactEnemy(Colour flagColour, Team flagTeam, EntityPlayer player,
			Colour playerColour, Team playerTeam, World world, BlockPos position) {
		
		//Flags can't be captured when no flag event is active.
		if(!getBoolean("ctf:flags_active")) {
			tellPlayer(player, "You can't pick up any flags before the event starts.");
			
		//Each player can only carry one flag at a time.
		} else if(getBoolean("ctf:has_flag(" + player.getName() + ")")) {
			tellPlayer(player, "You can't carry multiple flags at once.");
		
		//Each team can only capture each other flag a limited number of times.
		} else if(getInteger("ctf:num_captures(" + playerColour.UNLOCALISED_NAME + ","
				+ flagColour.UNLOCALISED_NAME + ")") >= MAX_CAPTURES) {
			
			tellPlayer(player, "Your team can't capture the same flag more than "
					+ MAX_CAPTURES + (MAX_CAPTURES == 1 ? " time." : " times."));
		
		//Pick up the enemy flag.
		} else {
			pickUp(flagColour, flagTeam, player, playerColour, playerTeam, world, position);
		}
	}
	
	/**
	 * Called to have a player pick up an enemy flag.
	 * @param flagColour the colour of the flag.
	 * @param flagTeam the team who owns the flag.
	 * @param player the player who interacts with the flag.
	 * @param playerColour the team colour of the player.
	 * @param playerTeam the team of the player.
	 * @param world the world in which the flag exists.
	 * @param position the position of the flag in the world.
	 */
	private static void pickUp(Colour flagColour, Team flagTeam, EntityPlayer player,
			Colour playerColour, Team playerTeam, World world, BlockPos position) {
		
		//Register the flag as having been picked up by the player.
		setEnum("ctf:held_flag(" + player.getName() + ")", flagColour);
		setBoolean("ctf:has_flag(" + player.getName() + ")", true);
		setBoolean("ctf:flag_in_world(" + flagColour.UNLOCALISED_NAME + ")", false);
		
		//Remove the flag from the world.
		Flag.remove(world, position);
		
		//Announce to everyone that the flag has been picked up.
		announce("&" + player.getName() + "& has picked up the &"
				+ flagColour.DISPLAY_NAME + " Flag&.", playerColour, flagColour);
	}
	
	/**
	 * Called when a player interacts with a friendly flag.
	 * @param colour the colour of the player and flag.
	 * @param team the team of the player and flag.
	 * @param player the player who interacts with the flag.
	 * @param world the world in which the flag exists.
	 * @param position the position of the flag in the world.
	 */
	private static void interactFriendly(Colour colour, Team team,
			EntityPlayer player, World world, BlockPos position) {
		
		//Players must already be carrying an enemy flag to capture a flag.
		if(!getBoolean("ctf:has_flag(" + player.getName() + ")")) {
			tellPlayer(player, "You can't pick up your own flag.");
			
		//Flags can't be captured when no event is active.
		} else if(!getBoolean("ctf:flags_active")) {
			tellPlayer(player, "You can't capture any flags after the event has ended.");
			
		//Capture an enemy flag.
		} else {
			
			//Get the properties of the flag to-be-captured.
			Colour capturedColour = getEnum("ctf:held_flag(" + player.getName() + ")", Colour.class);
			Team capturedTeam = getTeam(capturedColour);
			
			//Have the player capture the given flag.
			capture(colour, team, player, capturedColour, capturedTeam, world, position);
		}
	}
	
	/**
	 * Called to have a player capture an enemy flag.
	 * @param colour the colour of the player.
	 * @param team the team of the player.
	 * @param player the player who interacts with the flag.
	 * @param capturedColour the colour of the flag to be captured.
	 * @param capturedTeam the team of the flag to be captured.
	 * @param world the world in which the friendly flag exists.
	 * @param position the position of the friendly flag in the world.
	 */
	private static void capture(Colour colour, Team team, EntityPlayer player,
			Colour capturedColour, Team capturedTeam, World world, BlockPos position) {
		
		//Get the home position of the captured flag.
		BlockPos capturedHome = getPosition("ctf:flag_home(" + capturedColour.UNLOCALISED_NAME + ")");
		
		//Return the captured flag to its original position.
		BlockPos returnPos = Flag.add(world, capturedHome, capturedColour);
		
		//Register the flag as having been returned to its original position.
		setBoolean("ctf:has_flag(" + player.getName() + ")", false);
		setBoolean("ctf:flag_in_world(" + capturedColour.UNLOCALISED_NAME + ")", true);
		setPosition("ctf:flag_position(" + capturedColour.UNLOCALISED_NAME + ")", returnPos);
		setInteger("ctf:flag_dimension(" + capturedColour.UNLOCALISED_NAME + ")", 0);
		
		//Get the number of times this team has captured this particular flag, increasing it by 1.
		int captures = increment("ctf:num_captures(" + colour.UNLOCALISED_NAME
				+ "," + capturedColour.UNLOCALISED_NAME + ")", 1);
		
		//Announce to everyone that the flag has been captured and returned.
		
		announce("&" + player.getName() + "& has captured the &" + capturedColour.DISPLAY_NAME
				+ " Flag& (" + captures + "/" + MAX_CAPTURES + ").", colour, capturedColour);
		
		announce("The &" + capturedColour.DISPLAY_NAME + " Flag& has been returned to &"
				+ posStr(returnPos) + "&.", capturedColour, CYAN);
		
		tellTeam(team, "Your team has been awarded &" + CAPTURE_REWARD + "& points.", WHITE);
		tellTeam(capturedTeam, "Your team has lost &" + CAPTURE_PENALTY + "& points.", WHITE);
		
		//Award points accordingly.
		givePoints(team, CAPTURE_REWARD);
		givePoints(capturedTeam, -CAPTURE_PENALTY);
	}
	
	/** Players drop their flags when they log out. */
	@SubscribeEvent
	public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		dropFlag(event.player);
	}
	
	/** Players drop their flags when they die. */
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			dropFlag((EntityPlayer) event.getEntityLiving());
		}
	}
	
	/**
	 * Causes a given player to drop their flag, if they have one.
	 * Does nothing if the player doesn't have a flag.
	 * @param player the player to drop their flag.
	 */
	private void dropFlag(EntityPlayer player) {
		
		//If the player has a flag.
		if(getBoolean("ctf:has_flag(" + player.getName() + ")")) {
			
			//Get the colour of the flag.
			Colour flagColour = getEnum("ctf:held_flag(" + player.getName() + ")", Colour.class);
			
			//Place the flag in the world.
			BlockPos position = Flag.add(player.getEntityWorld(), player.getPosition(), flagColour);
			
			//Register the flag as having been dropped.
			setBoolean("ctf:has_flag(" + player.getName() + ")", false);
			setBoolean("ctf:flag_in_world(" + flagColour.UNLOCALISED_NAME + ")", true);
			setPosition("ctf:flag_position(" + flagColour.UNLOCALISED_NAME + ")", position);
			setInteger("ctf:flag_dimension(" + flagColour.UNLOCALISED_NAME + ")", player.dimension);
			
			//Get the team colour of the player who dropped the flag.
			Colour playerColour = fromFormatter(player.getTeam().getColor());
			
			//Announce to everyone that the flag has been dropped.
			announce("&" + player.getName() + "& has dropped the &" + flagColour.DISPLAY_NAME + " Flag& at "
					+ posStr(position, player.dimension) + ".", playerColour, flagColour);
		}
	}
	
	/** Players will be sent flag locations upon logging in during a purge. */
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		
		//Show all flag locations if the purge is active.
		if(getBoolean("ctf:flags_active")) {
			
			for(Team team : getTeams()) {
				
				Colour colour = fromFormatter(team.getColor());
				
				//Show flags which exist physically in the world.
				if(getBoolean("ctf:flag_in_world(" + colour.UNLOCALISED_NAME + ")")) {
					
					BlockPos position = getPosition("ctf:flag_position(" + colour.UNLOCALISED_NAME + ")");
					int dimension = getInteger("ctf:flag_dimension(" + colour.UNLOCALISED_NAME + ")");
					Messenger.tellPlayer(event.player, "The &" + colour.DISPLAY_NAME
							+ " Flag& is located at " + posStr(position, dimension) + ".", colour);
				}
			}
			
			//Show flags which are currently held by a player.
			for(WorldServer world : DimensionManager.getWorlds()) {
				
				//For each player holding a flag.
				for(EntityPlayer player : world.getPlayers(EntityPlayer.class,
						p -> getBoolean("ctf:has_flag(" + p.getName() + ")"))) {
					
					Colour flagColour = getEnum("ctf:held_flag(" + player + ")", Colour.class);
					Colour playerColour = fromFormatter(player.getTeam().getColor());
					
					//Show the player's position and held flag.
					Messenger.tellPlayer(event.player, "The &" + flagColour.UNLOCALISED_NAME
							+ " Flag& is held by &" + player + "& at " + posStr(
							player.getPosition(), player.dimension) + ".", flagColour, playerColour);
				}
			}
			
		//Show only the location of the player's own flag if the purge is in preparation.
		} else if(getBoolean("ctf:flags_prepared") && event.player.getTeam() != null) {
			
			Colour colour = fromFormatter(event.player.getTeam().getColor());
			BlockPos position = getPosition("ctf:flag_position(" + colour.UNLOCALISED_NAME + ")");
			Messenger.tellPlayer(event.player, "Your flag is located at " + posStr(position) + ".");
		}
	}
}