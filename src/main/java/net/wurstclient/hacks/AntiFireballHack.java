/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.Comparator;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;
import net.wurstclient.Category;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.RotationUtils;

public final class AntiFireballHack extends Hack
	implements UpdateListener, PostMotionListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 10, 0.05, ValueDisplay.DECIMAL);
	
	private final EnumSetting<Priority> priority = new EnumSetting<>("Priority",
		"Determines which fireball will be attacked first.\n"
			+ "\u00a7lDistance\u00a7r - Attacks the closest fireball.\n"
			+ "\u00a7lAngle\u00a7r - Attacks the fireball that requires\n"
			+ "the least head movement.",
		Priority.values(), Priority.ANGLE);
	
	private final CheckboxSetting ignoreCooldown =
		new CheckboxSetting("Ignore Cooldown", true);
	
	private FireballEntity target;
	
	public AntiFireballHack()
	{
		super("AntiFireball", "Automatically deflects fireballs.");
		setCategory(Category.COMBAT);
		addSetting(range);
		addSetting(priority);
		addSetting(ignoreCooldown);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PostMotionListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PostMotionListener.class, this);
		
		target = null;
	}
	
	@Override
	public void onUpdate()
	{
		ClientPlayerEntity player = MC.player;
		ClientWorld world = MC.world;
		
		if(player.getAttackCooldownProgress(0) < 1 && !ignoreCooldown.isChecked())
			return;
		
		double rangeSq = Math.pow(range.getValue(), 2);
		Stream<FireballEntity> stream = StreamSupport
			.stream(world.getEntities().spliterator(), true)
			.filter(e -> e instanceof FireballEntity).map(e -> (FireballEntity)e)
			.filter(e -> player.squaredDistanceTo(e) <= rangeSq);
		
		target = stream.min(priority.getSelected().comparator).orElse(null);
		if(target == null)
			return;
		
		WURST.getHax().autoSwordHack.setSlot();
		
		WURST.getRotationFaker()
			.faceVectorPacket(target.getBoundingBox().getCenter());
	}
	
	@Override
	public void onPostMotion()
	{
		if(target == null)
			return;
		
		ClientPlayerEntity player = MC.player;
		MC.interactionManager.attackEntity(player, target);
		player.swingHand(Hand.MAIN_HAND);
		
		target = null;
	}
	
	private enum Priority
	{
		DISTANCE("Distance", e -> MC.player.squaredDistanceTo(e)),
		
		ANGLE("Angle",
			e -> RotationUtils
				.getAngleToLookVec(e.getBoundingBox().getCenter()));
		
		private final String name;
		private final Comparator<FireballEntity> comparator;
		
		private Priority(String name,
			ToDoubleFunction<FireballEntity> keyExtractor)
		{
			this.name = name;
			comparator = Comparator.comparingDouble(keyExtractor);
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
}
