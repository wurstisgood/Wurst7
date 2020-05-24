/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.suggestion.Suggestion;

import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.util.ChatUtils;

public final class PluginsCmd extends Command implements PacketInputListener
{
	private int timer;
	private boolean isSearch;
	private String searchQuery;
	
	public PluginsCmd()
	{
		super("plugins",
			"Allows you to get the plugins of the server.\n"
				+ "Note: This will not find plugins that have no commands.",
				".plugins", ".plugins search <name>");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0)
		{
			timer = 0;
			isSearch = false;
			MC.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
			EVENTS.add(PacketInputListener.class, this);
		}else if(args.length == 2 && args[0].equalsIgnoreCase("search"))
		{
			timer = 0;
			isSearch = true;
			searchQuery = args[1];
			MC.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
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
			List<String> plugins = new ArrayList<>();
			for(Suggestion sug : packet.getSuggestions().getList()) 
			{
				String[] arguments = sug.getText().split(":");
				if(arguments.length > 1 && !arguments[0].substring(1).equals("") && 
					!plugins.contains(arguments[0].substring(1)))
					plugins.add(arguments[0].substring(1));
			}
			plugins = plugins.stream()
				.filter(plugin -> !plugin.equalsIgnoreCase("minecraft"))
				.filter(plugin -> !plugin.equalsIgnoreCase("bukkit")).collect(Collectors.toList());
			if(!isSearch)
			{
				StringBuilder builder = new StringBuilder("Plugins (" + plugins.size() + "): ");
				plugins.forEach(plugin -> builder.append(plugin).append(", "));
				if(plugins.size() > 0 && !builder.toString().equals(""))
					ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
				else
					ChatUtils.message("Cannot find any plugins!");
			}else if(searchQuery != null)
			{
				List<String> matching = new ArrayList<>();
				for(String pluginName : plugins)
					if(pluginName.contains(searchQuery))
						matching.add(pluginName);
				StringBuilder builder = new StringBuilder("Matching Queries (" + matching.size() + "): ");
				matching.forEach(plugin -> builder.append(plugin).append(", "));
				if(matching.size() > 0 && !builder.toString().equals(""))
					ChatUtils.message(builder.toString().substring(0, builder.toString().length() - 2)); 
				else
					ChatUtils.message("Cannot find any plugins matching the query.");
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
	
	@Override
	public String getPrimaryAction()
	{
		return "Get Plugins";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("plugins");
	}
}
