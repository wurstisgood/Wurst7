/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.wurstclient.WurstClient;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin
{
	@ModifyVariable(method = "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)V",
		at = @At("TAIL"))
	private List<Text> editTooltip(List<Text> list)
	{
		WurstClient.INSTANCE.getHax().shulkerPeekHack.editShulkerTooltip(((ItemStack)(Object)this), list);
		return list;
	}
}
