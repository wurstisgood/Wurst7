/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IContainer;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;

@SearchTags({"auto steal", "ChestStealer", "chest stealer",
	"steal store buttons", "Steal/Store buttons"})
public final class AutoStealHack extends Hack implements UpdateListener, PostMotionListener
{
	private boolean isStealing;
	private List<BlockEntity> openedChests = new ArrayList<>();
	public final EnumSetting<Mode> mode = new EnumSetting<>("Mode", Mode.values(), Mode.STEAL);
	private final SliderSetting delay = new SliderSetting("Delay",
		"Delay between moving stacks of items.\n"
			+ "Should be at least 70ms for NoCheat+ servers.",
		100, 0, 500, 10, v -> (int)v + "ms");
	
	private final CheckboxSetting autoOpen = new CheckboxSetting("Automatically Open Chests",
		"Warning: There will be no delay between moving stacks.", false);
	private final CheckboxSetting buttons =
		new CheckboxSetting("Steal/Store buttons", true);
	private final CheckboxSetting dropButton =
		new CheckboxSetting("Drop Button", false);
	private final CheckboxSetting shulkers =
		new CheckboxSetting("Steal from Shulkers", false);
	
	public AutoStealHack()
	{
		super("AutoSteal", "Automatically steals everything\n"
			+ "from all chests that you open.");
		setCategory(Category.ITEMS);
		addSetting(buttons);
		addSetting(dropButton);
		addSetting(delay);
		addSetting(mode);
		addSetting(autoOpen);
		addSetting(shulkers);
	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PostMotionListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		isStealing = false;
		openedChests.clear();
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		if(!isStealing && autoOpen.isChecked())
			for(BlockEntity be : MC.world.blockEntities) 
				if(be instanceof ChestBlockEntity ||
					(shulkers.isChecked() && be instanceof ShulkerBoxBlockEntity))
					if(MC.player.squaredDistanceTo(be.getPos().getX(), be.getPos().getY(), be.getPos().getZ()) < 20.0D
						&& MC.currentScreen == null && !openedChests.contains(be))
					{
						isStealing = true;
						WURST.getRotationFaker().faceVectorPacket(new Vec3d(be.getPos()));
						IMC.getInteractionManager().rightClickBlock(be.getPos(), 
							Direction.getFacing((float)MC.player.getX() - be.getPos().getX(), 
								(float)MC.player.getY() - be.getPos().getY(),
								(float)MC.player.getZ() - be.getPos().getZ()),
							new Vec3d(be.getPos()));
						openedChests.add(be);
						break;
					}
	}
	
	@Override
	public void onPostMotion()
	{
		if(autoOpen.isChecked()
			&& (MC.currentScreen instanceof GenericContainerScreen || MC.currentScreen instanceof ShulkerBoxScreen))
		{
			IContainer container = (IContainer)MC.currentScreen;
			container.stealFast();
			MC.player.closeScreen();
			isStealing = false;
		}
	}
	
	public boolean isAutoOpen()
	{
		return autoOpen.isChecked();
	}
	
	public boolean areButtonsVisible()
	{
		return buttons.isChecked();
	}
	
	public boolean isDropButtonVisible()
	{
		return dropButton.isChecked();
	}
	
	public long getDelay()
	{
		return delay.getValueI();
	}
	
	public boolean stealFromShulkers()
	{
		return shulkers.isChecked();
	}
	
	public enum Mode
	{
		STEAL("Steal"),
		DROP("Drop");
		
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
