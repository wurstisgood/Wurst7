/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"autobucket", "auto bucket"})
public final class WaterfallHack extends Hack implements UpdateListener, PostMotionListener
{
	private final CheckboxSetting cobweb = new CheckboxSetting("Webs", 
		true);
	private final CheckboxSetting water = new CheckboxSetting("Water",
		true);
	private final CheckboxSetting slime = new CheckboxSetting("Slime",
		false);
	private final SliderSetting fallDistance = new SliderSetting("Minimum Fall Distance",
		"The minimium distance the player has to fall before the item is placed.",
		3, 3, 15, 1, ValueDisplay.INTEGER);
	
	private final List<Block> blacklist = Arrays.asList(Blocks.AIR, Blocks.WATER, Blocks.LAVA);
	
	private BlockPos place;
	private BlockPos retrievePos;
	private boolean retrieveFaced;
	
	public WaterfallHack()
	{
		super("Waterfall",
			"Protects you from fall damage by placing webs or liquids\n" 
				+ "under you before you land. You can chose what kinds of blocks\n"
				+ "to place. Make sure to have the item in your hand!");
		setCategory(Category.OTHER);
		addSetting(cobweb);
		addSetting(water);
		addSetting(slime);
		addSetting(fallDistance);
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
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		ItemStack item = MC.player.inventory.getMainHandStack();
		if(retrievePos != null && MC.player.isInsideWaterOrBubbleColumn()
			&& item.getItem() == Items.BUCKET)
		{
			BlockUtils.faceBlockSimple(retrievePos);
			retrieveFaced = true;
			return;
		}
		if(!isCorrectItem(item))
			return;
		if(MC.player.fallDistance > fallDistance.getValueI())
		{
			Vec3d playerPos = MC.player.getPos();
			float yValue = getGroundPos(new BlockPos(playerPos), 3, 1);
			if(yValue <= 0)
				return;
			BlockPos pos = new BlockPos(playerPos.getX(), yValue, playerPos.getZ());
			BlockUtils.faceBlockSimple(pos);
			place = pos;
		}
	}
	
	@Override
	public void onPostMotion()
	{
		BlockPos pos;
		boolean retrieving = false;
		if(retrievePos != null && retrieveFaced)
		{
			pos = retrievePos;
			retrieving = true;
		}else
			pos = place;
		if(pos == null)
			return;
		
		Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5)
			.add(new Vec3d(Direction.DOWN.getVector()).multiply(0.5));
		BlockState state = BlockUtils.getState(pos);
		VoxelShape shape = state.getOutlineShape(MC.world, pos);
		BlockHitResult result = MC.world.rayTraceBlock(RotationUtils.getEyesPos(), hitVec, pos, shape, state);
		ItemStack item = MC.player.inventory.getMainHandStack();
		if(!(item.getItem() instanceof BlockItem))
			MC.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND));
		else
			MC.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result));
		MC.player.swingHand(Hand.MAIN_HAND);
		if(retrieving)
		{
			retrievePos = null;
			retrieveFaced = false;
		}else
		{
			if(!(item.getItem() instanceof BlockItem))
				retrievePos = place;
			else
				retrievePos = null;
			place = null;
		}
	}
	
	private boolean isCorrectItem(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		if(stack.getItem() == Items.WATER_BUCKET && water.isChecked())
			return true;
		if(stack.getItem() instanceof BlockItem
			&& ((BlockItem)stack.getItem()).getBlock() == Blocks.COBWEB && cobweb.isChecked())
			return true;
		if(stack.getItem() instanceof BlockItem
			&& ((BlockItem)stack.getItem()).getBlock() == Blocks.SLIME_BLOCK && slime.isChecked())
			return true;
		return false;
	}
	
	private float getGroundPos(BlockPos now, float limit, float offset)
	{
		for(float y = now.getY(); y >= now.getY() - limit; y -= offset)
		{
			BlockPos pos = new BlockPos(now.getX(), y, now.getZ());
			Block block = BlockUtils.getBlock(pos);
			if(pos != null && !blacklist.contains(block))
			{
				if(block == Blocks.COBWEB || block == Blocks.SLIME_BLOCK)
					return -1;
				return y;
			}
		}
		return -1;
	}
}
