package ctf.util;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Utilities for world manipulation.
 * @author Alec Dorrington
 */
public class WorldUtils {
	
	/**
	 * Places the given block state as close to the given position as possible.
	 * The block will always be placed on solid ground.
	 * @param world the world in which to place the block.
	 * @param position the desired position at which to place the block.
	 * @param block the block state to place.
	 * @return the actual position at which the block was placed.
	 */
	public static BlockPos placeBlock(World world,
			BlockPos position, IBlockState block) {
		
		//Increase search radius until a free block is found.
		outer: for(int r = 0;; r++) {
			
			//For each x value inside the current search radius.
			for(int x = -r; x <= r; x++) {
				
				//For each z value inside the current search radius.
				for(int z = x - r; z <= r - x; z++) {
					
					//For each y value on the edge of the current search radius.
					for(int i = 0; i < (x + z == r ? 1 : 2); i++) {
						
						//Determine the appropriate y value.
						int y = r - x - z;
						y *= i == 0 ? -1 : 1;
						
						//If this location is free.
						if(y >= 0 && y < world.getHeight() &&
								world.getBlockState(position.add(x, y, z))
								.getBlock().isReplaceable(world, position)) {
							
							//Stop searching once a free block is found.
							position = position.add(x, y, z);
							break outer;
						}
					}
				}
			}
		}
		//Find solid ground at or below this position.
		position = findSurface(world, position);
		world.setBlockState(position, block);
		return position;
	}
	
	/**
	 * Returns the maximum world height at the position (x, z).
	 * @param world the world of which to get the height of.
	 * @param x the x coordinate.
	 * @param z the z coordinate.
	 * @return the y coordinate of the highest block at this location.
	 */
	public static int getHeight(World world, int x, int z) {
		return findSurface(world, new BlockPos(x, 255, z)).getY() - 1;
	}
	
	/**
	 * Returns the position equal to or directly below the given one that is on solid ground.
	 * @param world the world in which to search.
	 * @param position the position from which to find solid ground.
	 * @return the position as described above.
	 */
	public static BlockPos findSurface(World world, BlockPos position) {
		
		//Shift the position down until solid ground is found.
		while(world.getBlockState(position.down()).getBlock().isReplaceable(world, position) &&
				!(world.getBlockState(position).getBlock() instanceof BlockLiquid)) {
			position = position.down();
		}
		return position.up();
	}
}