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
package ruiseki.okcurios.api.type.helper;

import java.util.Collection;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;

import ruiseki.okcurios.api.type.ISlotType;

public interface ISlotHelper {

    /**
     * Registers a {@link ISlotType} instance.
     * <br>
     * Modders: DO NOT USE DIRECTLY - Use IMC to send the appropriate SlotTypeMessage
     *
     * @param slotType The {@link ISlotType} instance
     */
    void addSlotType(ISlotType slotType);

    /**
     * Gets the {@link ISlotType} registered to the given identifier, or null if none is registered.
     *
     * @param identifier The {@link ISlotType} identifier
     * @return The {@link ISlotType} registered to the identifier, or null if not found
     */
    ISlotType getSlotType(String identifier);

    /**
     * @return A collection of all registered {@link ISlotType}
     */
    Collection<ISlotType> getSlotTypes();

    /**
     * @return A collection of all registered {@link ISlotType} for a specific entity
     */
    Collection<ISlotType> getSlotTypes(EntityLivingBase livingEntity);

    /**
     * Gets all unique registered {@link ISlotType} identifiers.
     *
     * @return A set of identifiers
     */
    Set<String> getSlotTypeIds();

    /**
     * Retrieves the number of slots that an entity has for a specific curio type.
     *
     * @param livingEntity The holder of the slot(s) as a {@link EntityLivingBase}
     * @param id           The identifier of the {@link ISlotType}
     * @return The number of slots
     */
    int getSlotsForType(EntityLivingBase livingEntity, String id);

    /**
     * Sets the number of slots that an entity has for a specific curio type.
     *
     * @param id           The identifier of the {@link ISlotType}
     * @param livingEntity The holder of the slot(s) as a {@link EntityLivingBase}
     * @param amount       The number of slots
     */
    void setSlotsForType(String id, EntityLivingBase livingEntity, int amount);

    /**
     * Adds a {@link ISlotType} to the entity with default settings.
     *
     * @param id           The identifier of the {@link ISlotType}
     * @param livingEntity The holder of the slot(s) as a {@link EntityLivingBase}
     */
    void unlockSlotType(String id, EntityLivingBase livingEntity);

    /**
     * Removes a {@link ISlotType} from the entity.
     *
     * @param id           The identifier of the {@link ISlotType}
     * @param livingEntity The holder of the slot(s) as a {@link EntityLivingBase}
     */
    void lockSlotType(String id, EntityLivingBase livingEntity);
}
