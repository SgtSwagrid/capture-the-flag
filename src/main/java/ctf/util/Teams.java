package ctf.util;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.DimensionManager;

/**
 * Wrapper class for Minecraft teams.
 * @author Alec Dorrington
 */
public class Teams {
	
	/** The world scoreboard. */
	private static final Scoreboard SCOREBOARD =
			DimensionManager.getWorld(0).getScoreboard();
	
	/** The score objective. */
	private static final ScoreObjective SCORE = getObjective("Score");
	
	static {
		//Display the score in the sidebar.
		SCOREBOARD.setObjectiveInDisplaySlot(1, SCORE);
	}
	
	/**
	 * Give/remove points to/from a specified team.
	 * @param team the team to give points to.
	 * @param points the number of points to add/take.
	 */
	public static void givePoints(Team team, int points) {
		String name = team.getColor() + team.getName() + TextFormatting.RESET;
		ScoreObjective score = SCOREBOARD.getObjectiveInDisplaySlot(1);
		SCOREBOARD.getOrCreateScore(name, score).increaseScore(points);
	}
	
	/**
	 * Finds the team of the given colour.
	 * @param colour the colour of team to find.
	 * @return the matching team.
	 */
	public static Team getTeam(Colour colour) {
		for(Team team : getTeams()) {
			if(team.getColor().equals(colour.FORMATTER)) {
				return team;
			}
		}
		return null;
	}
	
	/**
	 * @return a list of all the current teams.
	 */
	public static Collection<ScorePlayerTeam> getTeams() {
		return SCOREBOARD.getTeams();
	}
	
	/**
	 * Determines whether a player is on the team of a particular colour.
	 * @param player the player to check.
	 * @param colour the colour of the team.
	 * @return whether the player is on the team.
	 */
	public static boolean isPlayerOnTeam(EntityPlayer player, Colour colour) {
		return player.getTeam().getColor().equals(colour.FORMATTER);
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