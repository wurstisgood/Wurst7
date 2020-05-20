/*
 * Copyright © 2014 - 2018 | Wurst-Imperium | All rights reserved.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.WordUtils;

import com.mojang.brigadier.suggestion.Suggestion;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.TextFormat;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.util.ChatUtil;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.util.ChatUtils;

public final class ListCmd extends Command implements PacketInputListener
{
	private boolean isSearch;
	private String searchQuery;
	
	private int timer;
	
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
		if(args.length == 0)
		{
			List<String> players = new ArrayList<>();
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
				players.add(ChatUtil.stripTextFormat(info.getProfile().getName()));
			 StringBuilder builder = new StringBuilder("Online Players (" + players.size() + "): ");
			 players.forEach(player -> builder.append(player).append(", "));
			 if(players.size() > 0 && !builder.toString().equals(""))
				 ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
			 else
				 ChatUtils.message("Cannot find any players!");
		}else if(args.length == 1 && args[0].equalsIgnoreCase("tabcomplete"))
		{	
			timer = 0;
			isSearch = false;
			MC.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, ""));
			EVENTS.add(PacketInputListener.class, this);
		}else if(args.length == 1 && args[0].equalsIgnoreCase("teams"))
		{
			List<String> players = new ArrayList<>();
			Map<TextFormat, List<String>> playersSorted = new LinkedHashMap<>();
			for(TextFormat formatting : TextFormat.values())
				playersSorted.put(formatting, new ArrayList<>());
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
				players.add(info.getProfile().getName());
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
		}else if(args.length == 2 && args[0].equalsIgnoreCase("search"))
		{
			List<String> matching = new ArrayList<>();
			String search = args[1];
			for(PlayerListEntry info : MC.player.networkHandler.getPlayerList())
			{
				String realName = ChatUtil.stripTextFormat(info.getProfile().getName());
				if(realName.contains(search))
					matching.add(realName);
			}
			StringBuilder builder = new StringBuilder("Matching Queries (" + matching.size() + "): ");
			matching.forEach(player -> builder.append(player).append(", "));
			if(matching.size() > 0 && !builder.toString().equals(""))
				 ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
			 else
				 ChatUtils.message("Cannot find any players matching the query.");
		}else if(args.length == 3 && args[0].equalsIgnoreCase("tabcomplete") &&
			args[1].equalsIgnoreCase("search"))
		{
			timer = 0;
			isSearch = true;
			searchQuery = args[2];
			MC.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, ""));
			EVENTS.add(PacketInputListener.class, this);
		}else
			throw new CmdSyntaxError();
	}
	
	@Override
	public void onReceivedPacket(PacketInputEvent event)
	{
		timer += 50;
		if(event.getPacket() instanceof CommandSuggestionsS2CPacket) 
		{
			CommandSuggestionsS2CPacket packet = (CommandSuggestionsS2CPacket)event.getPacket();
            event.cancel();
            List<String> players = new ArrayList<>();
            for(Suggestion sug : packet.getSuggestions().getList()) 
            	players.add(sug.getText());
            if(!isSearch)
            {
            	StringBuilder builder = new StringBuilder("Online Players (" + players.size() + "): ");
            	players.forEach(player -> builder.append(player).append(", "));
            	if(players.size() > 0 && !builder.toString().equals(""))
            		ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
            	else
            		ChatUtils.message("Cannot find any players!");
            }else if(searchQuery != null)
            {
            	List<String> matching = new ArrayList<>();
            	for(String playerName : players)
            		if(playerName.contains(searchQuery))
            			matching.add(playerName);
            	StringBuilder builder = new StringBuilder("Matching Queries (" + matching.size() + "): ");
    			matching.forEach(player -> builder.append(player).append(", "));
    			if(matching.size() > 0 && !builder.toString().equals(""))
    				 ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
    			 else
    				 ChatUtils.message("Cannot find any players matching the query.");
            	searchQuery = null;
            }
            EVENTS.remove(PacketInputListener.class, this);
		}
		if(timer >= 20000)
		{
			ChatUtils.message("Server did not respond to TabComplete request.");
			EVENTS.remove(PacketInputListener.class, this);
		}
	}
	
	private String getColor(String name)
	{
		String[] colors =
		{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c",
			"d", "e", "f"};
		for(int i = 0; i < 16; i++)
			if(name.contains("\u00a7" + colors[i]))
				return colors[i];
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
