package com.direwolf20.logisticslasers.client.particles.itemparticle;

import com.direwolf20.logisticslasers.client.particles.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemFlowParticleData implements IParticleData {
    private final ItemStack itemStack;

    public ItemFlowParticleData(ItemStack itemStack) {
        this.itemStack = itemStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
    }

    public static Codec<ItemFlowParticleData> func_239809_a_(ParticleType<ItemFlowParticleData> p_239809_0_) {
        return ItemStack.ITEMSTACK_CODEC.xmap((p_239810_1_) -> {
            return new ItemFlowParticleData(p_239810_1_);
        }, (p_239808_0_) -> {
            return p_239808_0_.itemStack;
        });
    }

    @Nonnull
    @Override
    public ParticleType<ItemFlowParticleData> getType() {
        return ModParticles.ITEMFLOWPARTICLE;
    }

    public void write(PacketBuffer buffer) {
        buffer.writeItemStack(this.itemStack);
    }

    public String getParameters() {
        return Registry.PARTICLE_TYPE.getKey(this.getType()) + " " + (new ItemInput(this.itemStack.getItem(), this.itemStack.getTag())).serialize();
    }

    @OnlyIn(Dist.CLIENT)
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public static final IDeserializer<ItemFlowParticleData> DESERIALIZER = new IDeserializer<ItemFlowParticleData>() {
        @Nonnull
        @Override
        public ItemFlowParticleData deserialize(ParticleType<ItemFlowParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            ItemParser itemparser = (new ItemParser(reader, false)).parse();
            ItemStack itemstack = (new ItemInput(itemparser.getItem(), itemparser.getNbt())).createStack(1, false);
            return new ItemFlowParticleData(itemstack);
        }

        @Override
        public ItemFlowParticleData read(ParticleType<ItemFlowParticleData> particleTypeIn, PacketBuffer buffer) {
            return new ItemFlowParticleData(buffer.readItemStack());
        }
    };
}
