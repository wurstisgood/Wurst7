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

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.wurstclient.Category;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;

public final class ShulkerPeekHack extends Hack
{
	public final CheckboxSetting stackItems = new CheckboxSetting(
		"Merge Identical Items", true);
	private static final Identifier SHULKER_TEXTURE =
		new Identifier("textures/gui/container/shulker_box.png");

	public ShulkerPeekHack()
	{
		super("ShulkerPeek",
			"Allows you to preview shulker boxes in your inventory.");
		setCategory(Category.RENDER);
		addSetting(stackItems);
	}

	public void render(MatrixStack matrices, HandledScreen<?> screen, Slot focused, int mouseX, int mouseY)
	{
		if(!isEnabled())
			return;
		if(focused != null && !focused.getStack().isEmpty())
		{
			CompoundTag tag = focused.getStack().getTag();
			if(tag != null && tag.contains("BlockEntityTag", 10))
			{
				CompoundTag blockTag = tag.getCompound("BlockEntityTag");
				if(blockTag.contains("Items", 9))
					renderShulkerInventory(matrices, screen, blockTag,
						mouseX + 8, mouseY + 24);
			}
		}
	}
	
	private void renderShulkerInventory(MatrixStack matrices, HandledScreen<?> screen, CompoundTag blockTag, int x, int y)
	{
		List<ItemStack> items = getItemsInShulker(blockTag);
		if(!items.isEmpty())
		{
			int size = items.size();
			int slotHeight = size / 9 + (size % 9 == 0 ? 0 : 1);
			drawInventory(matrices, screen, items, x + 7, y - 110 - 18 + 42 + slotHeight * 18);
		}
	}
	
	private List<ItemStack> getItemsInShulker(CompoundTag blockTag)
	{
		List<ItemStack> items = new ArrayList<>();
		DefaultedList<ItemStack> allItems =
			DefaultedList.ofSize(27, ItemStack.EMPTY);
		Inventories.fromTag(blockTag, allItems);
		
		for(ItemStack stack : allItems)
			if(!stack.isEmpty())
			{
				boolean add = true;
				if(stackItems.isChecked())
					for(ItemStack storedStack : items)
						if(stack.isItemEqual(storedStack)
							&& ItemStack.areTagsEqual(storedStack, stack))
						{
							storedStack.setCount(
								stack.getCount() + storedStack.getCount());
							add = false;
						}
				if(add)
					items.add(stack);
			}
		return items;
	}
	
	private void drawInventory(MatrixStack matrices, HandledScreen<?> screen, List<ItemStack> stacks, int width, int height)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		int slotHeight = stacks.size() / 9 + (stacks.size() % 9 == 0 ? 0 : 1);
		int realSlotHeight = slotHeight;
		if(slotHeight == 3)
			slotHeight = 1;
		else if(slotHeight == 1)
			slotHeight = 3;
		
		MC.getTextureManager().bindTexture(SHULKER_TEXTURE);
		GlStateManager.disableDepthTest();
		GlStateManager.disableLighting();
		screen.drawTexture(matrices, width - 8, height + 12 + slotHeight * 18, 0, 0, 176,
			5);
		screen.drawTexture(matrices, width - 8, height + 12 + slotHeight * 18 + 5, 0, 16,
			176, realSlotHeight * 18);
		screen.drawTexture(matrices, width - 8, height + 17 + slotHeight * 18 + realSlotHeight * 18,
			0, 160, 176, 6);
		GlStateManager.enableDepthTest();
		GlStateManager.translatef(0.0F, 0.0F, 32.0F);
		int size = stacks.size();
		
		for(int i = 0; i < size; ++i)
			renderItem(stacks.get(i), i % 9 * 18 + width,
				slotHeight * 18 + (i / 9 + 1) * 18 + height + 1);
		
		GlStateManager.disableLighting();
		MC.getItemRenderer().zOffset = 0;
	}
	
	private void renderItem(ItemStack stack, int x, int y)
	{
		GlStateManager.enableDepthTest();
		MC.getItemRenderer().zOffset = 120;
		DiffuseLighting.enable();
		MC.getItemRenderer().renderInGuiWithOverrides(stack, x, y);
		String count =
			stack.getCount() == 1 ? "" : String.valueOf(stack.getCount());
		String plus = "+" + stack.getCount() % stack.getMaxCount();
		new StringBuilder().append(stack.getCount() / stack.getMaxCount())
			.append("S").append(plus).toString();
		MC.getItemRenderer().renderGuiItemOverlay(MC.textRenderer, stack, x,
			y, count);
		MC.getItemRenderer().zOffset = 0;
	}
	
	public void editShulkerTooltip(ItemStack stack, List<Text> list)
	{
		if(!isEnabled())
			return;
		if(stack.getItem() instanceof BlockItem && ((BlockItem)stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
		{
			List<Text> matched = new ArrayList<>();
			for(Text t : list)
				if(t.getString().matches("^.*\\sx\\d+$"))
					matched.add(t);
				
			for(Text t : matched)
				list.remove(t);
			
			if(list.size() < 2)
				return;
			
			String[] split =
				new TranslatableText("container.shulkerBox.more", "%s").getString().split("%s");
			if(list.get(1).getString().contains(split[0]) && list.get(1).getString().contains(split[1]))
				list.remove(1);
		}
	}
}
