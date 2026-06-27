package ruiseki.okcurios.common.inventory.container;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcurios.OKCurios;
import ruiseki.okcurios.api.CuriosApi;
import ruiseki.okcurios.api.type.capability.ICuriosItemHandler;
import ruiseki.okcurios.api.type.inventory.ICurioStacksHandler;
import ruiseki.okcurios.api.type.inventory.IDynamicStackHandler;
import ruiseki.okcurios.common.inventory.CosmeticCurioSlot;
import ruiseki.okcurios.common.inventory.CurioSlot;
import ruiseki.okcurios.common.network.server.SPacketScroll;

public class CuriosContainer extends ContainerPlayer {

    public final LazyOptional<ICuriosItemHandler> curiosHandler;
    private final EntityPlayer player;

    private final boolean isLocalWorld;

    public int lastScrollIndex = 0;
    private boolean cosmeticColumn = false;

    private static final int CURIOS_START_INDEX = 45;

    public CuriosContainer(InventoryPlayer playerInventory, boolean isRemote, EntityPlayer player) {
        super(playerInventory, isRemote, player);

        this.inventoryItemStacks.clear();
        this.inventorySlots.clear();

        this.player = player;
        this.isLocalWorld = isRemote;
        this.curiosHandler = CuriosApi.getCuriosInventory(player);

        this.addSlotToContainer(
            new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 144, 36));

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + i * 2, 88 + j * 18, 26 + i * 18));
            }
        }

        for (int k = 0; k < 4; ++k) {
            final int armorType = k;
            this.addSlotToContainer(
                new Slot(playerInventory, playerInventory.getSizeInventory() - 1 - k, 8, 8 + k * 18) {

                    @Override
                    public int getSlotStackLimit() {
                        return 1;
                    }

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        if (stack == null) return false;
                        return stack.getItem()
                            .isValidArmor(stack, armorType, player);
                    }

                    @Override
                    @SideOnly(Side.CLIENT)
                    public IIcon getBackgroundIconIndex() {
                        return ItemArmor.func_94602_b(armorType);
                    }
                });
        }

        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlotToContainer(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlotToContainer(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
        }

        this.curiosHandler.ifPresent(curios -> {
            Map<String, ICurioStacksHandler> curioMap = curios.getCurios();
            int slots = 0;
            int yOffset = 12;

            for (String identifier : curioMap.keySet()) {
                ICurioStacksHandler stacksHandler = curioMap.get(identifier);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (stacksHandler.isVisible()) {
                    for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                        this.addSlotToContainer(
                            new CurioSlot(
                                this.player,
                                stackHandler,
                                i,
                                identifier,
                                -18,
                                yOffset,
                                stacksHandler.getRenders(),
                                stacksHandler.canToggleRendering()));
                        yOffset += 18;
                        slots++;
                    }
                }
            }

            yOffset = 12;
            slots = 0;
            for (String identifier : curioMap.keySet()) {
                ICurioStacksHandler stacksHandler = curioMap.get(identifier);
                if (stacksHandler.isVisible() && stacksHandler.hasCosmetic()) {
                    IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
                    for (int i = 0; i < cosmeticHandler.getSlots() && slots < 8; i++) {
                        this.cosmeticColumn = true;
                        this.addSlotToContainer(
                            new CosmeticCurioSlot(this.player, cosmeticHandler, i, identifier, -37, yOffset));
                        yOffset += 18;
                        slots++;
                    }
                }
            }
        });
        this.scrollToIndex(0);
    }

    public boolean hasCosmeticColumn() {
        return this.cosmeticColumn;
    }

    public void resetSlots() {
        this.scrollToIndex(this.lastScrollIndex);
    }

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        if (this.inventorySlots.size() > slotID) {
            super.putStackInSlot(slotID, stack);
        }
    }

    public void scrollToIndex(int indexIn) {

        this.curiosHandler.ifPresent(curios -> {
            Map<String, ICurioStacksHandler> curioMap = curios.getCurios();

            int slots = 0;
            int yOffset = 12;
            int index = 0;
            int startingIndex = indexIn;

            if (this.inventorySlots.size() > CURIOS_START_INDEX) {
                this.inventorySlots.subList(CURIOS_START_INDEX, this.inventorySlots.size())
                    .clear();
            }
            if (this.inventoryItemStacks != null && this.inventoryItemStacks.size() > CURIOS_START_INDEX) {
                this.inventoryItemStacks.subList(CURIOS_START_INDEX, this.inventoryItemStacks.size())
                    .clear();
            }

            for (String identifier : curioMap.keySet()) {
                ICurioStacksHandler stacksHandler = curioMap.get(identifier);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (stacksHandler.isVisible()) {
                    for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                        if (index >= startingIndex) {
                            slots++;
                        }
                        index++;
                    }
                }
            }

            startingIndex = MathHelper.clamp_int(startingIndex, 0, index - 8);
            index = 0;
            slots = 0;

            for (String identifier : curioMap.keySet()) {
                ICurioStacksHandler stacksHandler = curioMap.get(identifier);
                IDynamicStackHandler stackHandler = stacksHandler.getStacks();

                if (stacksHandler.isVisible()) {
                    for (int i = 0; i < stackHandler.getSlots() && slots < 8; i++) {
                        if (index >= startingIndex) {
                            this.addSlotToContainer(
                                new CurioSlot(
                                    this.player,
                                    stackHandler,
                                    i,
                                    identifier,
                                    -18,
                                    yOffset,
                                    stacksHandler.getRenders(),
                                    stacksHandler.canToggleRendering()));
                            yOffset += 18;
                            slots++;
                        }
                        index++;
                    }
                }
            }

            index = 0;
            slots = 0;
            yOffset = 12;

            for (String identifier : curioMap.keySet()) {
                ICurioStacksHandler stacksHandler = curioMap.get(identifier);
                if (stacksHandler.isVisible() && stacksHandler.hasCosmetic()) {
                    IDynamicStackHandler cosmeticHandler = stacksHandler.getCosmeticStacks();
                    for (int i = 0; i < cosmeticHandler.getSlots() && slots < 8; i++) {
                        if (index >= startingIndex) {
                            this.cosmeticColumn = true;
                            this.addSlotToContainer(
                                new CosmeticCurioSlot(this.player, cosmeticHandler, i, identifier, -37, yOffset));
                            yOffset += 18;
                            slots++;
                        }
                        index++;
                    }
                }
            }

            if (!this.isLocalWorld && this.player instanceof EntityPlayerMP playerMP) {
                OKCurios.instance.getPacketHandler()
                    .sendToPlayer(new SPacketScroll(this.windowId, indexIn), playerMP);
            }
            this.lastScrollIndex = indexIn;
        });
    }

    public void scrollTo(float pos) {
        this.curiosHandler.ifPresent(curios -> {
            int k = (curios.getVisibleSlots() - 8);
            int j = (int) (pos * k + 0.5D);

            if (j < 0) {
                j = 0;
            }

            if (j == this.lastScrollIndex) {
                return;
            }

            if (this.isLocalWorld) {
                OKCurios.instance.getPacketHandler()
                    .sendToServer(new SPacketScroll(this.windowId, j));
            }
        });
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (this.player == null || this.player.worldObj == null) return;
        ItemStack craftedResult = CraftingManager.getInstance()
            .findMatchingRecipe(this.craftMatrix, this.player.worldObj);
        this.craftResult.setInventorySlotContents(0, craftedResult);
    }

    @Override
    public void onContainerClosed(EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);
        this.craftResult.setInventorySlotContents(0, (ItemStack) null);

        if (!playerIn.worldObj.isRemote) {
            for (int i = 0; i < 4; ++i) {
                ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
                if (itemstack != null) {
                    playerIn.dropPlayerItemWithRandomChoice(itemstack, false);
                }
            }
        }
    }

    public boolean canScroll() {

        return this.curiosHandler.map(curios -> {

            if (curios.getVisibleSlots() > 8) {
                return 1;
            }
            return 0;
        })
            .orElse(0) == 1;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index == 0) {
                if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (index < 9) {
                if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
                    return null;
                }
            } else if (index < 45) {
                if (CuriosApi.getCuriosInventory(playerIn)
                    .isPresent()
                    && !CuriosApi.getItemStackSlots(itemstack1)
                        .isEmpty()) {
                    if (!this.mergeItemStack(itemstack1, CURIOS_START_INDEX, this.inventorySlots.size(), false)) {
                        return null;
                    }
                } else if (itemstack1.getItem() instanceof ItemArmor armor) {
                    int armorSlot = 5 + (3 - armor.armorType);
                    Slot targetArmorSlot = (Slot) this.inventorySlots.get(armorSlot);
                    if (!targetArmorSlot.getHasStack() && targetArmorSlot.isItemValid(itemstack1)) {
                        if (!this.mergeItemStack(itemstack1, armorSlot, armorSlot + 1, false)) {
                            return null;
                        }
                    } else if (index < 36) {
                        if (!this.mergeItemStack(itemstack1, 36, 45, false)) {
                            return null;
                        }
                    } else {
                        if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
                            return null;
                        }
                    }
                } else if (index < 36) {
                    if (!this.mergeItemStack(itemstack1, 36, 45, false)) {
                        return null;
                    }
                } else {
                    if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
                        return null;
                    }
                }
            } else {
                if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
                    return null;
                }
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack((ItemStack) null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(playerIn, itemstack1);
        }

        return itemstack;
    }
}
