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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.mob.ZombiePigmanEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;
import net.wurstclient.util.BlockBreaker;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.FakePlayerEntity;

public final class IgniteHack extends Hack implements UpdateListener, PostMotionListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 4, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting filterPlayers = new CheckboxSetting(
		"Filter players", "Won't attack other players.", false);
	private final CheckboxSetting filterSleeping = new CheckboxSetting(
		"Filter sleeping", "Won't attack sleeping players.", false);
	private final SliderSetting filterFlying =
		new SliderSetting("Filter flying",
			"Won't attack players that\n" + "are at least the given\n"
				+ "distance above ground.",
			0, 0, 2, 0.05,
			v -> v == 0 ? "off" : ValueDisplay.DECIMAL.getValueString(v));
	
	private final CheckboxSetting filterMonsters = new CheckboxSetting(
		"Filter monsters", "Won't attack zombies, creepers, etc.", false);
	private final CheckboxSetting filterPigmen = new CheckboxSetting(
		"Filter pigmen", "Won't attack zombie pigmen.", false);
	private final CheckboxSetting filterEndermen =
		new CheckboxSetting("Filter endermen", "Won't attack endermen.", false);
	
	private final CheckboxSetting filterAnimals = new CheckboxSetting(
		"Filter animals", "Won't attack pigs, cows, etc.", false);
	private final CheckboxSetting filterBabies =
		new CheckboxSetting("Filter babies",
			"Won't attack baby pigs,\n" + "baby villagers, etc.", false);
	private final CheckboxSetting filterPets =
		new CheckboxSetting("Filter pets",
			"Won't attack tamed wolves,\n" + "tamed horses, etc.", false);
	
	private final CheckboxSetting filterVillagers = new CheckboxSetting(
		"Filter villagers", "Won't attack villagers.", false);
	private final CheckboxSetting filterGolems =
		new CheckboxSetting("Filter golems",
			"Won't attack iron golems,\n" + "snow golems and shulkers.", false);
	
	private final CheckboxSetting filterInvisible = new CheckboxSetting(
		"Filter invisible", "Won't attack invisible entities.", false);
	
	private final CheckboxSetting hand = new CheckboxSetting(
		"Only Ignite When In Hand", false);
	
	private List<Entity> entities = new ArrayList<>();
	private int index;
	private BlockPos hit;
	private int oldItem;
	private BlockPos smashPos;
	private boolean resetBlock;
	
	private long timer;

	public IgniteHack()
	{
		super("Ignite",
			"If you have a flint and steel in your hotbar,\n"
				+ "you will ignite all enemies around you.");
		setCategory(Category.COMBAT);
		addSetting(hand);
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
		int slot = findFlintAndSteel();
		if(slot > -1)
		{
			ClientPlayerEntity player = MC.player;
			ClientWorld world = MC.world;
			double rangeSq = Math.pow(range.getValue(), 2);
			Stream<LivingEntity> stream = StreamSupport
				.stream(MC.world.getEntities().spliterator(), true)
				.filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity)e)
				.filter(e -> !e.removed && e.getHealth() > 0)
				.filter(e -> player.squaredDistanceTo(e) <= rangeSq)
				.filter(e -> e != player)
				.filter(e -> !(e instanceof FakePlayerEntity))
				.filter(e -> !WURST.getFriends().contains(e.getEntityName()));
			
			if(filterPlayers.isChecked())
				stream = stream.filter(e -> !(e instanceof PlayerEntity));
			
			if(filterSleeping.isChecked())
				stream = stream.filter(e -> !(e instanceof PlayerEntity
					&& ((PlayerEntity)e).isSleeping()));
			
			if(filterFlying.getValue() > 0)
				stream = stream.filter(e -> {
					
					if(!(e instanceof PlayerEntity))
						return true;
					
					Box box = e.getBoundingBox();
					box = box.union(box.offset(0, -filterFlying.getValue(), 0));
					return world.doesNotCollide(box);
				});
			
			if(filterMonsters.isChecked())
				stream = stream.filter(e -> !(e instanceof Monster));
			
			if(filterPigmen.isChecked())
				stream = stream.filter(e -> !(e instanceof ZombiePigmanEntity));
			
			if(filterEndermen.isChecked())
				stream = stream.filter(e -> !(e instanceof EndermanEntity));
			
			if(filterAnimals.isChecked())
				stream = stream.filter(
					e -> !(e instanceof AnimalEntity || e instanceof AmbientEntity
						|| e instanceof WaterCreatureEntity));
			
			if(filterBabies.isChecked())
				stream = stream.filter(e -> !(e instanceof PassiveEntity
					&& ((PassiveEntity)e).isBaby()));
			
			if(filterPets.isChecked())
				stream = stream
					.filter(e -> !(e instanceof TameableEntity
						&& ((TameableEntity)e).isTamed()))
					.filter(e -> !(e instanceof HorseBaseEntity
						&& ((HorseBaseEntity)e).isTame()));
			
			if(filterVillagers.isChecked())
				stream = stream.filter(e -> !(e instanceof VillagerEntity));
			
			if(filterGolems.isChecked())
				stream = stream.filter(e -> !(e instanceof GolemEntity));
			
			if(filterInvisible.isChecked())
				stream = stream.filter(e -> !e.isInvisible());
			
			entities = stream.collect(Collectors.toList());
			
			if(entities.size() <= 0)
				return;
			if(index >= entities.size())
				index = 0;
			Entity entity = entities.get(index);
			if(entity != null)
			{
				Vec3d entityVec = entity.getPos();
				BlockPos blockPosBelow = new BlockPos(entityVec.getX(), entityVec.getY() - 1.0D, entityVec.getZ());
				BlockPos entityPos = new BlockPos(entity);
				Block blockBelow = BlockUtils.getBlock(blockPosBelow);
				Block block = BlockUtils.getBlock(entityPos);
				if(blockBelow != null && !(blockBelow instanceof AirBlock))
				{
					if(block == Blocks.FIRE || BlockUtils.getHardness(entityPos) < 1)
						return;
					if(!(block instanceof AirBlock))
					{
						smashPos = entityPos;
						BlockUtils.faceBlockSimple(entityPos);
						return;
					}
					if(resetBlock)
					{
						MC.interactionManager.cancelBlockBreaking();
						resetBlock = false;
					}
					if(hand.isChecked()
						&& !(MC.player.inventory.getMainHandStack().getItem() instanceof FlintAndSteelItem))
						return;
					oldItem = MC.player.inventory.selectedSlot;
					if(!hand.isChecked())
						MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
					BlockUtils.faceBlockSimple(blockPosBelow);
					hit = blockPosBelow;
				}else
				{
					incrementIndex();
					timer = System.currentTimeMillis();
				}
			}
			if(System.currentTimeMillis() >= timer + 250)
			{
				incrementIndex();
				timer = System.currentTimeMillis();
			}
		}
	}
	
	@Override
	public void onPostMotion()
	{
		if(smashPos != null)
		{
			BlockBreaker.breakOneBlock(smashPos);
			smashPos = null;
			resetBlock = true;
		}
		if(hit == null)
			return;
		Vec3d hitVec = new Vec3d(hit).add(0.5, 0.5, 0.5)
			.add(new Vec3d(Direction.DOWN.getVector()).multiply(0.5));
		MC.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(
			Hand.MAIN_HAND, new BlockHitResult(hitVec, Direction.UP, hit, false)));
		MC.player.swingHand(Hand.MAIN_HAND);
		if(!hand.isChecked())
			MC.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldItem));
		hit = null;
		oldItem = -1;
	}
	
	private int findFlintAndSteel()
	{
		for(int i = 0; i < 9; i++)
		{
			ItemStack itemStack = MC.player.inventory.getInvStack(i);
			if(itemStack.getItem() instanceof FlintAndSteelItem)
				return i;
		}
		return -1;
	}
	
	private void incrementIndex()
	{
		index++;
		if(index >= entities.size() - 1)
			index = 0;
	}
}
