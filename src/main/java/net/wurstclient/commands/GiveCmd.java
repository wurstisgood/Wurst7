/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.Arrays;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

public final class GiveCmd extends Command
{
	private ItemTemplate[] getTemplates()
	{
		return new ItemTemplate[]{
		new ItemTemplate("Knockback Stick", Items.STICK,
			"{Enchantments:[{id:knockback, lvl:12}], display:{Name:'{\"text\":\"\u00a76Knockback Stick\"}'},"
				+ "HideFlags:63}"),
		
		new ItemTemplate("One Hit Sword", Items.DIAMOND_SWORD,
			"{AttributeModifiers:[{AttributeName:generic.attack_damage,"
				+ "UUID:[I;0,246216,0,24636],Amount:2147483647,"
				+ "Name:generic.attackDamage}],"
				+ "display:{Name:'{\"text\":\"\u00a76One Hitter\"}'}, Unbreakable:1,"
				+ "HideFlags:63}"),
		
		new ItemTemplate("Troll Speed Crash Chestplate",
			Items.DIAMOND_CHESTPLATE,
			"{AttributeModifiers:[" + "{AttributeName:generic.movement_speed,"
				+ "Name:generic.movementSpeed, Amount:2147483647,"
				+ "Operation:0, UUID:[I;0,43631,0,2641]}"
				+ "], display:{Name:'{\"text\":\"\u00a76Magical Chestplate\"}'}, Unbreakable:1,"
				+ "HideFlags:63}"),
		
		new ItemTemplate("Troll Speed Crash Axe", Items.WOODEN_AXE,
			"{AttributeModifiers:[" + "{AttributeName:generic.movement_speed,"
				+ "Name:generic.movementSpeed, Amount:2147483647,"
				+ "Operation:0, UUID:[I;0,43631,0,2641]}"
				+ "], display:{Name:'{\"text\":\"\u00a74WorldEdit Axe\"}'}, Unbreakable:1,"
				+ "HideFlags:63}"),
		
		new ItemTemplate("Firework Smoke Bomb", Items.FIREWORK_ROCKET,
			"{Fireworks:{Flight:-1,Explosions:[" + generateNBTFireworks()
				+ "{Flicker:1b,Trail:1b,Type:2,Colors:[I;38718],FadeColors:[I;60159]},"
				+ "{Flicker:1b,Trail:1b,Type:3,Colors:[I;3873557],FadeColors:[I;11708091]},"
				+ "{Flicker:1b,Trail:1b,Type:4,Colors:[I;11468981],FadeColors:[I;16711935]},"
				+ "{Flicker:1b,Trail:1b,Type:0,Colors:[I;16711680],FadeColors:[I;16711849]},"
				+ "{Flicker:1b,Trail:1b,Type:1,Colors:[I;16776960],FadeColors:[I;16744319]}"
				+ "]}, display:{Name:'{\"text\":\"\u00a70Smoke\u00a74Bomb\"}'}," + "HideFlags:63}"),
		
		new ItemTemplate("God Boots", Items.DIAMOND_BOOTS, "{Enchantments:["
			+ "{id:protection, lvl:9999}," + "{id:fire_protection, lvl:9999}," + "{id:feather_falling, lvl:9999},"
			+ "{id:blast_protection, lvl:9999}," + "{id:projectile_protection, lvl:9999}," + "{id:respiration, lvl:9999},"
			+ "{id:aqua_affinity, lvl:9999}," + "{id:thorns, lvl:9999}"
			+ "], AttributeModifiers:["
			+ "{AttributeName:generic.movement_speed,"
			+ "Name:generic.movementSpeed, Amount:0.2,"
			+ "Operation:0, UUID:[I;0,43631,0,2641]},"
			+ "{AttributeName:generic.max_health,"
			+ "Name:generic.maxHealth, Amount:500,"
			+ "Operation:0, UUID:[I;0,89173,0,146919]},"
			+ "{AttributeName:generic.attack_damage,"
			+ "Name:generic.attackDamage, Amount:2147483647,"
			+ "Operation:0, UUID:[I;0,89680,0,130215]},"
			+ "{AttributeName:generic.knockback_resistance,"
			+ "Name:generic.knockbackResistance, Amount:1,"
			+ "Operation:0, UUID:[I;0,84926,0,175435]},"
			+ "{AttributeName:generic.armor,"
			+ "Name:generic.armor, Amount:10000,"
			+ "Operation:0, UUID:[I;0,83431,0,134568]}"
			+ "], display:{Name:'{\"text\":\"\u00a79GodBoots\"}'}, Unbreakable:1," + "HideFlags:63}"),
		
		new ItemTemplate("God Boots No Enchants", Items.DIAMOND_BOOTS,
			"{AttributeModifiers:["
			+ "{AttributeName:generic.movement_speed,"
			+ "Name:generic.movementSpeed, Amount:0.2,"
			+ "Operation:0, UUID:[I;0,43631,0,2641]},"
			+ "{AttributeName:generic.max_health,"
			+ "Name:generic.maxHealth, Amount:500,"
			+ "Operation:0, UUID:[I;0,89173,0,146919]},"
			+ "{AttributeName:generic.attack_damage,"
			+ "Name:generic.attackDamage, Amount:2147483647,"
			+ "Operation:0, UUID:[I;0,89680,0,130215]},"
			+ "{AttributeName:generic.knockback_resistance,"
			+ "Name:generic.knockbackResistance, Amount:1,"
			+ "Operation:0, UUID:[I;0,84926,0,175435]},"
			+ "{AttributeName:generic.armor,"
			+ "Name:generic.armor, Amount:10000,"
			+ "Operation:0, UUID:[I;0,83431,0,134568]}"
			+ "], display:{Name:'{\"text\":\"\u00a79GodBoots\"}'}, Unbreakable:1," + "HideFlags:63}"),
		
		new ItemTemplate("5 Speed Legs", Items.DIAMOND_LEGGINGS,
			"{AttributeModifiers:[" + "{AttributeName:generic.movement_speed,"
				+ "Name:generic.movementSpeed, Amount:5,"
				+ "Operation:0, UUID:[I;0,43631,0,2641]}"
				+ "], display:{Name:'{\"text\":\"\u00a7c\u00a7oSPEEDSTER\"}'}, Unbreakable:1,"
				+ "HideFlags:63}"),
		
		new ItemTemplate("Super Bow", Items.BOW,
			"{Enchantments:[" + "{id:power, lvl:32767}, {id:punch, lvl:5}, {id:flame, lvl:1},"
				+ "{id:infinity, lvl:1}"
				+ "], display:{Name:'{\"text\":\"\u00a76Super Bow\"}'}, HideFlags:63}"),
		
		new ItemTemplate("Super Thorns Chestplate", Items.DIAMOND_CHESTPLATE,
			"{Enchantments:[" + "{id:thorns, lvl:32767}," + "{id:protection, lvl:32767}"
				+ "], AttributeModifiers:["
				+ "{AttributeName:generic.max_health, Name:generic.maxHealth,"
				+ "Amount:200, Operation:0, UUID:[I;0,43631,0,2641]}"
				+ "], display:{Name:'{\"text\":\"\u00a76Super Thorns Chestplate\"}'}, HideFlags:63,"
				+ "Unbreakable:1}"),
		
		new ItemTemplate("Super Potion", Items.POTION,
			"{CustomPotionEffects: ["
				+ "{Id:11, Amplifier:127, Duration:2147483647},"
				+ "{Id:10, Amplifier:127, Duration:2147483647},"
				+ "{Id:23, Amplifier:127, Duration:2147483647},"
				+ "{Id:16, Amplifier:0, Duration:2147483647},"
				+ "{Id:8, Amplifier:3, Duration:2147483647},"
				+ "{Id:1, Amplifier:5, Duration:2147483647},"
				+ "{Id:5, Amplifier:127, Duration:2147483647},"
				+ "{Id:26, Amplifier:127, Duration:2147483647}],"
				+ "display:{Name:'{\"text\":\"\u00a76Super Potion\"}'},"
				+ "Potion:\"minecraft:water\", HideFlags:63}"),
		
		new ItemTemplate("Griefer Potion", Items.POTION,
			"{CustomPotionEffects:["
				+ "{Id:3, Amplifier:127, Duration:2147483647}"
				+ "], display:{Name:'{\"text\":\"\u00a76Griefer Potion\"}'},"
				+ "Potion:\"minecraft:water\", HideFlags:63}"),
		
		new ItemTemplate("Lingering Damage", Items.LINGERING_POTION,
			"{CustomPotionEffects:["
				+ "{Id:2, Amplifier:127, Duration:2147483647},"
				+ "{Id:4, Amplifier:127, Duration:2147483647},"
				+ "{Id:9, Amplifier:127, Duration:2147483647},"
				+ "{Id:10, Amplifier:127, Duration:2147483647},"
				+ "{Id:15, Amplifier:127, Duration:2147483647},"
				+ "{Id:18, Amplifier:127, Duration:2147483647},"
				+ "{Id:19, Amplifier:0, Duration:2147483647},"
				+ "{Id:24, Amplifier:127, Duration:2147483647},"
				+ "{Id:25, Amplifier:127, Duration:2147483647},"
				+ "{Id:27, Amplifier:127, Duration:2147483647}"
				+ "], display:{Name:'{\"text\":\"\u00a74Lingering\"}'},"
				+ "Potion:\"minecraft:water\", HideFlags:63}"),
		
		new ItemTemplate("Skeleton Trap", Items.SKELETON_HORSE_SPAWN_EGG,
			"{EntityTag:{SkeletonTrap:1b},"
				+ "display:{Name:'{\"text\":\"\u00a77Skeleton Trap\"}'},HideFlags:63}"),
		
		new ItemTemplate("Hopper", Item.fromBlock(Blocks.HOPPER),
			"{BlockEntityTag:{TransferCooldown:0,Items:[{Slot:0b,id:dispenser,Count:127b,tag:"
				+ "{BlockEntityTag:{CustomName:'{\"text\":\"\u00a76\u00a7lOres\"}',Items:[{Slot:0b,id:coal_ore,Count:127b},"
				+ "{Slot:1b,id:iron_ore,Count:127b},"
				+ "{Slot:2b,id:gold_ore,Count:127b},"
				+ "{Slot:3b,id:lapis_ore,Count:127b},"
				+ "{Slot:4b,id:nether_star,Count:127b},"
				+ "{Slot:5b,id:redstone_ore,Count:127b},"
				+ "{Slot:6b,id:diamond_ore,Count:127b},"
				+ "{Slot:7b,id:emerald_ore,Count:127b},"
				+ "{Slot:8b,id:quartz_ore,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lOres\"}'}}},{Slot:1b,id:dispenser,Count:127b,"
				+ "tag:{BlockEntityTag:{CustomName:'{\"text\":\"\u00a76\u00a7lResource-Blocks\"}',"
				+ "Items:[{Slot:0b,id:coal_block,Count:127b},"
				+ "{Slot:1b,id:iron_block,Count:127b},"
				+ "{Slot:2b,id:gold_block,Count:127b},"
				+ "{Slot:3b,id:lapis_block,Count:127b},"
				+ "{Slot:4b,id:beacon,Count:127b},"
				+ "{Slot:5b,id:redstone_block,Count:127b},"
				+ "{Slot:6b,id:diamond_block,Count:127b},"
				+ "{Slot:7b,id:emerald_block,Count:127b},"
				+ "{Slot:8b,id:quartz_block,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lResource-Blocks\"}'}}},"
				+ "{Slot:2b,id:hopper,Count:127b,"
				+ "tag:{BlockEntityTag:{TransferCooldown:0,Items:["
				+ "{Slot:0b,id:dispenser,Count:127b,tag:{BlockEntityTag:{Items:["
				+ "{Slot:0b,id:coal_ore,Count:127b},"
				+ "{Slot:1b,id:iron_ore,Count:127b},"
				+ "{Slot:2b,id:gold_ore,Count:127b},"
				+ "{Slot:3b,id:lapis_ore,Count:127b},"
				+ "{Slot:4b,id:nether_star,Count:127b},"
				+ "{Slot:5b,id:redstone_ore,Count:127b},"
				+ "{Slot:6b,id:diamond_ore,Count:127b},"
				+ "{Slot:7b,id:emerald_ore,Count:127b},"
				+ "{Slot:8b,id:quartz_ore,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lOres\"}'}}},"
				+ "{Slot:1b,id:dispenser,Count:127b,tag:{BlockEntityTag:{"
				+ "Items:[{Slot:0b,id:coal_block,Count:127b},"
				+ "{Slot:1b,id:iron_block,Count:127b},"
				+ "{Slot:2b,id:gold_block,Count:127b},"
				+ "{Slot:3b,id:lapis_block,Count:127b},"
				+ "{Slot:4b,id:beacon,Count:127b},"
				+ "{Slot:5b,id:redstone_block,Count:127b},"
				+ "{Slot:6b,id:diamond_block,Count:127b},"
				+ "{Slot:7b,id:emerald_block,Count:127b},"
				+ "{Slot:8b,id:quartz_block,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lResource-Blocks\"}'}}},"
				+ "{Slot:2b,id:obsidian,Count:127b,tag:{display:{Name:'{\"text\":\"\u00a7c\u00a7lTest with me ;)\"}'}}},"
				+ "{Slot:3b,id:dispenser,Count:127b,tag:{BlockEntityTag:{Items:["
				+ "{Slot:0b,id:sand,Count:127b},"
				+ "{Slot:1b,id:sand,Count:127b,Damage:1s},"
				+ "{Slot:2b,id:gravel,Count:127b},"
				+ "{Slot:3b,id:clay,Count:127b},"
				+ "{Slot:4b,id:bedrock,Count:127b},"
				+ "{Slot:5b,id:sponge,Count:127b},"
				+ "{Slot:6b,id:grass,Count:127b},"
				+ "{Slot:7b,id:dirt,Count:127b,Damage:2s},"
				+ "{Slot:8b,id:mycelium,Count:127b}],id:Trap,Lock:\"\"},display:{"
				+ "Name:'{\"text\":\"\u00a76\u00a7lExtra-Blocks\"}'}}},"
				+ "{Slot:4b,id:dispenser,Count:127b,tag:{BlockEntityTag:{Items:[{"
				+ "Slot:0b,id:snow,Count:127b},"
				+ "{Slot:1b,id:ice,Count:127b},"
				+ "{Slot:2b,id:packed_ice,Count:127b},"
				+ "{Slot:3b,id:hay_block,Count:127b},"
				+ "{Slot:4b,id:glowstone,Count:127b},"
				+ "{Slot:5b,id:brick_block,Count:127b},"
				+ "{Slot:6b,id:netherrack,Count:127b},"
				+ "{Slot:7b,id:soul_sand,Count:127b},"
				+ "{Slot:8b,id:nether_brick,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lExtra-Blocks2\"}'}}}],id:Hopper,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a7c\u00a7lBackUp#NoLimit\"}'}}},"
				+ "{Slot:3b,id:dispenser,Count:127b,"
				+ "tag:{BlockEntityTag:{CustomName:'{\"text\":\"\u00a76\u00a7lExtra-Blocks\"}',Items:[{"
				+ "Slot:0b,id:sand,Count:127b},"
				+ "{Slot:1b,id:sand,Count:127b,Damage:1s},"
				+ "{Slot:2b,id:gravel,Count:127b},"
				+ "{Slot:3b,id:clay,Count:127b},"
				+ "{Slot:4b,id:bedrock,Count:127b},"
				+ "{Slot:5b,id:sponge,Count:127b},"
				+ "{Slot:6b,id:grass,Count:127b},"
				+ "{Slot:7b,id:dirt,Count:127b,Damage:2s},"
				+ "{Slot:8b,id:mycelium,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lExtra-Blocks-1\"}'}}},"
				+ "{Slot:4b,id:dispenser,Count:127b,tag:{BlockEntityTag:{"
				+ "CustomName:'{\"text\":\"\u00a76\u00a7lExtra-Blocks2\"}',Items:[{Slot:0b,id:snow,Count:127b},"
				+ "{Slot:1b,id:ice,Count:127b},"
				+ "{Slot:2b,id:packed_ice,Count:127b},"
				+ "{Slot:3b,id:hay_block,Count:127b},"
				+ "{Slot:4b,id:glowstone,Count:127b},"
				+ "{Slot:5b,id:brick_block,Count:127b}"
				+ ",{Slot:6b,id:netherrack,Count:127b},"
				+ "{Slot:7b,id:soul_sand,Count:127b},"
				+ "{Slot:8b,id:nether_brick,Count:127b}],id:Trap,Lock:\"\"},"
				+ "display:{Name:'{\"text\":\"\u00a76\u00a7lExtra-Blocks-2\"}'}}}],id:Hopper,Lock:\"\"},"
				+ "display:{Lore:['{\"text\":\"\u00a76Resource Rich Edition!\"}']}}"),

		new ItemTemplate("Slime Crash", Items.SLIME_SPAWN_EGG,
			"{display:{Name:'{\"text\":\"\u00a7aSlime of Doom\"}'},EntityTag:{Size:32767}}"),

		new ItemTemplate("Creeper Nuke", Items.CREEPER_SPAWN_EGG,
			"{display:{Name:'{\"text\":\"\u00a73Tekkit Nuke\"}'},"
				+ "EntityTag:{ExplosionRadius:127,Fuse:-1,ignited:1}}")};
	}
	
	public GiveCmd()
	{
		super("give",
			"Gives you an item with custom NBT data.\n"
				+ "Requires creative mode.",
			".give <item> [<amount>] [<nbt>]", ".give <id> [<amount>] [<nbt>]",
			".give template <template_id> [<amount>]", ".give templates");
	}
	
	private String generateNBTFireworks()
	{
		String nbt = "";
		for(int i = 0; i < 1000; i++)
			nbt = nbt
				+ "{Flicker:1b,Trail:1b,Type:2,Colors:[I;38718],FadeColors:[I;60159]},"
				+ "{Flicker:1b,Trail:1b,Type:3,Colors:[I;3873557],FadeColors:[I;11708091]},"
				+ "{Flicker:1b,Trail:1b,Type:4,Colors:[I;11468981],FadeColors:[I;16711935]},"
				+ "{Flicker:1b,Trail:1b,Type:0,Colors:[I;16711680],FadeColors:[I;16711849]},"
				+ "{Flicker:1b,Trail:1b,Type:1,Colors:[I;16776960],FadeColors:[I;16744319]},";
		return nbt;
	}
	
	private ItemTemplate templates[];
	
	@Override
	public void call(String[] args) throws CmdException
	{
		templates = getTemplates();
		// validate input
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		if(!MC.player.abilities.creativeMode)
			throw new CmdError("Creative mode only.");
		
		// list all templates
		if(args[0].equalsIgnoreCase("templates"))
		{
			ChatUtils.message("\u00a7cItem templates:");
			for(int i = 0; i < templates.length; i++)
			{
				ItemTemplate template = templates[i];
				ChatUtils.message("\u00a7c" + (i + 1) + "\u00a7c: \u00a76" + template.name);
			}
			return;
		}
		
		// prepare item
		if(args[0].equalsIgnoreCase("template"))
		{
			// item from template
			
			if(args.length < 2 || args.length > 3)
				throw new CmdSyntaxError();
			if(!MathUtils.isInteger(args[1]))
				throw new CmdSyntaxError("Template ID must be a number.");
			int id = Integer.valueOf(args[1]);
			if(id < 1 || id > templates.length)
				throw new CmdError("Template ID is out of range.");

			ItemTemplate template = templates[id - 1];
			
			int amount = 1;
			if(args.length == 3)
			{
				if(!MathUtils.isInteger(args[2]))
					throw new CmdSyntaxError("Not a number: " + args[2]);
				
				amount = Integer.valueOf(args[2]);
				
				if(amount < 1)
					throw new CmdError("Amount cannot be less than 1.");
				
				if(amount > 64)
					throw new CmdError("Amount cannot be more than 64.");
			}
			
			// generate item
			ItemStack stack = new ItemStack(template.item, amount);
			try
			{
				CompoundTag tag = StringNbtReader.parse(template.tag);
				stack.setTag(tag);
				
			}catch(CommandSyntaxException e)
			{
				ChatUtils.message(e.getMessage());
				throw new CmdSyntaxError("NBT data is invalid.");
			}
			
			// give item
			if(placeStackInHotbar(stack))
				ChatUtils.message("Item" + (amount > 1 ? "s" : "") + " created.");
			else
				throw new CmdError("Please clear a slot in your hotbar.");
			return;
		}
		
		// id/name
		Item item = getItem(args[0]);
		
		if(item == Items.AIR && MathUtils.isInteger(args[0]))
			item = Item.byRawId(Integer.parseInt(args[0]));
		
		if(item == Items.AIR)
			throw new CmdError("Item \"" + args[0] + "\" could not be found.");
		
		// amount
		int amount = 1;
		if(args.length >= 2)
		{
			if(!MathUtils.isInteger(args[1]))
				throw new CmdSyntaxError("Not a number: " + args[1]);
			
			amount = Integer.valueOf(args[1]);
			
			if(amount < 1)
				throw new CmdError("Amount cannot be less than 1.");
			
			if(amount > 64)
				throw new CmdError("Amount cannot be more than 64.");
		}
		
		// nbt data
		String nbt = null;
		if(args.length >= 3)
			nbt = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		
		// generate item
		ItemStack stack = new ItemStack(item, amount);
		if(nbt != null)
			try
			{
				CompoundTag tag = StringNbtReader.parse(nbt);
				stack.setTag(tag);
				
			}catch(CommandSyntaxException e)
			{
				ChatUtils.message(e.getMessage());
				throw new CmdSyntaxError("NBT data is invalid.");
			}
		
		// give item
		if(placeStackInHotbar(stack))
			ChatUtils.message("Item" + (amount > 1 ? "s" : "") + " created.");
		else
			throw new CmdError("Please clear a slot in your hotbar.");
	}
	
	private Item getItem(String id) throws CmdSyntaxError
	{
		try
		{
			return Registry.ITEM.get(new Identifier(id));
			
		}catch(InvalidIdentifierException e)
		{
			throw new CmdSyntaxError("Invalid item: " + id);
		}
	}
	
	private boolean placeStackInHotbar(ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
		{
			if(!MC.player.inventory.getStack(i).isEmpty())
				continue;
			
			MC.player.networkHandler.sendPacket(
				new CreativeInventoryActionC2SPacket(36 + i, stack));
			return true;
		}
		
		return false;
	}
	
	private static class ItemTemplate
	{
		public Item item;
		public String name, tag;

		public ItemTemplate(String name, Item item, String tag)
		{
			this.name = name;
			this.item = item;
			this.tag = tag;
		}
	}
}
