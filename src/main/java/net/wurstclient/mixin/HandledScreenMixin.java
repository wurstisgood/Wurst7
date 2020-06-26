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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import net.wurstclient.WurstClient;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin
{
	@Shadow
	protected Slot focusedSlot;
	
	@Inject(at = {@At("TAIL")},
		method = {"render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"})
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		WurstClient.INSTANCE.getHax().shulkerPeekHack.render(matrices, (HandledScreen<?>)(Object)this, focusedSlot, mouseX, mouseY);
	}
}
