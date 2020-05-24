package net.wurstclient.commands;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;
import net.wurstclient.command.CmdError;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.MathUtils;

public final class EnchantCmd extends Command
{
	public EnchantCmd()
	{
		super("enchant", "Enchants an item with everything,\n"
			+ "except for silk touch and curses.", ".enchant");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(!MC.player.abilities.creativeMode)
			throw new CmdError("Creative mode only.");
		
		if(args.length == 1 && args[0].equalsIgnoreCase("clear")) 
		{
			ItemStack currentItem = MC.player.inventory.getMainHandStack();
			if(currentItem.isEmpty())
				throw new CmdError("There is no item in your hand.");
			
			CompoundTag tag = currentItem.getTag();
			
			if(tag != null && tag.contains("Enchantments")) 
			{
				tag.remove("Enchantments");
				MC.player.networkHandler.sendPacket(
					new CreativeInventoryActionC2SPacket(
						36 + MC.player.inventory.selectedSlot, currentItem));
				ChatUtils.message("Enchantments cleared.");
			}
			return;
		}
		
		boolean allItems;
		Enchantment enchant;
		boolean enchantAll;
		int level;
		boolean max;
		
		if(args.length == 0 || (args.length == 1 && args[0].equals("all")))
		{
			allItems = args.length == 1;
			enchant = null;
			enchantAll = true;
			level = Byte.MAX_VALUE;
			max = false;
		}else
		{
			if(args.length != 3)
				throw new CmdSyntaxError();
			
			if(args[0].equalsIgnoreCase("allitems"))
				allItems = true;
			else if(args[0].equals("hand"))
				allItems = false;
			else
				throw new CmdSyntaxError();
			
			if(args[1].equalsIgnoreCase("all"))
			{
				enchantAll = true;
				enchant = null;
			}else
			{
				enchantAll = false;
				try
				{
					enchant = getEnchantmentFromString(args[1]);
				}catch(InvalidIdentifierException e)
				{
					throw new CmdSyntaxError("Enchantment name is invaild.");
				}
				if(enchant == null)
				{
					if(MathUtils.isInteger(args[1]))
						throw new CmdSyntaxError("Enchantment ID is invaild.");
					else
						throw new CmdSyntaxError("Enchantment name is invaild.");
				}
			}
			
			if(args[2].equalsIgnoreCase("max"))
			{
				max = true;
				level = 0;
			}else if(MathUtils.isInteger(args[2]))
			{
				if(Integer.valueOf(args[2]) < Short.MIN_VALUE || Integer.valueOf(args[2]) > Short.MAX_VALUE)
					throw new CmdError("Enchantments cannot be higher than " + Short.MAX_VALUE + " or less than " +
						Short.MIN_VALUE + ".");
				max = false;
				level = Integer.parseInt(args[2]);
			}else
				throw new CmdSyntaxError();
		}
		
		if(allItems)
		{
			for(int i = 0; i < 41; i++)
			{	
				ItemStack item =
					MC.player.inventory.getInvStack(i);
				if(item.isEmpty())
					continue;
				enchant(item, enchant, enchantAll, level, max);
				MC.player.networkHandler.sendPacket(
					new CreativeInventoryActionC2SPacket(i, item));
			}
			ChatUtils.message("All items enchanted.");
		}else
		{
			ItemStack currentItem = MC.player.inventory.getMainHandStack();
			if(currentItem.isEmpty())
				throw new CmdError("There is no item in your hand.");
			enchant(currentItem, enchant, enchantAll, level, max);
			MC.player.networkHandler.sendPacket(
				new CreativeInventoryActionC2SPacket(
					36 + MC.player.inventory.selectedSlot, currentItem));
			ChatUtils.message("Item enchanted.");
		}
	}
	
	private Enchantment getEnchantmentFromString(String string)
	{
		if(MathUtils.isInteger(string))
			return Enchantment.byRawId(Integer.parseInt(string));
		else
			return Registry.ENCHANTMENT.get(new Identifier(string));
	}
	
	private void enchant(ItemStack stack, Enchantment enchant, boolean enchantAll, int level, boolean max)
	{
		if(enchantAll)
			for(Enchantment ench : Registry.ENCHANTMENT)
			{
				if(ench == Enchantments.SILK_TOUCH)
					continue;
				
				if(ench.isCursed())
					continue;
				
				if(ench == Enchantments.QUICK_CHARGE && !max && level > 5)
					addEnchantmentShort(stack, ench, (short)5);
				else
					addEnchantmentShort(stack, ench, (short)(max ? enchant.getMaximumLevel() : level));
			}
		else
			addEnchantmentShort(stack, enchant, (short)(max ? enchant.getMaximumLevel() : level));
	}
	
	public void addEnchantmentShort(ItemStack stack, Enchantment enchantment, short level)
	{
		stack.getOrCreateTag();
		if(!stack.getTag().contains("Enchantments", 9))
			stack.getTag().put("Enchantments", new ListTag());
		
		ListTag listTag = stack.getTag().getList("Enchantments", 10);
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(enchantment)));
		compoundTag.putShort("lvl", level);
		listTag.add(compoundTag);
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Enchant Held Item";
	}
	
	@Override
	public void doPrimaryAction()
	{
		WURST.getCmdProcessor().process("enchant");
	}
}
