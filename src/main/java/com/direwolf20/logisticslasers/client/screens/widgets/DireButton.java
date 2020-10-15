package com.direwolf20.logisticslasers.client.screens.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class DireButton extends Button {

    public DireButton(int x, int y, int widthIn, int heightIn, ITextComponent buttonText, IPressable action) {
        super(x, y, widthIn, heightIn, buttonText, action);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        //super.render(matrices, mouseX, mouseY, partialTicks);

        if (this.visible) {
            FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
            Minecraft.getInstance().getTextureManager().bindTexture(WIDGETS_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.isHovered = isMouseOver(mouseX, mouseY);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.blit(matrices, this.x, this.y, 0, 46, this.width / 2, this.height);
            this.blit(matrices, this.x + this.width / 2, this.y, 200 - this.width / 2, 46, this.width / 2, this.height);


            int bottomToDraw = 2;
            this.blit(matrices, this.x, this.y + this.height - bottomToDraw, 0, 66 - bottomToDraw, this.width / 2, bottomToDraw);
            this.blit(matrices, this.x + this.width / 2, this.y + this.height - bottomToDraw, 200 - this.width / 2, 66 - bottomToDraw, this.width / 2, bottomToDraw);

            int j = 14737632;

            if (this.isHovered) {
                j = 16777120;
            } else if (!this.active) {
                j = 10526880;
            } else if (this.packedFGColor != 0) {
                j = this.packedFGColor;
            }

            int y = this.y + (this.height - 7) / 2;
            if (this.getMessage().getString().equals("~"))
                y = this.y + (this.height - 1) / 2;
            this.drawCenteredString(matrices, fontrenderer, this.getMessage().getString(), this.x + this.width / 2, y, j);
        }
    }
}
