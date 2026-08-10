package com.cleanroommc.modularui.drawable.text;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Box;
import java.util.Objects;

import lombok.Getter;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;

public class TextIcon implements IIcon {

    @Getter private final String text;
    private final int width, height;
    private final float scale;
    private final Alignment alignment;
    private static final Box margin = new Box();

    public TextIcon(String text, int width, int height, float scale, Alignment alignment) {
        this.text = text;
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.alignment = alignment;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        TextRenderer.SHARED.setPos(x, y);
        TextRenderer.SHARED.setAlignment(this.alignment, width);
        TextRenderer.SHARED.setScale(this.scale);
        TextRenderer.SHARED.drawSimple(this.text);
    }

    @Override
    public @Nullable IDrawable getWrappedDrawable() {
        return null;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public Box getMargin() {
        return margin;
    }


    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof TextIcon textIcon)) return false;
        return width == textIcon.width && height == textIcon.height && Float.compare(scale, textIcon.scale) == 0 &&
                Objects.equals(text, textIcon.text) && Objects.equals(alignment, textIcon.alignment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(text);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + Float.hashCode(scale);
        result = 31 * result + Objects.hashCode(alignment);
        return result;
    }
}
