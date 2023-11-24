package com.mndk.bteterrarenderer.mixin.gui;

import com.mndk.bteterrarenderer.core.gui.TextComponentManager;
import lombok.experimental.UtilityClass;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@UtilityClass
@Mixin(value = TextComponentManager.class, remap = false)
public class TextComponentManagerMixin {

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public Object fromJson(String json) {
        return Text.Serializer.fromJson(json);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Nullable
    @Overwrite
    public Object getStyleComponentFromLine(@Nonnull Object lineComponent, int mouseXFromLeft) {
        if(lineComponent instanceof StringVisitable visitable) {
            return MinecraftClient.getInstance().textRenderer.getTextHandler().getStyleAt(visitable, mouseXFromLeft);
        }
        else if(lineComponent instanceof OrderedText text) {
            return MinecraftClient.getInstance().textRenderer.getTextHandler().getStyleAt(text, mouseXFromLeft);
        }
        return null;
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Overwrite
    public boolean handleClick(@Nonnull Object styleComponent) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if(currentScreen == null) return false;
        return currentScreen.handleTextClick((Style) styleComponent);
    }

    /** @author m4ndeokyi
     *  @reason mixin overwrite */
    @Nullable
    @Overwrite
    public Object getTooltipTextFromStyleComponent(@Nonnull Object styleComponent) {
        HoverEvent hoverEvent = ((Style) styleComponent).getHoverEvent();
        return hoverEvent == null ? null : hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
    }

}