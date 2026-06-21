package ruiseki.okcurios.common.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityManager;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.init.IInitListener;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.type.capability.ICurio;

public class CurioItemCapability implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step != IInitListener.Step.PREINIT) return;
        CapabilityManager.INSTANCE.register(ICurio.class, new Capability.IStorage<ICurio>() {

            @Override
            public @NotNull NBTBase writeNBT(Capability<ICurio> capability, ICurio iCurio,
                ForgeDirection forgeDirection) {
                return new NBTTagCompound();
            }

            @Override
            public void readNBT(Capability<ICurio> capability, ICurio iCurio, ForgeDirection forgeDirection,
                NBTBase nbtBase) {
                // Đọc dữ liệu nếu cần
            }
        }, CurioItemWrapper::new);
    }

    public static ICapabilityProvider createProvider(final ICurio curio) {
        return new Provider(curio);
    }

    private static class CurioItemWrapper implements ICurio {

    }

    public static class Provider implements ICapabilityProvider {

        final ICurio capability;

        Provider(ICurio curio) {
            this.capability = curio;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, ForgeDirection facing) {
            return capability == CuriosCapability.ITEM;
        }

        @SuppressWarnings("unchecked")
        @Override
        public @Nullable <T> T getCapability(@NotNull Capability<T> capability, ForgeDirection facing) {
            return hasCapability(capability, facing) ? (T) this.capability : null;
        }
    }
}
