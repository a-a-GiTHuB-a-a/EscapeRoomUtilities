package com.daboxen.EscapeRoomUtilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
		@Nullable Integer itemTime = pStack.get(EscapeRoomUtilities.TIME_TO_LIVE);

		if (itemTime == null) {
			pStack.set(EscapeRoomUtilities.TIME_TO_LIVE, this.timeToLive);
		} else {
			if (itemTime == 0) {
				pStack = pStack.transmuteCopy(this.asItem());
			} else {
				pStack.set(EscapeRoomUtilities.TIME_TO_LIVE, --itemTime);
			}
		}
		
		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
	}
}