package com.direwolf20.logisticslasers.common.items.logiccards;

import com.direwolf20.logisticslasers.common.container.cards.PolyFilterContainer;
import com.direwolf20.logisticslasers.common.util.ItemHandlerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;

public class CardPolymorph extends CardInserter {
    public CardPolymorph() {
        super();
        CARDTYPE = CardType.INSERT;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (world.isRemote) return new ActionResult<>(ActionResultType.PASS, itemStack);
        System.out.println(itemStack);
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
                (windowId, playerInventory, playerEntity) -> new PolyFilterContainer(itemStack, windowId, playerInventory, handler, tempArray), new StringTextComponent("")), (buf -> {
            buf.writeItemStack(itemStack);
            buf.writeBlockPos(BlockPos.ZERO);
            buf.writeInt(-1);
        }));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    public static ArrayList<ItemStack> setListFromContainer(ItemStack stack, IItemHandler handler) {
        if (handler == null)
            return new ArrayList<>();
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(handler);
        ArrayList<ItemStack> itemStacks = new ArrayList<>(inventoryCounts.getItemCounts().values());
        ListNBT list = new ListNBT();
        for (ItemStack itemStack : itemStacks) {
            CompoundNBT tag = new CompoundNBT();
            itemStack.setCount(1);
            tag.put("itemStack", itemStack.serializeNBT());
            tag.putInt("count", stack.getCount());
            list.add(tag);
        }
        stack.getOrCreateTag().put("inv", list);
        return itemStacks;
    }

    public static void addContainerToList(ItemStack stack, IItemHandler handler) {
        if (handler == null)
            return;
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(handler);
        ArrayList<ItemStack> handlerItemStacks = new ArrayList<>(inventoryCounts.getItemCounts().values());
        CompoundNBT compound = stack.getOrCreateTag();
        ListNBT nbtList = compound.getList("inv", Constants.NBT.TAG_COMPOUND);
        ItemHandlerUtil.InventoryCounts cardCounts = new ItemHandlerUtil.InventoryCounts(nbtList);
        //ListNBT list = new ListNBT();
        for (ItemStack itemStack : handlerItemStacks) {
            if (cardCounts.getCount(itemStack) != 0) continue;
            CompoundNBT tag = new CompoundNBT();
            itemStack.setCount(1);
            tag.put("itemStack", itemStack.serializeNBT());
            tag.putInt("count", stack.getCount());
            nbtList.add(tag);
        }
        stack.getOrCreateTag().put("inv", nbtList);
    }

    public static void clearList(ItemStack stack) {
        stack.getOrCreateTag().put("inv", new ListNBT());
    }

    public static ArrayList<ItemStack> getListFromCard(ItemStack stack) {
        CompoundNBT compound = stack.getOrCreateTag();
        ListNBT nbtList = compound.getList("inv", Constants.NBT.TAG_COMPOUND);
        ItemHandlerUtil.InventoryCounts inventoryCounts = new ItemHandlerUtil.InventoryCounts(nbtList);
        return !compound.contains("inv") ? setListFromContainer(stack, null) : new ArrayList<>(inventoryCounts.getItemCounts().values());
    }
}
