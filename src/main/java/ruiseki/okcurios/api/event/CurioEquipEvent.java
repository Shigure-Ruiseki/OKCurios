/*
 * Copyright (c) 2018-2020 C4
 * This file is part of Curios, a mod made for Minecraft.
 * Curios is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Curios is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with Curios. If not, see <https://www.gnu.org/licenses/>.
 */
package ruiseki.okcurios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

import cpw.mods.fml.common.eventhandler.Event;
import ruiseki.okcurios.api.SlotContext;

/**
 * CurioEquipEvent is fired when a curio item is about to be equipped and allows an event listener
 * to specify whether it should or not. <br>
 * This event is fired when ever the
 * {@link ruiseki.okcurios.api.type.capability.ICurio#canEquip(String, EntityLivingBase)}
 * is checked. <br>
 * <br>
 * This event has a {@link HasResult result}:
 * <li>{@link Result#ALLOW} means the curio item can be equipped.</li>
 * <li>{@link Result#DEFAULT} means the item tags and
 * {@link ruiseki.okcurios.api.type.capability.ICurio#canEquip(String, EntityLivingBase)}
 * determines the result.</li>
 * <li>{@link Result#DENY} means the curio item cannot be equipped.</li><br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
@Event.HasResult
public class CurioEquipEvent extends LivingEvent {

    private final SlotContext slotContext;
    private final ItemStack stack;

    public CurioEquipEvent(ItemStack stack, SlotContext slotContext) {
        super(slotContext.wearer());
        this.slotContext = slotContext;
        this.stack = stack;
    }

    public SlotContext getSlotContext() {
        return slotContext;
    }

    public ItemStack getStack() {
        return stack;
    }
}
