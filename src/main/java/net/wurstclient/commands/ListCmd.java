/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.WordUtils;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.TextFormat;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class ListCmd extends Command
{
	private final Map<Character, TextFormat> colorMap = initColorMap();
	
	public ListCmd()
	{
		super("list", "Shows the list of all the online players.\n"
			+ "The tabcomplete mode shows names that don't have colors or prefixes to them.",
			"search <name>", "teams", "tabcomplete", "tabcomplete search <name>");
	}
	
	private Map<Character, TextFormat> initColorMap()
	{
		Map<Character, TextFormat> colorMap = new HashMap<>();
		for(TextFormat formatting : TextFormat.values())
			colorMap.put(formatting.toString().charAt(1), formatting);
		return colorMap;
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("tabcomplete")))
		{
			List<String> players = new ArrayList<>();
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
				players.add(args.length == 1 ? info.getProfile().getName() : 
					info.getDisplayName().asFormattedString());
			 StringBuilder builder = new StringBuilder("Online Players (" + players.size() + "): ");
			 players.forEach(player -> builder.append(player).append(", "));
			 if(players.size() > 0 && !builder.toString().equals(""))
				 ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
			 else
				 ChatUtils.message("Cannot find any players!");
		}else if(args.length == 1 && args[0].equalsIgnoreCase("teams"))
		{
			List<String> players = new ArrayList<>();
			Map<TextFormat, List<String>> playersSorted = new LinkedHashMap<>();
			for(TextFormat formatting : TextFormat.values())
				playersSorted.put(formatting, new ArrayList<>());
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
				players.add(info.getDisplayName().asFormattedString());
			for(String name : players)
				playersSorted.get(colorMap.get(getColor(name).charAt(0))).add(name);
			for(Entry<TextFormat, List<String>> entry : playersSorted.entrySet())
			{
				if(entry.getValue().isEmpty())
					continue;
				List<String> names = entry.getValue();
				String colorName = WordUtils.capitalize(entry.getKey().name().toLowerCase().replace('_', ' '));
				StringBuilder builder = new StringBuilder(entry.getKey() + colorName + TextFormat.RESET +
					" (" + names.size() + "): ");
				names.forEach(name -> builder.append(name).append(", "));
				ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
			}
		}else if((args.length == 2 && args[0].equalsIgnoreCase("search"))
			|| (args.length == 3 && args[0].equalsIgnoreCase("tabcomplete")
			&& args[1].equalsIgnoreCase("search")))
		{
			List<String> matching = new ArrayList<>();
			String search = args[args.length - 1];
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
			{
				String realName = args.length == 3 ? info.getProfile().getName() : 
					info.getDisplayName().asFormattedString();
				if(realName.contains(search))
					matching.add(realName);
			}
			StringBuilder builder = new StringBuilder("Matching Queries (" + matching.size() + "): ");
			matching.forEach(player -> builder.append(player).append(", "));
			if(matching.size() > 0 && !builder.toString().equals(""))
				 ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
			 else
				 ChatUtils.message("Cannot find any players matching the query.");
		}else
			throw new CmdSyntaxError();
	}
	
	private String getColor(String name)
	{
		List<Character> colors =
		Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
			'd', 'e');
		for(int i = 0; i < name.length() - 2; i++)
			if(name.charAt(i) == '\u00a7')
			{
				char next =  name.charAt(i + 1);
				if(colors.contains(next))
					return String.valueOf(next);
			}
		return "f";
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Get Players";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("list");
	}
}
