package ruiseki.okcurios.common.capability;

import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.CapabilityManager;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.init.IInitListener;
import ruiseki.okcurios.api.CuriosCapability;
import ruiseki.okcurios.api.type.capability.ICurio;

public class CurioItemCapability implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step != IInitListener.Step.PREINIT) return;
        CapabilityManager.INSTANCE.register(ICurio.class);
    }

    public static ICapabilityProvider createProvider(final ICurio curio) {
        return new Provider(curio);
    }

    public static class Provider implements ICapabilityProvider {

        final LazyOptional<ICurio> capability;

        Provider(ICurio curio) {
            this.capability = LazyOptional.of(() -> curio);
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable ForgeDirection side) {
            return CuriosCapability.ITEM.orEmpty(cap, this.capability);
        }
    }
}
