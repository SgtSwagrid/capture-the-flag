package ctf.util;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.text.TextFormatting;

/**
 * Represents the available Minecraft colours.
 * Used to unify the dye and text colour systems.
 * @author Alec
 */
public enum Colour {
	
	//List of available colours.
	WHITE("White", EnumDyeColor.WHITE, TextFormatting.WHITE),
	ORANGE("Orange", EnumDyeColor.ORANGE, TextFormatting.GOLD),
	MAGENTA("Magenta", EnumDyeColor.MAGENTA, TextFormatting.DARK_PURPLE),
	LIGHT_BLUE("Light Blue", EnumDyeColor.LIGHT_BLUE, TextFormatting.AQUA),
	YELLOW("Yellow", EnumDyeColor.YELLOW, TextFormatting.YELLOW),
	GREEN("Green", EnumDyeColor.LIME, TextFormatting.GREEN),
	PINK("Pink", EnumDyeColor.PINK, TextFormatting.LIGHT_PURPLE),
	GRAY("Gray", EnumDyeColor.GRAY, TextFormatting.DARK_GRAY),
	LIGHT_GRAY("Light Gray", EnumDyeColor.SILVER, TextFormatting.GRAY),
	CYAN("Cyan", EnumDyeColor.CYAN, TextFormatting.DARK_AQUA),
	PURPLE("Purple", EnumDyeColor.PURPLE, TextFormatting.DARK_RED),
	BLUE("Blue", EnumDyeColor.BLUE, TextFormatting.BLUE),
	BROWN("Brown", EnumDyeColor.BROWN, TextFormatting.DARK_BLUE),
	DARK_GREEN("Dark Green", EnumDyeColor.GREEN, TextFormatting.DARK_GREEN),
	RED("Red", EnumDyeColor.RED, TextFormatting.RED),
	BLACK("Black", EnumDyeColor.BLACK, TextFormatting.BLACK);
	
	/** The display name of this colour. */
	public final String DISPLAY_NAME;
	
	/** The unlocalised name of this colour. Uses underscores instead of spaces. */
	public final String UNLOCALISED_NAME;
	
	/** The dye colour matching this colour. */
	public final EnumDyeColor DYE_COLOUR;
	
	/** The text formatter matching this colour. */
	public final TextFormatting FORMATTER;
	
	/**
	 * @param name the display name of the colour.
	 * @param dyeColour the matching dye colour.
	 * @param formatter the matching text formatter.
	 */
	Colour(String name, EnumDyeColor dyeColour, TextFormatting formatter) {
		DISPLAY_NAME = name;
		UNLOCALISED_NAME = name.replace(" ", "_").toLowerCase();
		DYE_COLOUR = dyeColour;
		FORMATTER = formatter;
	}
	
	/**
	 * Find the colour matching a given dye colour.
	 * @param dyeColour the dye colour of which to find the colour.
	 * @return the matching colour.
	 */
	public static Colour fromDye(EnumDyeColor dyeColour) {
		for(Colour colour : values()) {
			if(colour.DYE_COLOUR == dyeColour) {
				return colour;
			}
		}
		return null;
	}
	
	/**
	 * Find the colour matching a given text formatter.
	 * @param formatter the text formatter of which to find the colour.
	 * @return the matching colour.
	 */
	public static Colour fromFormatter(TextFormatting formatter) {
		for(Colour colour : values()) {
			if(colour.FORMATTER == formatter) {
				return colour;
			}
		}
		return null;
	}
	
	/**
	 * Find a colour by name.
	 * @param name the unlocalised or display name of the colour.
	 * @return the matching colour.
	 */
	public static Colour fromName(String name) {
		for(Colour colour : values()) {
			if(colour.DISPLAY_NAME.equals(name) ||
					colour.UNLOCALISED_NAME.equals(name)) {
				return colour;
			}
		}
		return null;
	}
	
	public String toString() { return DISPLAY_NAME; }
}