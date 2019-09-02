package ctf.util;

import java.util.EnumSet;
import java.util.LinkedList;

import ctf.CTF;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;

/**
 * Helper class used to save arbitrary persistent values to the world file.
 * Values will be instantly shared across all devices connected to the world.
 * This is done by use of the Minecraft scoreboard.
 * @author Alec
 */
public class Values {
	
	/** The world scoreboard. */
	private static final Scoreboard SCOREBOARD =
			DimensionManager.getWorld(0).getScoreboard();
	
	/** The data objective. Used to store all the values. */
	private static final ScoreObjective DATA =
			getObjective(CTF.MODID + "_data");
	
	/**
	 * Set an integer value. There is no need for the value to already exist.
	 * @param name the name of the value entry.
	 * @param value the new value.
	 */
	public static void setInteger(String name, int value) {
		name = name.toLowerCase().replace(" ", "_");
		Score score = SCOREBOARD.getOrCreateScore(name, DATA);
		score.setScorePoints(value);
	}
	
	/**
	 * Set a boolean value. There is no need for the value to already exist.
	 * @param name the name of the value entry.
	 * @param value the new value.
	 */
	public static void setBoolean(String name, boolean value) {
		//Booleans are stored as a 0 or 1 integer value.
		setInteger(name, value ? 1 : 0);
	}
	
	/**
	 * Set a position value. There is no need for the value to already exist.
	 * @param name the name of the value entry.
	 * @param position the new value.
	 */
	public static void setPosition(String name, BlockPos position) {
		//Positions are stored as 3 separate integers.
		setInteger(name + "[x]", position.getX());
		setInteger(name + "[y]", position.getY());
		setInteger(name + "[z]", position.getZ());
	}
	
	/**
	 * Set an enum value. There is no need for the value to already exist.
	 * @param name the name of the value entry.
	 * @param value the new value.
	 */
	public static void setEnum(String name, Enum<?> value) {
		//Enums are stored as a single integer representing the ordinal.
		setInteger(name, value.ordinal());
	}
	
	/**
	 * Returns an integer value. The default value is 0 for values yet to exist.
	 * @param name the name of the value entry.
	 * @return the integer value.
	 */
	public static int getInteger(String name) {
		name = name.toLowerCase().replace(" ", "_");
		return SCOREBOARD.getOrCreateScore(name, DATA).getScorePoints();
	}
	
	/**
	 * Returns a boolean value. The default value is false for values yet to exist.
	 * @param name the name of the value entry.
	 * @return the boolean value.
	 */
	public static boolean getBoolean(String name) {
		return getInteger(name) == 1;
	}
	
	/**
	 * Returns a position value. The default value is (0, 0, 0) for values yet to exist.
	 * @param name the name of the value entry.
	 * @return the position value.
	 */
	public static BlockPos getPosition(String name) {
		return new BlockPos(getInteger(name + "[x]"),
				getInteger(name + "[y]"),
				getInteger(name + "[z]"));
	}
	
	/**
	 * Returns an enum value. The default value is the first enum entry for values yet to exist.
	 * @param name the name of the value entry.
	 * @param e the enum type used to interpret the value.
	 * @return the enum value.
	 */
	public static <T extends Enum<T>> T getEnum(String name, Class<T> e) {
		return new LinkedList<>(EnumSet.allOf(e)).get(getInteger(name));
	}
	
	/**
	 * Increments an integer value by the specified amount. Starts at 0 for values yet to exist.
	 * @param name the name of the value entry.
	 * @param amount the amount by which to increment.
	 * @return the new value stored under the given name.
	 */
	public static int increment(String name, int amount) {
		setInteger(name, getInteger(name) + amount);
		return getInteger(name);
	}
	
	/**
	 * Get the scoreboard objective of the given name.
	 * Will create a new objective if it doesn't yet exist.
	 * @param name the name of the objective to find/create.
	 * @return the objective instance.
	 */
	private static ScoreObjective getObjective(String name) {
		ScoreObjective objective = SCOREBOARD.getObjective(name);
		return objective != null ? objective :
				SCOREBOARD.addScoreObjective(name, IScoreCriteria.DUMMY);
	}
}