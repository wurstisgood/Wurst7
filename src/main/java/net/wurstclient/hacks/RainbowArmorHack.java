/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.registry.Registry;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.ChatUtils;

public final class RainbowArmorHack extends Hack implements UpdateListener
{
	private final CheckboxSetting headBlock = new CheckboxSetting(
		"Use Blocks for Head Slot", false);
	private final CheckboxSetting rainbowArmor = new CheckboxSetting(
		"Rainbow Leather Armor", true);
	private final BlockListSetting blacklist = new BlockListSetting(
		"Blocks to Avoid", "Blocks that will not be placed in the head slot.", "minecraft:pumpkin",
		"minecraft:spawner", "minecraft:brown_mushroom_block", "minecraft:red_mushroom_block",
		"minecraft:command_block", "minecraft:barrier", "minecraft:dragon_egg", "minecraft:farmland",
		"minecraft:tall_grass", "minecraft:chain_command_block", "minecraft:repeating_command_block",
		"minecraft:grass_path", "minecraft:structure_block", "minecraft:structure_void");
	
	public RainbowArmorHack()
	{
		super("RainbowArmor", 
			"Rapidly switches the items in your armor slots,\n"
			+ "making it look like you have rainbow armor.\n"
			+ "Creative mode only.");
		setCategory(Category.ITEMS);
		addSetting(headBlock);
		addSetting(rainbowArmor);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!MC.player.abilities.creativeMode)
		{
			ChatUtils.error("Creative mode only.");
			setEnabled(false);
		}
		if(headBlock.isChecked())
		{
			ItemStack block = getRandomBlock();
			MC.player.inventory.armor.set(3, block);
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(5, block));
		}
		if(rainbowArmor.isChecked())
		{
			ItemStack helm = new ItemStack(Items.LEATHER_HELMET);
			ItemStack chest = new ItemStack(Items.LEATHER_CHESTPLATE);
			ItemStack legs = new ItemStack(Items.LEATHER_LEGGINGS);
			ItemStack feet = new ItemStack(Items.LEATHER_BOOTS);
			int color = (int)Math.round(Math.random() * 16777215);
			((DyeableArmorItem)chest.getItem()).setColor(chest, color);
			((DyeableArmorItem)legs.getItem()).setColor(legs, color);
			((DyeableArmorItem)feet.getItem()).setColor(feet, color);
			if(!headBlock.isChecked())
				((DyeableArmorItem)helm.getItem()).setColor(helm, color);
			MC.player.inventory.armor.set(2, chest);
			MC.player.inventory.armor.set(1, legs);
			MC.player.inventory.armor.set(0, feet);
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
				6, chest));
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
				7, legs));
			MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
				8, feet));
			if(!headBlock.isChecked())
			{
				MC.player.inventory.armor.set(3, helm);
				MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
					5, helm));
			}
		}
	}
	
	private ItemStack getRandomBlock()
	{
		Random random = new Random();
	    ArrayList<Block> blockReg = Lists.newArrayList(Registry.BLOCK.iterator());
	    blockReg.remove(Blocks.AIR);
	    for(String blockName : blacklist.getBlockNames())
	    	 blockReg.remove(BlockUtils.getBlockFromName(blockName));
	    int randBlock = random.nextInt(blockReg.size());
	    return new ItemStack(blockReg.get(randBlock));
	}
}
