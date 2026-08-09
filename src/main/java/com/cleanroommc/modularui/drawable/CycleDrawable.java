package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import com.google.common.collect.Iterables;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A drawable that cycles through a list of drawables over time.
 */
public class CycleDrawable implements IDrawable {

    private IDrawable[] drawables;
    private int cycleTime = 1000;

    public CycleDrawable() {
        this(DrawableStack.EMPTY_BACKGROUND);
    }

    public CycleDrawable(IDrawable... drawables) {
        this.drawables = drawables;
    }

    public CycleDrawable(Iterable<IDrawable> drawables) {
        this(Iterables.toArray(drawables, IDrawable.class));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        IDrawable current = getCurrent();
        if (current == null) return;
        current.draw(context, x, y, width, height, widgetTheme);
    }

    public @Nullable IDrawable getCurrent() {
        if (this.drawables.length == 0) return null;
        return this.drawables[Math.abs((int) (System.currentTimeMillis() / this.cycleTime) % this.drawables.length)];
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable current = getCurrent();
        return current != null && current.canApplyTheme();
    }

    public IDrawable[] drawables() {
        return this.drawables;
    }

    public List<IDrawable> getListDrawables() {
        return Arrays.asList(this.drawables);
    }

    public CycleDrawable drawables(IDrawable... drawables) {
        this.drawables = drawables;
        return this;
    }

    public CycleDrawable drawables(Iterable<IDrawable> drawables) {
        return drawables(Iterables.toArray(drawables, IDrawable.class));
    }

    public int cycleTime() {
        return this.cycleTime;
    }

    public CycleDrawable cycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CycleDrawable drawable = (CycleDrawable) o;
        return cycleTime == drawable.cycleTime && Objects.deepEquals(drawables, drawable.drawables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(drawables), cycleTime);
    }
}
