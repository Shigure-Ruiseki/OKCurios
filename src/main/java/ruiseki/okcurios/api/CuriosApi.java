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

import ruiseki.okcurios.api.type.helper.ICuriosHelper;
import ruiseki.okcurios.api.type.helper.IIconHelper;
import ruiseki.okcurios.api.type.helper.ISlotHelper;

public final class CuriosApi {

    private static IIconHelper iconHelper;
    private static ISlotHelper slotHelper;
    private static ICuriosHelper curiosHelper;

    public static IIconHelper getIconHelper() {
        return iconHelper;
    }

    public static ISlotHelper getSlotHelper() {
        return slotHelper;
    }

    public static ICuriosHelper getCuriosHelper() {
        return curiosHelper;
    }

    public static void setIconHelper(IIconHelper helper) {
        if (iconHelper == null) {
            iconHelper = helper;
        }
    }

    public static void setSlotHelper(ISlotHelper helper) {
        slotHelper = helper;
    }

    public static void setCuriosHelper(ICuriosHelper helper) {
        if (curiosHelper == null) {
            curiosHelper = helper;
        }
    }
}
