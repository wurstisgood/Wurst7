/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import net.minecraft.client.util.TextFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

public final class HologramCmd extends Command implements PacketOutputListener
{
	private int slot = -1;
	private int index;
	private boolean shouldClear;
	private String[] colorCodes;
	private BlockPos standPos;
	private static final Map<TextFormat, Color> colorsMap = new HashMap<>();
	private static final Map<TextFormat, Color> graysMap = new HashMap<>();

	static
	{
		colorsMap.put(TextFormat.DARK_BLUE, new Color(0, 0, 170));
		colorsMap.put(TextFormat.DARK_GREEN, new Color(0, 170, 0));
		colorsMap.put(TextFormat.DARK_AQUA, new Color(0, 170, 170));
		colorsMap.put(TextFormat.DARK_RED, new Color(170, 0, 0));
		colorsMap.put(TextFormat.DARK_PURPLE, new Color(170, 0, 170));
		colorsMap.put(TextFormat.GOLD, new Color(255, 170, 0));
		colorsMap.put(TextFormat.BLUE, new Color(85, 85, 255));
		colorsMap.put(TextFormat.GREEN, new Color(85, 255, 85));
		colorsMap.put(TextFormat.AQUA, new Color(85, 255, 255));
		colorsMap.put(TextFormat.RED, new Color(255, 85, 85));
		colorsMap.put(TextFormat.LIGHT_PURPLE, new Color(255, 85, 255));
		colorsMap.put(TextFormat.YELLOW, new Color(255, 255, 85));
		
		graysMap.put(TextFormat.BLACK, new Color(0, 0, 0));
		graysMap.put(TextFormat.DARK_GRAY, new Color(85, 85, 85));
		graysMap.put(TextFormat.GRAY, new Color(170, 170, 170));
		graysMap.put(TextFormat.WHITE, new Color(255, 255, 255));
	}
	
	public HologramCmd()
	{
		super("hologram",
			"Creates a hologram at your desired coordinates after placing your Armor Stand anywhere.\n"
				+ "Use $ for colors, use $$ for $. Requires creative mode.",
			"text <x> <y> <z> <text>", "image <x> <y> <z> <height> <URL>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(!MC.player.abilities.creativeMode)
			throw new CmdError("Creative mode only.");
		if(args.length >= 5 && MathUtils.isDouble(args[1])
			&& MathUtils.isDouble(args[2]) && MathUtils.isDouble(args[3]))
		{
			if(args[0].equalsIgnoreCase("text"))
			{
				ItemStack stack = new ItemStack(Items.ARMOR_STAND);
				CompoundTag compound = new CompoundTag();
				ListTag pos = new ListTag();
				pos.add(DoubleTag.of(Integer.parseInt(args[1])));
				pos.add(DoubleTag.of(Integer.parseInt(args[2])));
				pos.add(DoubleTag.of(Integer.parseInt(args[3])));
				compound.putBoolean("Invisible", true);
				compound.putBoolean("ShowArms", false);
				compound.putBoolean("Small", true);
				compound.putBoolean("Marker", true);
				compound.putBoolean("NoBasePlate", true);
				compound.putBoolean("NoGravity", true);
				compound.putBoolean("CustomNameVisible", true);
				String text = "";
				for(int i = 4; i < args.length; i++)
					text += args[i] + " ";
				text = text.substring(0, text.length() - 1);
				text = text.replace("$", "\u00a7").replace("\u00a7\u00a7", "$");
				compound.putString("CustomName", text);
				compound.put("Pos", pos);
				stack.putSubTag("EntityTag", compound);
				stack.setCustomName(new LiteralText("\u00a74Hologram"));
				if(placeStackInHotbar(stack, false))
					ChatUtils.message("Item created.");
				else
					throw new CmdError("Please clear a slot in your hotbar.");
			}else if(args[0].equalsIgnoreCase("image") && args.length == 6
				&& MathUtils.isInteger(args[4]))
			{
				if(Integer.parseInt(args[4]) <= 0)
					throw new CmdSyntaxError("Width cannot be negative or 0!");
				// Clear any previous running armor stand generators
				standPos = null;
				colorCodes = null;
				slot = -1;
				index = 0;
				ItemStack stack = new ItemStack(Items.ARMOR_STAND);
				CompoundTag compound = new CompoundTag();
				ListTag pos = new ListTag();
				pos.add(DoubleTag.of(Integer.parseInt(args[1])));
				pos.add(DoubleTag.of(Integer.parseInt(args[2])));
				pos.add(DoubleTag.of(Integer.parseInt(args[3])));
				compound.putBoolean("Invisible", true);
				compound.putBoolean("ShowArms", false);
				compound.putBoolean("Small", true);
				compound.putBoolean("Marker", true);
				compound.putBoolean("NoGravity", true);
				compound.putBoolean("CustomNameVisible", true);
				compound.put("Pos", pos);
				// The Pos argument will be the position of the top armor stand
				standPos = new BlockPos(Integer.parseInt(args[1]),
					Integer.parseInt(args[2]), Integer.parseInt(args[3]));
				//From ImageIO.read()
				BufferedImage image;
				try
				{
					URL url = new URL(args[5]);
					InputStream istream = null;
					try
					{
						URLConnection connection = url.openConnection();
						// Will bypass 403 errors
						connection.setRequestProperty("User-Agent",
							"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
						connection.connect();
						istream = connection.getInputStream();
					}catch(IOException e)
					{
						throw new IIOException(
							"Can't get input stream from URL!", e);
					}
					ImageInputStream stream =
						ImageIO.createImageInputStream(istream);
					try
					{
						image = ImageIO.read(stream);
						if(image == null)
							stream.close();
					}finally
					{
						istream.close();
					}
					float divide =
						image.getHeight() / Integer.parseInt(args[4]);
					int newWidth = (int)(image.getWidth() / divide);
					if(newWidth <= 0)
						throw new CmdError("Image is too tall!");
					image =
						resizeImage(image, newWidth, Integer.parseInt(args[4]));
					// Flips width and height so it can be converted easier
					TextFormat[][] chatImg =
						new TextFormat[image.getHeight()][image.getWidth()];
					for(int x = 0; x < image.getWidth(); x++)
						for(int y = 0; y < image.getHeight(); y++)
						{
							int rgb = image.getRGB(x, y);
							chatImg[y][x] =
								getClosestChatColor(new Color(rgb, true));
						}
					int index = 0;
					colorCodes = new String[image.getHeight()];
					for(TextFormat[] width : chatImg)
					{
						String str = "";
						for(TextFormat formatting : width)
						{
							if(formatting == null)
								formatting = TextFormat.WHITE;
							str += formatting.toString() + "\u2b1b";
						}
						colorCodes[index] = str;
						index++;
					}
				}catch(Exception e)
				{
					e.printStackTrace();
					throw new CmdError(
						"An error occured while trying to convert the URL into a color code!");
				}
				compound.putString("CustomName", colorCodes[index]);
				stack.putSubTag("EntityTag", compound);
				stack.setCustomName(new LiteralText("\u00a74Hologram - "
					+ (colorCodes.length - index) + " Stands Left to Place!"));
				// Doesn't increment index here because it will prevent the
				// first layer from being placed
				if(placeStackInHotbar(stack, true))
				{
					ChatUtils
						.message("Place all the armor stands to create image!");
					EVENTS.add(PacketOutputListener.class, this);
				}else
				{
					// Incorrect arguments will clear any previous running armor
					// stand generators
					standPos = null;
					colorCodes = null;
					slot = -1;
					index = 0;
					EVENTS.remove(PacketOutputListener.class, this);
					throw new CmdError("Please clear a slot in your hotbar.");
				}
			}else
				throw new CmdSyntaxError();
		}else
			throw new CmdSyntaxError();
	}

	@Override
	public void onSentPacket(PacketOutputEvent event)
	{
		if(shouldClear)
		{
			EVENTS.remove(PacketOutputListener.class, this);
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
				36 + slot, ItemStack.EMPTY));
			ChatUtils.message("Image creation successful!");
			slot = -1;
			shouldClear = false;
		}
		if(!(event.getPacket() instanceof PlayerInteractBlockC2SPacket))
			return;
		if(MC.player.inventory.selectedSlot == slot
			&& MC.player.inventory.getMainHandStack().getItem() == Items.ARMOR_STAND)
		{
			ItemStack stack = new ItemStack(Items.ARMOR_STAND);
			CompoundTag compound = new CompoundTag();
			ListTag pos = new ListTag();
			// Armor stand goes down 0.2 in pos each time
			pos.add(DoubleTag.of(standPos.getX()));
			pos.add(DoubleTag.of(standPos.getY() - 0.2 * index));
			pos.add(DoubleTag.of(standPos.getZ()));
			compound.putBoolean("Invisible", true);
			compound.putBoolean("ShowArms", false);
			compound.putBoolean("Small", true);
			compound.putBoolean("Marker", true);
			compound.putBoolean("NoGravity", true);
			compound.putBoolean("CustomNameVisible", true);
			compound.put("Pos", pos);
			compound.putString("CustomName", colorCodes[index]);
			stack.putSubTag("EntityTag", compound);
			stack.setCustomName(new LiteralText("\u00a74Hologram - "
				+ (colorCodes.length - 1 - index) + " Stands Left to Place!"));
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, stack));
			index++;
			if(index > colorCodes.length - 1)
			{
				// Clears item and slot on next run
				shouldClear = true;
				standPos = null;
				colorCodes = null;
				index = 0;
			}
		}else if(slot == -1 || MC.player.inventory.getInvStack(slot) == null)
		{
			standPos = null;
			colorCodes = null;
			slot = -1;
			index = 0;
			EVENTS.remove(PacketOutputListener.class, this);
		}
	}
	
	private boolean placeStackInHotbar(ItemStack stack, boolean setSlot)
	{
		for(int i = 0; i < 9; i++)
		{
			if(!MC.player.inventory.getInvStack(i).isEmpty())
				continue;
			
			if(setSlot)
				slot = i;
			
			MC.player.networkHandler.sendPacket(
				new CreativeInventoryActionC2SPacket(36 + i, stack));
			return true;
		}
		return false;
	}
	
	private TextFormat getClosestChatColor(Color color)
	{
		if(color.getAlpha() < 80)
			return null;
		for(Entry<TextFormat, Color> entry : colorsMap.entrySet())
			if(areIdentical(entry.getValue(), color))
				return entry.getKey();
		double bestGrayDistance = -1.0D;
		TextFormat bestGrayMatch = null;
		for(Entry<TextFormat, Color> entry : graysMap.entrySet())
		{
			double distance = getDistance(color, entry.getValue());
			if(distance < bestGrayDistance || bestGrayDistance == -1.0D)
			{
				bestGrayDistance = distance;
				bestGrayMatch = entry.getKey();
			}
		}
		if(bestGrayDistance < 17500.0D)
			return bestGrayMatch;
		double bestColorDistance = -1.0D;
		TextFormat bestColorMatch = null;
		for(Entry<TextFormat, Color> entry : colorsMap.entrySet())
		{
			double distance = getDistance(color, entry.getValue());
			if(distance < bestColorDistance || bestColorDistance == -1.0D)
			{
				bestColorDistance = distance;
				bestColorMatch = entry.getKey();
			}
		}
		return bestColorMatch;
	}

	private boolean areIdentical(Color c1, Color c2)
	{
		return Math.abs(c1.getRed() - c2.getRed()) <= 5
			&& Math.abs(c1.getGreen() - c2.getGreen()) <= 5
			&& Math.abs(c1.getBlue() - c2.getBlue()) <= 5;
	}

	private double getDistance(Color c1, Color c2)
	{
		double rmean = (c1.getRed() + c2.getRed()) / 2.0D;
		double r = c1.getRed() - c2.getRed();
		double g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2.0D + rmean / 256.0D;
		double weightG = 4.0D;
		double weightB = 2.0D + (255.0D - rmean) / 256.0D;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}

	private BufferedImage resizeImage(BufferedImage originalImage, int width,
		int height)
	{
		Image img = originalImage.getScaledInstance(width, height, 1);
		BufferedImage bimage =
			new BufferedImage(img.getWidth(null), img.getHeight(null), 2);
		
		Graphics2D graphics = bimage.createGraphics();
		graphics.drawImage(img, 0, 0, null);
		graphics.dispose();
		
		return bimage;
	}
}
