/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.block.SlimeBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"slime jump", "slimeboost", "slime boost"})
public final class SlimeJumpHack extends Hack implements UpdateListener
{
	public final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.values(), Mode.LOWBOOST);
	public final SliderSetting multiplier = new SliderSetting("Multiplier", 1.8, 0.1, 5, 0.1,
		ValueDisplay.DECIMAL);
	
	private long timer;
	
	public SlimeJumpHack()
	{
		super("SlimeJump", "Changes the way you move when you hit a slime block.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
		addSetting(multiplier);
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
		if(mode.getSelected() == Mode.HIGHBOOST)
		{
			Vec3d vel = MC.player.getVelocity();
			if(MC.player.collided && MC.player.onGround &&
				MC.world.getBlockState(new BlockPos(MC.player).add(0, -0.2, 0)).getBlock() instanceof SlimeBlock)
			{
				MC.player.setVelocity(vel.getX(), 0.5D, vel.getZ());
				if(System.currentTimeMillis() >= timer + 1000)
				{
					MC.player.setVelocity(vel.getX(), multiplier.getValue(), vel.getZ());
					timer = System.currentTimeMillis();
				}
			}
		}
	}
	
	public enum Mode
	{
		NOBOUNCE("NoBounce"),
		LOWBOOST("LowBoost"),
		HIGHBOOST("HighBoost");

		private final String name;

		private Mode(String name)
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
