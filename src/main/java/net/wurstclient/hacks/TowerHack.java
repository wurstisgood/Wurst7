/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import net.wurstclient.Category;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

public final class TowerHack extends Hack implements UpdateListener
{
	private final CheckboxSetting pressed =
		new CheckboxSetting("Only Tower When Space Pressed", true);
	
	public TowerHack()
	{
		super("Tower",
			"Automatically builds up for you when you press jump.\n"
				+ "If you need a slower version, use ScaffoldWalk.");
		setCategory(Category.BLOCKS);
		addSetting(pressed);
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
		if(!MC.options.keyJump.isPressed() && pressed.isChecked())
			return;
		Vec3d eyesPos = new Vec3d(MC.player.getX(),
			MC.player.getEyeY(), MC.player.getZ());
		Vec3d end = eyesPos.add(0, -4, 0);
		BlockHitResult rayTrace = MC.world.rayTrace(new RayTraceContext(eyesPos, end,
			RayTraceContext.ShapeType.OUTLINE,
			RayTraceContext.FluidHandling.NONE, MC.player));
		if(MC.player != null && MC.player.inventory.getMainHandStack() != null
			&& MC.player.inventory.getMainHandStack().getItem() instanceof BlockItem)
		{
			if(MC.player.onGround)
			{
				Vec3d vel = MC.player.getVelocity();
				MC.player.setVelocity(vel.getX(), 0.41999998688697815D, vel.getZ());
			}
			if(rayTrace.getSide() == Direction.UP
				&& rayTrace.getType() == BlockHitResult.Type.BLOCK)
			{
				WURST.getRotationFaker().faceVectorPacket(rayTrace.getPos());
				IMC.getInteractionManager().rightClickBlock(rayTrace.getBlockPos(),
					rayTrace.getSide(), rayTrace.getPos());
				MC.player.swingHand(Hand.MAIN_HAND);
				Vec3d vel = MC.player.getVelocity();
				MC.player.setVelocity(vel.getX(), 0.41999998688697815D, vel.getZ());
			}
		}
	}
}
