/*
 * Copyright © 2014 - 2018 | Wurst-Imperium | All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.wurstclient.hacks;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"BoatFly", "HorseJump"})
public final class VehicleHack extends Hack implements UpdateListener
{
	private final CheckboxSetting jump =
		new CheckboxSetting("Horses Always Make Highest Jump", true);
	private final CheckboxSetting control =
		new CheckboxSetting("Always Allow Steering of Animals", true);
	private final CheckboxSetting fly =
		new CheckboxSetting("Enable Flight when Riding Boat", false);
	private final CheckboxSetting swim =
		new CheckboxSetting("Prevent Forced Dismounting When Swimming", false);
	private final CheckboxSetting speed =
		new CheckboxSetting("Enable Vehicle Speedhack", false);
	private final SliderSetting multiplier = new SliderSetting(
		"Vehicle Speed Multiplier", "Only works when vehicle speedhack is enabled.",
		1, 0.1, 20, 0.1, ValueDisplay.DECIMAL);

	public VehicleHack()
	{
		super("Vehicle", "Allows for non-vanilla behavior in vehicles");
		setCategory(Category.MOVEMENT);
		addSetting(jump);
		addSetting(control);
		addSetting(fly);
		addSetting(swim);
		addSetting(speed);
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
		if(!MC.player.hasVehicle())
			return;
		Entity riding = MC.player.getVehicle();
		Vec3d vel = riding.getVelocity();
		if(swim.isChecked() && riding.isInsideWaterOrBubbleColumn()
			&& riding instanceof LivingEntity)
			riding.setVelocity(vel.getX(), 0.2, vel.getZ());
		if(fly.isChecked())
		{
			riding.setVelocity(vel.getX(), 0, vel.getZ());
			if(MC.options.keyJump.isPressed())
				riding.setVelocity(vel.getX(), 0.3, vel.getZ());
			if(MC.options.keyBack.isPressed())
				riding.setVelocity(vel.getX(), -0.3, vel.getZ());
		}
		vel = riding.getVelocity();
		if(speed.isChecked() && (riding instanceof LivingEntity
			|| riding instanceof BoatEntity))
		{
			double base = riding instanceof LivingEntity ? getBaseMoveSpeed((LivingEntity)riding) : 0.2873;
			double[] movement = getMovement(multiplier.getValue(),
				MC.player.forwardSpeed,
				MC.player.sidewaysSpeed,
				MC.player.yaw);
			riding.setVelocityClient(movement[0] * base, vel.getY(), movement[1] * base);
            if(riding instanceof BoatEntity)
            	riding.yaw = MC.player.yaw;
		}
	}

	public double getBaseMoveSpeed(LivingEntity entity)
	{
		double baseSpeed = 0.2873;
		if(entity.hasStatusEffect(StatusEffects.SPEED))
		{
			int amplifier = entity.getStatusEffect(StatusEffects.SPEED).getAmplifier();
			baseSpeed *= 1.0D + 0.2D * (amplifier + 1);
		}
		return baseSpeed;
	}

	public static double[] getMovement(double multiplier, double forward, double strafing,
		float yaw)
	{
		double moveX = 0;
		double moveZ = 0;
		if(forward != 0 || strafing != 0)
		{
			if(forward != 0)
			{
				yaw = (float)(yaw
					+ (strafing > 0 ? 1 : strafing < 0 ? -1 : 0)
						* (forward > 0 ? -45 : 45));
				forward = forward > 0 ? 1 : forward < 0 ? -1 : 0;
				strafing = 0;
			}
			yaw = (float)Math.toRadians(yaw + 90);
			moveX =
				forward * multiplier * MathHelper.cos(yaw) + strafing * multiplier * MathHelper.sin(yaw);
			moveZ =
				forward * multiplier * MathHelper.sin(yaw) - strafing * multiplier * MathHelper.cos(yaw);
		}
		return new double[]{moveX, moveZ};
	}
	
	public boolean shouldModHorseJump()
	{
		return isEnabled() && jump.isChecked();
	}
	
	public boolean shouldAllowControl()
	{
		return isEnabled() && control.isChecked();
	}
}
