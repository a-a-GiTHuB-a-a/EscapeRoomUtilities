package com.daboxen.EscapeRoomUtilities;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimedItem extends Item {
	public int timeToLive;
	public Supplier<Item> convertsTo;

	public TimedItem(Properties props, int timeToLive, Supplier<Item> convertsTo) {
		super(props);

		this.timeToLive = timeToLive;
		this.convertsTo = convertsTo;
	}

	public void inventoryTick(@Nonnull ItemStack pStack, @Nonnull Level pLevel, @Nonnull Entity pEntity, int pSlotId, boolean pIsSelected) {
		@Nullable Integer itemTime = pStack.get(EscapeRoomUtilities.TIME_TO_LIVE);

		if (itemTime == null) {
			pStack.set(EscapeRoomUtilities.TIME_TO_LIVE, this.timeToLive);
		} else {
			if (itemTime == 0) {
				pStack = pStack.transmuteCopy(convertsTo.get());
			} else {
				pStack.set(EscapeRoomUtilities.TIME_TO_LIVE, --itemTime);
			}
		}
		
		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
	}
}