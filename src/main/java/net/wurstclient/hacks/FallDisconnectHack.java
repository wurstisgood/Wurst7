/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.AbstractMap;
import java.util.Map.Entry;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;

public final class FallDisconnectHack extends Hack implements UpdateListener
{
	private final SliderSetting fallDistance = new SliderSetting("Minimum Fall Distance",
		"The minimium distance the player has to fall before the item is placed.",
		6, 3, 15, 1, ValueDisplay.INTEGER);
	private final SliderSetting distGround = new SliderSetting("Distance From Ground",
		"The distance from ground to trigger the disconnect.",
		3, 2, 6, 1, ValueDisplay.INTEGER);
	
	public FallDisconnectHack()
	{
		super("FallDisconnect",
			"Disconnects when falling large distances to prevent fall damage.");
		setCategory(Category.OTHER);
		addSetting(fallDistance);
		addSetting(distGround);
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
		if(MC.isInSingleplayer() || MC.player.abilities.creativeMode)
			return;
		if(MC.player.fallDistance > fallDistance.getValueI())
		{
			Entry<Float, Block> result = getDistanceFromGround(new BlockPos(MC.player.getPos()), distGround.getValueI(), 1);
			if(result.getKey() < 0)
				return;
			if(result.getValue() == Blocks.WATER)
				return;
			if(result.getKey() <= distGround.getValueF())
			{
				MC.world.disconnect();
				setEnabled(false);
			}
		}
	}
	
	private Entry<Float, Block> getDistanceFromGround(BlockPos currentpos, float limit, float offset)
	{
		for(float y = currentpos.getY(); y >= currentpos.getY() - limit; y -= offset)
		{
			BlockPos pos = new BlockPos(currentpos.getX(), y, currentpos.getZ());
			if(pos != null)
			{
				Block block = BlockUtils.getBlock(pos);
				if(!(block instanceof AirBlock))
					return new AbstractMap.SimpleEntry<Float, Block>(currentpos.getY() - y, block);
			}
		}
		return new AbstractMap.SimpleEntry<Float, Block>(-1F, null);
	}
}
