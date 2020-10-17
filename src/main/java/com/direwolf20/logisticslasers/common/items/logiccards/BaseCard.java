package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.LogisticsLasers;
import com.direwolf20.logisticslasers.common.container.cards.BasicFilterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

public abstract class BaseCard extends Item {

    protected BaseCard.CardType CARDTYPE;

    public BaseCard() {
        super(new Item.Properties().maxStackSize(64).group(LogisticsLasers.itemGroup));
    }

    public enum CardType {
        EXTRACT,
        INSERT,
        PROVIDE,
        STOCK,
        CRAFT
    }

    public BaseCard(Properties prop) {
        super(prop);
    }

    public CardType getCardType() {
        return CARDTYPE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(ActionResultType.PASS, itemStack);
        ItemStackHandler handler = getInventory(itemStack);
        IIntArray tempArray = new IIntArray() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0:
                        return getPriority(itemStack);
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
                return 1;
            }
        };
        NetworkHooks.openGui((ServerPlayerEntity) player, new SimpleNamedContainerProvider(
                (windowId, playerInventory, playerEntity) -> new BasicFilterContainer(itemStack, windowId, playerInventory, handler, tempArray), new StringTextComponent("")), (buf -> {
            buf.writeItemStack(itemStack);
        }));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    public static ItemStackHandler setInventory(ItemStack stack, ItemStackHandler handler) {
        stack.getOrCreateTag().put("inv", handler.serializeNBT());
        ListNBT countList = new ListNBT();
        for (int i = 0; i < handler.getSlots(); i++) {
            CompoundNBT countTag = new CompoundNBT();
            ItemStack itemStack = handler.getStackInSlot(i);
            if (itemStack.getCount() > itemStack.getMaxStackSize()) {
                countTag.putInt("Slot", i);
                countTag.putInt("Count", itemStack.getCount());
                countList.add(countTag);
            }
        }
        stack.getOrCreateTag().put("counts", countList);
        return handler;
    }

    public static ItemStackHandler getInventory(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        ItemStackHandler handler = new ItemStackHandler(BasicFilterContainer.SLOTS);
        handler.deserializeNBT(compound.getCompound("inv"));
        ListNBT countList = compound.getList("counts", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < countList.size(); i++) {
            CompoundNBT countTag = countList.getCompound(i);
            int slot = countTag.getInt("Slot");
            ItemStack itemStack = handler.getStackInSlot(slot);
            itemStack.setCount(countTag.getInt("Count"));
            handler.setStackInSlot(slot, itemStack);
        }
        return !compound.contains("inv") ? setInventory(stack, new ItemStackHandler(BasicFilterContainer.SLOTS)) : handler;
    }

    public static Set<ItemStack> getFilteredItems(ItemStack stack) {
        Set<ItemStack> filteredItems = new HashSet<>();
        if (stack.getItem() instanceof CardPolymorph) {
            filteredItems = new HashSet<>(CardPolymorph.getListFromCard(stack));
        } else {
            ItemStackHandler filterSlotHandler = getInventory(stack);
            for (int i = 0; i < filterSlotHandler.getSlots(); i++) {
                ItemStack itemStack = filterSlotHandler.getStackInSlot(i);
                if (!itemStack.isEmpty())
                    filteredItems.add(itemStack);
            }
        }
        return filteredItems;
    }

    public static int setPriority(ItemStack card, int priority) {
        card.getOrCreateTag().putInt("priority", priority);
        return priority;
    }

    public static int getPriority(ItemStack card) {
        CompoundNBT compound = card.getOrCreateTag();
        return !compound.contains("priority") ? setPriority(card, 0) : compound.getInt("priority");
    }

    public static boolean setWhiteList(ItemStack card, boolean whitelist) {
        card.getOrCreateTag().putBoolean("whitelist", whitelist);
        return whitelist;
    }

    public static boolean getWhiteList(ItemStack card) {
        CompoundNBT compound = card.getOrCreateTag();
        return !compound.contains("whitelist") ? setWhiteList(card, true) : compound.getBoolean("whitelist");
    }
}
