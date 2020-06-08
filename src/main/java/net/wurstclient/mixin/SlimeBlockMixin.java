/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.SlimeBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.SlimeJumpHack;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin
{
	@Inject(at = {@At("HEAD")},
		method = {
			"method_21847(Lnet/minecraft/entity/Entity;)V"},
		cancellable = true)
	private void onSlimeMotion(Entity entity, CallbackInfo ci)
	{
		SlimeJumpHack slimeJumpHack = WurstClient.INSTANCE.getHax().slimeJumpHack;
		Vec3d vel = entity.getVelocity();
		if(vel.getY() < 0.0D && slimeJumpHack.isEnabled() && entity instanceof ClientPlayerEntity)
        {
        	double motion = -vel.getY();
	        	switch(slimeJumpHack.mode.getSelected())
	        	{
	        		case NOBOUNCE:
	        			motion = 0;
	        			break;
	        		case LOWBOOST:
	        			motion = motion * 1.1;
	        			break;
					default:
						break;		
	        	}
        	entity.setVelocity(vel.getX(), motion, vel.getZ());
        	ci.cancel();
        }
	}
}
