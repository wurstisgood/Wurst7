/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.wurstclient.WurstClient;

@Mixin(PigEntity.class)
public class PigEntityMixin
{
	@Inject(at = {@At("HEAD")}, method = {"canBeControlledByRider()Z"},
		cancellable = true)
	private void controlEntity(CallbackInfoReturnable<Boolean> ci)
	{
		if(getPrimaryPassenger() instanceof PlayerEntity
			&& WurstClient.INSTANCE.getHax().vehicleHack.shouldAllowControl())
			ci.setReturnValue(true);
	}
	
	@Shadow
	public Entity getPrimaryPassenger()
	{
		return null;
	}
}
