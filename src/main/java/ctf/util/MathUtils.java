package ctf.util;

/**
 * Class for storing mathematical utilities.
 * @author Alec
 */
public class MathUtils {
	
	/**
	 * Chooses an integer in the given range (inclusive) at random.
	 * @param min the minimum value.
	 * @param max the maximum value.
	 * @return a random value in the range.
	 */
	public static int randomRange(int min, int max) {
		return (int) (Math.random() * (max - min) + min);
	}
	
	/**
	 * Chooses a float in the given range (inclusive) at random.
	 * @param min the minimum value.
	 * @param max the maximum value.
	 * @return a random value in the range.
	 */
	public static float randomRange(float min, float max) {
		return (float) (Math.random() * (max - min) + min);
	}
}