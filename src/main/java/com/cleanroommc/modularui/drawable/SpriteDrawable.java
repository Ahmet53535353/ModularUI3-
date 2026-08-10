package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteDrawable implements IDrawable {

    private final TextureAtlasSprite sprite;
    private boolean canApplyTheme = false;

    public SpriteDrawable(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        GuiDraw.drawSprite(this.sprite, x, y, width, height);
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(this.sprite.getIconWidth(), this.sprite.getIconHeight());
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(this.sprite.getIconWidth(), this.sprite.getIconHeight());
    }

    @Override
    public boolean canApplyTheme() {
        return this.canApplyTheme;
    }

    public SpriteDrawable canApplyTheme(boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
        return this;
    }


    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SpriteDrawable that)) return false;
        return canApplyTheme == that.canApplyTheme &&
                sprite.getIconName().equals(that.sprite.getIconName());
    }

    @Override
    public int hashCode() {
        int result = sprite.getIconName().hashCode();
        result = 31 * result + Boolean.hashCode(canApplyTheme);
        return result;
    }
}
