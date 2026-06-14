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
package ruiseki.okcurios.api;

import net.minecraft.util.ResourceLocation;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityInject;
import ruiseki.okcurios.Reference;
import ruiseki.okcurios.api.type.capability.ICurio;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;

public class CuriosCapability {

    @CapabilityInject(ICuriosItemHandler.class)
    public static final Capability<ICuriosItemHandler> INVENTORY = null;

    @CapabilityInject(ICurio.class)
    public static final Capability<ICurio> ITEM = null;

    public static final ResourceLocation ID_INVENTORY = new ResourceLocation(Reference.MOD_ID, "inventory");
    public static final ResourceLocation ID_ITEM = new ResourceLocation(Reference.MOD_ID, "item");
}
