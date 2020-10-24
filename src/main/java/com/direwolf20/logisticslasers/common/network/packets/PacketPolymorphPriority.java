package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.items.logiccards.BaseCard;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketPolymorphPriority {
    private int slotNumber;
    private BlockPos sourcePos;
    private int change;

    public PacketPolymorphPriority(int slotNumber, BlockPos pos, int change) {
        this.slotNumber = slotNumber;
        this.sourcePos = pos;
        this.change = change;
    }

    public static void encode(PacketPolymorphPriority msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeBlockPos(msg.sourcePos);
        buffer.writeInt(msg.change);
    }

    public static PacketPolymorphPriority decode(PacketBuffer buffer) {
        return new PacketPolymorphPriority(buffer.readInt(), buffer.readBlockPos(), buffer.readInt());

    }

    public static class Handler {
        public static void handle(PacketPolymorphPriority msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;

                ItemStack itemStack;
                if (msg.slotNumber == -1) {
                    itemStack = sender.getHeldItemMainhand();
                    if (!(itemStack.getItem() instanceof BaseCard))
                        itemStack = sender.getHeldItemOffhand();
                } else {
                    Slot slot = container.inventorySlots.get(msg.slotNumber);
                    itemStack = slot.getStack();
                }

                if (itemStack.getItem() instanceof BaseCard) {
                    BaseCard.setPriority(itemStack, BaseCard.getPriority(itemStack) + msg.change);
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
