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

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.wurstclient.WurstClient;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin
{
	@Inject(at = {@At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applyEquipOffset",
		ordinal = 4)},
		method = {"renderFirstPersonItem"})
	private void lowerShield(AbstractClientPlayerEntity player, float tickDelta, float pitch,
		Hand hand, float swingProgress, ItemStack item, float equipProgress,
		MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci)
	{
		if(WurstClient.INSTANCE.getHax().noShieldHack.isEnabled())
			matrices.translate(0, -0.3, 0);
	}
}
