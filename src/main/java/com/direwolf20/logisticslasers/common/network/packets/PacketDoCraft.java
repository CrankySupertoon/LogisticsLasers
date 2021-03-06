package com.direwolf20.logisticslasers.common.network.packets;

import com.direwolf20.logisticslasers.common.container.CraftingStationContainer;
import com.direwolf20.logisticslasers.common.tiles.CraftingStationTile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDoCraft {
    ItemStack result;
    int amount;
    boolean bulk;

    public PacketDoCraft(ItemStack stack, int amt, boolean bulk) {
        this.result = stack;
        this.amount = amt;
        this.bulk = bulk;
    }

    public static void encode(PacketDoCraft msg, PacketBuffer buffer) {
        buffer.writeItemStack(msg.result);
        buffer.writeInt(msg.amount);
        buffer.writeBoolean(msg.bulk);
    }

    public static PacketDoCraft decode(PacketBuffer buffer) {
        return new PacketDoCraft(buffer.readItemStack(), buffer.readInt(), buffer.readBoolean());
    }

    public static class Handler {
        public static void handle(PacketDoCraft msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity sender = ctx.get().getSender();
                if (sender == null)
                    return;

                Container container = sender.openContainer;
                if (container == null)
                    return;

                if (container instanceof CraftingStationContainer) {
                    CraftingStationTile te = ((CraftingStationContainer) container).tile;
                    te.onCraft(sender, msg.result, msg.amount, msg.bulk);
                }


            });

            ctx.get().setPacketHandled(true);
        }
    }
}
