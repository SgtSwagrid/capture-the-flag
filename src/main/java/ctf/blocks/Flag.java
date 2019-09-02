package ctf.blocks;

import java.util.HashMap;
import java.util.Map;

import ctf.CTF;
import ctf.events.FlagEvent;
import ctf.util.Colour;
import ctf.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * Flags are used to score points during a flag event.
 * An instance of Flag represents a particular flag colour.
 * @author Alec Dorrington
 */
public class Flag extends Block {
	
	/** Contains the flag instance for each colour. */
	private static Map<Colour, Flag> flags = new HashMap<>();
	
	/** The colour of this flag instance. */
	public final Colour COLOUR;
	
	/** Flag collision box. */
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 2.0, 0.625);
	
	{
		//Flags can't be destroyed.
		setBlockUnbreakable();
		setHardness(10000000.0F);
		setResistance(10000000.0F);
	}
	
	/**
	 * Construct a new flag instance of the given colour.
	 * One such instance should be created for each colour available.
	 * @param colour the flag colour
	 */
	private Flag(Colour colour) {
		
		super(Material.BARRIER);
		
		String name = colour.UNLOCALISED_NAME + "_flag";
		setUnlocalizedName(name);
		
		ResourceLocation resource = new ResourceLocation(CTF.MODID, name);
		setRegistryName(resource);
		
		ForgeRegistries.BLOCKS.register(this);
		
		COLOUR = colour;
	}
	
	//Trigger flag interaction when a flag is left-clicked.
	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		
		//Only trigger interaction on the server side.
		if(!world.isRemote) {
			FlagEvent.interact(COLOUR, player, world, pos);
		}
	}
	
	//Trigger flag interaction when a flag is right-clicked.
	@Override
	public boolean onBlockActivated(World world, BlockPos pos,
			IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		
		//Only trigger interaction on the server side.
		if(!world.isRemote) {
			FlagEvent.interact(COLOUR, player, world, pos);
		}
		return true;
	}
	
	//Flags aren't full blocks.
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	
	//Flags should show their team colour on maps.
	@Override public MapColor getMapColor(IBlockState state,
			IBlockAccess world, BlockPos position) {
		return MapColor.getBlockColor(COLOUR.DYE_COLOUR);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AABB;
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return AABB;
    }
	
	/**
	 * Get the flag instance for flags of a particular colour.
	 * @param colour the colour of flag.
	 * @return the flag instance of this colour.
	 */
	public static Flag get(Colour colour) {
		return flags.get(colour);
	}
	
	/**
	 * Create a flag instance for each possible colour of flag.
	 * To be called during Forge pre-initialisation.
	 */
	public static void init() {
		
		for(Colour colour : Colour.values()) {
			flags.put(colour, new Flag(colour));
		}
	}
	
	/**
	 * Add a flag as close to the given position as possible.
	 * @param world the world in which to add the flag.
	 * @param position the desired flag position.
	 * @param colour the colour of flag to add.
	 * @return the actual position of the flag.
	 */
	public static BlockPos add(World world, BlockPos position, Colour colour) {
		
		BlockPos pos = WorldUtils.placeBlock(world, position, get(colour).getDefaultState());
		
		//If the flag is on water/lava, place a dirt block below it.
		if(world.getBlockState(pos.down()).getBlock() instanceof BlockLiquid) {
			world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState());
		}
		return pos;
	}
	
	/**
	 * Remove the flag at the given position.
	 * @param world the world from which to remove a flag.
	 * @param position the position of the flag.
	 */
	public static void remove(World world, BlockPos position) {
		
		//Get the block at the specified position.
		Block block = world.getBlockState(position).getBlock();
		
		if(block instanceof Flag) {
			
			Flag flag = (Flag) block;
			
			//Remove the block if it is a flag.
			world.setBlockState(position, Blocks.AIR.getDefaultState());
			
			//Remove other parts of the flag also.
			for(BlockPos pos : new BlockPos[] {position.up(), position.down()}) {
				
				//Remove adjacent flag blocks of the same colour.
				if(world.getBlockState(pos).getBlock() == flag) {
					remove(world, pos);
				}
			}
		}
	}
}