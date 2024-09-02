package com.daboxen.EscapeRoomUtilities;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimedItem extends Item {
	public int timeToLive;

	public TimedItem(Properties props, int timeToLive) {
		super(props);

		this.timeToLive = timeToLive;
	}

	public void inventoryTick(@Nonnull ItemStack pStack, @Nonnull Level pLevel, @Nonnull Entity pEntity, int pSlotId, boolean pIsSelected) {
		CompoundTag tag = new CompoundTag();

		if (tag.contains("time_to_live", Tag.TAG_INT)) {
			tag.putInt("time_to_live", this.timeToLive);
		} else {
			int stackTime = tag.getInt("time_to_live");
			if (stackTime == 0) {
				pStack = pStack.transmuteCopy(this.asItem());
			} else {
				tag.putInt("time_to_live", tag.getInt("time_to_live") - 1);
			}
		}
		
		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
	}
}