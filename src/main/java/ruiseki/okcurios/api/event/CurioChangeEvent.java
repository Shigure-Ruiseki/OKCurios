package ruiseki.okcurios.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

import org.jetbrains.annotations.NotNull;

/**
 * CurioChangeEvent is fired when the Curio of a LivingEntity changes. <br>
 * This event is
 * fired whenever changes in curios are detected in
 * <br>
 * {@link net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent}.
 * <br>
 * This also includes entities joining the World, as well as being cloned. <br>
 * This event is
 * fired on server-side only. <br>
 * <br>
 * {@link #type} contains the affected {@link ruiseki.okcurios.api.type.ISlotType}. <br>
 * {@link #from} contains the {@link ItemStack} that was equipped previously.
 * <br>
 * {@link #to} contains the {@link ItemStack} that is equipped now. <br>
 * {@link #index} contains the
 * index of the curio slot
 * <br>
 * This event is not {@link cpw.mods.fml.common.eventhandler.Cancelable}. <br>
 * <br>
 * This event does not have a result. {@link HasResult} <br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 **/
public class CurioChangeEvent extends LivingEvent {

    private final String type;
    private final ItemStack from;
    private final ItemStack to;
    private final int index;

    public CurioChangeEvent(EntityLivingBase entity, String type, int index, @NotNull ItemStack from,
        @NotNull ItemStack to) {
        super(entity);
        this.type = type;
        this.from = from;
        this.to = to;
        this.index = index;
    }

    public String getIdentifier() {
        return this.type;
    }

    public int getSlotIndex() {
        return this.index;
    }

    @NotNull
    public ItemStack getFrom() {
        return this.from;
    }

    @NotNull
    public ItemStack getTo() {
        return this.to;
    }
}
