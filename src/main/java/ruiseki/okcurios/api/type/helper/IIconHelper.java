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

import net.minecraft.util.ResourceLocation;

public interface IIconHelper {

    /**
     * Clears all of the registered icons.
     */
    void clearIcons();

    /**
     * Adds a {@link ResourceLocation} for the icon to the given slot type identifier.
     *
     * @param identifier       The slot type identifier
     * @param resourceLocation The {@link ResourceLocation} for to the icon
     */
    void addIcon(String identifier, ResourceLocation resourceLocation);

    /**
     * Gets the resource location of the icon registered to the identifier.
     *
     * @param identifier The identifier of the slot type
     * @return The resource location of the icon registered to the identifier
     */
    ResourceLocation getIcon(String identifier);
}
