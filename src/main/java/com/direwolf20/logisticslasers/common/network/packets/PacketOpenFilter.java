package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.BasicFilterContainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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

                ItemStackHandler handler = getInventory(itemStack);
                NetworkHooks.openGui(sender, new SimpleNamedContainerProvider(
                        (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler, msg.sourcePos), new StringTextComponent("")));
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
