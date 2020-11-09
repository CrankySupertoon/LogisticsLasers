package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.PolyFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.StockerFilterContainer;
import com.direwolf20.logisticslasers.common.container.cards.TagFilterContainer;
import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import com.direwolf20.logisticslasers.common.items.logiccards.CardInserterTag;
import com.direwolf20.logisticslasers.common.items.logiccards.CardPolymorph;
import com.direwolf20.logisticslasers.common.items.logiccards.CardStocker;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.function.Supplier;

import static com.direwolf20.logisticslasers.common.items.logiccards.BaseCard.getInventory;

public class PacketOpenFilter {
    private int slotNumber;
    private BlockPos sourcePos;

    public PacketOpenFilter(int slotNumber, BlockPos pos) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
    }

    public static void encode(PacketOpenFilter msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
    }

    public static PacketOpenFilter decode(PacketBuffer buffer) {
        return new PacketOpenFilter(buffer.readInt(), buffer.readBlockPos());

    }

    public static class Handler {
        public static void handle(PacketOpenFilter msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                Slot slot = container.inventorySlots.get(msg.slotNumber);
                ItemStack itemStack = slot.getStack();
                IIntArray tempArray;
                ItemStackHandler handler = getInventory(itemStack);
                if (itemStack.getItem() instanceof CardStocker) {
                    tempArray = new IIntArray() {
                        @Override
                        public int get(int index) {
                            if (index == 0)
                                return BaseCard.getPriority(itemStack);
                            else if (index < 16)
                                return BaseCard.getInventory(itemStack).getStackInSlot(index - 1).getCount();
                            else
                                throw new IllegalArgumentException("Invalid index: " + index);
                        }

                        @Override
                        public void set(int index, int value) {
                            throw new IllegalStateException("Cannot set values through IIntArray");
                        }

                        @Override
                        public int size() {
                            return 16;
                        }
                    };
                } else {
                    tempArray = new IIntArray() {
                        @Override
                        public int get(int index) {
                            switch (index) {
                                case 0:
                                    return BaseCard.getPriority(itemStack);
                                case 1:
                                    return BaseCard.getExtractAmt(itemStack);
                                default:
                                    throw new IllegalArgumentException("Invalid index: " + index);
                            }
                        }

                        @Override
                        public void set(int index, int value) {
                            throw new IllegalStateException("Cannot set values through IIntArray");
                        }

                        @Override
                        public int size() {
                            return 2;
                        }
                    };
                }
                if (itemStack.getItem() instanceof CardStocker) {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new StockerFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                    }));
                } else if (itemStack.getItem() instanceof CardInserterTag) {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new TagFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                    }));
                } else if (itemStack.getItem() instanceof CardPolymorph) {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new PolyFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                        buf.writeBlockPos(msg.sourcePos);
                        buf.writeInt(msg.slotNumber);
                    }));
                } else {
                    NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                            (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos, tempArray), new StringTextComponent("")), (buf -> {
                        buf.writeItemStack(itemStack);
                    }));
                }
                //}
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
