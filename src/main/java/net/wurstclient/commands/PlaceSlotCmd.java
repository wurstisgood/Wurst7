/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.commands;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;

public final class PlaceSlotCmd extends Command
{
	public PlaceSlotCmd()
	{
		super("placeslot", "Duplicates items from your hand to a specified armor slot.\n"
			+ "Requires creative mode.",
			"(head|chest|legs|feet)");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 1)
			throw new CmdSyntaxError();
		if(!MC.player.abilities.creativeMode)
			throw new CmdError("Creative mode only.");
		if(args.length == 1 && args[0].equalsIgnoreCase("head") ||
			args[0].equalsIgnoreCase("chest") || args[0].equalsIgnoreCase("legs") ||
			args[0].equalsIgnoreCase("feet"))
		{
			int slot = -1;
			int armorSlot = -1;
			ItemStack item = null;
			item = MC.player.inventory.getMainHandStack();
			if(item == null)
				throw new CmdError("There is no item in your hand.");
			switch(args[0].toLowerCase())
			{
				case "head":
					slot = 5;
					armorSlot = 3;
					break;
				case "chest":
					slot = 6;
					armorSlot = 2;
					break;
				case "legs":
					slot = 7;
					armorSlot = 1;
					break;
				case "feet":
					slot = 8;
					armorSlot = 0;
					break;
				default:
					throw new CmdSyntaxError();
			}
			if(!MC.player.inventory.getArmorStack(armorSlot).isEmpty())	
				throw new CmdError("Item already in slot \"" + args[0] + "\"");
			if(slot > 8 || slot < 5)
				throw new CmdError("Armor slot is invaild.");
			else 
			{
				MC.player.inventory.armor.set(armorSlot, item);
				MC.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(
					slot, item));
			}
			ChatUtils.message("Item placed in slot \"" + args[0] + "\"");
		}else
			throw new CmdSyntaxError();
	}
}
