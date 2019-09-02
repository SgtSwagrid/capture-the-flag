package ctf.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

/**
 * Wrapper class for sending chat messages.
 * @author Alec
 */
public class Messenger {
	
	/*
	 * COLOURING:
	 * 
	 * The text contained between each '&' pair will receive custom colouring,
	 * with all other text using the default colour.
	 * 
	 * The number of colours provided should match the number of '&' pairs,
	 * which each specified colour referring to its own '&' pair, in order.
	 * 
	 * Note that the included '&' symbols will not themselves be shown in text.
	 */
	
	/** The primary text colour. */
	private static Colour DEFAULT_COLOUR = Colour.YELLOW;
	
	/** The symbol used to signify a change in text colour. */
	private static String DELIMITER = "&";
	
	/**
	 * Send a chat message to a specific player.
	 * @param player the player to send a message to.
	 * @param message the message to send.
	 * @param colours the colour(s) of the message.
	 */
	public static void tellPlayer(EntityPlayer player, String message, Colour... colours) {
		player.sendMessage(format(message, colours));
	}
	
	/**
	 * Send a chat message to every player in a team.
	 * @param team the team to send a message to.
	 * @param message the message to send.
	 * @param colours the colour(s) of the message.
	 */
	public static void tellTeam(Team team, String message, Colour... colours) {
		for(WorldServer world : DimensionManager.getWorlds()) {
			world.getPlayers(EntityPlayer.class,
					p -> p.isOnScoreboardTeam(team))
			.forEach(p -> p.sendMessage(format(message, colours)));
		}
	}
	
	/**
	 * Send a chat message to every online player.
	 * @param message the message to send.
	 * @param colours the colour(s) of the message.
	 */
	public static void announce(String message, Colour... colours) {
		for(WorldServer world : DimensionManager.getWorlds()) {
			world.getPlayers(EntityPlayer.class, p -> true).forEach(
					p -> p.sendMessage(format(message, colours)));
		}
	}
	
	/**
	 * Provides a human-readable string representation of a BlockPos.
	 * The string format is compatible with JourneyMap.
	 * @param position the position to format.
	 * @return a string representing the position.
	 */
	public static String posStr(BlockPos position) {
		return TextFormatting.AQUA + "[X:" + position.getX() + ", Y:"+ position.getY()
				+ ", Z:" + position.getZ() + "]" + DEFAULT_COLOUR.FORMATTER;
	}
	
	/**
	 * Provides a human-readable string representation of a BlockPos.
	 * The string format is compatible with JourneyMap.
	 * @param position the position to format.
	 * @param dimension the dimension in which the position resides.
	 * @return a string representing the position.
	 */
	public static String posStr(BlockPos position, int dimension) {
		//Only include the dimension if it isn't the overworld.
		if(dimension == 0) {
			return posStr(position);
		} else {
			return TextFormatting.AQUA + "[X:" + position.getX() + ", Y:" + position.getY()
					+ ", Z:" + position.getZ() + ", DIM:" + dimension + "]" + DEFAULT_COLOUR.FORMATTER;
		}
	}
	
	/**
	 * Formats a string to include a particular colour scheme.
	 * @param rawText the uncoloured text.
	 * @param colours the list of colours to use.
	 * @return the coloured string.
	 */
	private static TextComponentString format(String rawText, Colour... colours) {
		
		String text = "";
		
		int i = 0;
		for(String part : rawText.split(DELIMITER, -1)) {
			
			//For every second delimiter, reset the colour.
			if(i % 2 == 0) {
				text += DEFAULT_COLOUR.FORMATTER.toString();
				
			//For every other delimiter, apply the next colour in the array.
			} else {
				text += colours[i / 2].FORMATTER;
			}
			text += part;
			i++;
		}
		return new TextComponentString(text + TextFormatting.RESET);
	}
}