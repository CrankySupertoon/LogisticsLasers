package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.customslot.BasicFilterSlot;
import com.direwolf20.logisticslasers.common.container.customslot.StockerFilterSlot;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketFilterSlot {
    private int slotNumber;
    private ItemStack stack;

    public PacketFilterSlot(int slotNumber, ItemStack stack) {
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    public static void encode(PacketFilterSlot msg, PacketBuffer buffer) {
        buffer.writeInt(msg.slotNumber);
        buffer.writeItemStack(msg.stack);
    }

    public static PacketFilterSlot decode(PacketBuffer buffer) {
        return new PacketFilterSlot(buffer.readInt(), buffer.readItemStack());
    }

    public static class Handler {
        public static void handle(PacketFilterSlot msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                Slot slot = container.inventorySlots.get(msg.slotNumber);
                if (slot instanceof BasicFilterSlot || slot instanceof StockerFilterSlot)
                    slot.putStack(msg.stack);
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
