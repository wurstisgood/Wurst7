/*
 * Copyright (C) 2014 - 2020 | Alexander01998 | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.item.ItemGroup;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyBinding;

@SearchTags({"InventoryWalk", "MenuWalk", "inv walk", "inventory walk",
	"menu walk"})
public final class InvWalkHack extends Hack implements UpdateListener
{
	public InvWalkHack()
	{
		super("InvWalk", "Allows you to walk while the inventory is open.");
		setCategory(Category.MOVEMENT);
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
		KeyBinding[] keybinds = new KeyBinding[]{
			MC.options.keyForward, MC.options.keyBack,
			MC.options.keyLeft, MC.options.keyRight,
			MC.options.keyJump, MC.options.keySprint,
			MC.options.keySneak};
		if(MC.currentScreen instanceof AbstractInventoryScreen
			&& (!(MC.currentScreen instanceof CreativeInventoryScreen)
				|| ((CreativeInventoryScreen)MC.currentScreen).getSelectedTab() != ItemGroup.SEARCH.getIndex()))
			for(KeyBinding bind : keybinds)
				if(((IKeyBinding)bind).getKeyCode().getCategory() == Type.KEYSYM)
					bind.setPressed(((IKeyBinding)bind).isActallyPressed());
				else if(((IKeyBinding)bind).getKeyCode().getCategory() == Type.MOUSE)
					bind.setPressed(GLFW.glfwGetMouseButton(MC.getWindow().getHandle(),
						((IKeyBinding)bind).getKeyCode().getKeyCode()) == 1);
	}
}
