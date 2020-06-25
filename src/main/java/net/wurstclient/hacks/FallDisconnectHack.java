/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.stream.Stream;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

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
		ClientPlayerEntity player = MC.player;
		if(MC.isInSingleplayer() || player.abilities.creativeMode)
			return;
		if(player.fallDistance > fallDistance.getValueI())
		{
			Vec3d motion = MC.player.getVelocity();
			Stream<VoxelShape> collision =
				MC.world.getCollisions(player,
					player.getBoundingBox().expand(motion.getX(),
						-distGround.getValue(), motion.getZ()), null);
			double y = -distGround.getValue();
			
			y = VoxelShapes.calculateMaxOffset(Direction.Axis.Y, player.getBoundingBox(), collision, y);
			
			if(y != -distGround.getValue())
			{
				if(isInWater(player.getBoundingBox().contract(0.001D), player))
					return;
				MC.world.disconnect();
				setEnabled(false);
			}
		}
	}

	public boolean isInWater(Box bb, Entity entityIn)
	{
		int xMin = MathHelper.floor(bb.x1);
		int xMax = MathHelper.ceil(bb.x2);
		int yMin = MathHelper.floor(bb.y1);
		int yMax = MathHelper.ceil(bb.y2);
		int zMin = MathHelper.floor(bb.z1);
		int zMax = MathHelper.ceil(bb.z2);
		
		if(!MC.world.isRegionLoaded(xMin, yMin, zMin, xMax, yMax, zMax))
			return false;
		
		for(int i = xMin; i < xMax; ++i)
			for(int j = yMin; j < yMax; ++j)
				for(int k = zMin; k < zMax; ++k)
				{
					BlockPos pos = new BlockPos(i, j, k);
					if(MC.world.getFluidState(pos).matches(FluidTags.WATER))
						return true;
				}
		return false;
	}
}
