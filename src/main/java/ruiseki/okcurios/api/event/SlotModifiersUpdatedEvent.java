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

import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

import com.google.common.collect.ImmutableSet;

/**
 * {@link SlotModifiersUpdatedEvent} is fired when the slot size is dynamically changed during
 * gameplay through slot modifiers.
 * <br>
 * This event is fired on both the client and the server.
 * <br>
 * {@link #types} contains the affected {@link ruiseki.okcurios.api.type.ISlotType}. <br>
 * <br>
 * This event is not {@link cpw.mods.fml.common.eventhandler.Cancelable}. <br>
 * <br>
 * This event does not have a result. {@link HasResult} <br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 **/
public class SlotModifiersUpdatedEvent extends LivingEvent {

    private final Set<String> types;

    public SlotModifiersUpdatedEvent(EntityLivingBase livingEntity, Set<String> types) {
        super(livingEntity);
        this.types = types;
    }

    public Set<String> getTypes() {
        return ImmutableSet.copyOf(this.types);
    }
}
