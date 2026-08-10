package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Interpolations;
import com.cleanroommc.modularui.utils.MathUtils;

import org.jetbrains.annotations.Nullable;

public class SubAreaDrawable extends DelegateDrawable {

    private float u0 = 0, v0 = 0, u1 = 1, v1 = 1;

    public SubAreaDrawable(@Nullable IDrawable drawable) {
        super(drawable);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        Stencil.apply((int) (x + this.u0 * width), (int) (y + this.v0 * height), (int) ((this.u1 - this.u0) * width), (int) ((this.v1 - this.v0) * height), context);
        super.draw(context, x, y, width, height, widgetTheme);
        Stencil.remove();
    }

    @Override
    public IDrawable getSubArea(float u0, float v0, float u1, float v1) {
        return new SubAreaDrawable(getWrappedDrawable()).uv(lerpU(u0), lerpV(v0), lerpU(u1), lerpV(v1));
    }

    protected final float lerpU(float u) {
        return Interpolations.lerp(this.u0, this.u1, u);
    }

    protected final float lerpV(float v) {
        return Interpolations.lerp(this.v0, this.v1, v);
    }

    public SubAreaDrawable u0(float u) {
        this.u0 = MathUtils.clamp(u, 0f, 1f);
        return this;
    }

    public SubAreaDrawable u1(float u) {
        this.u1 = MathUtils.clamp(u, 0f, 1f);
        return this;
    }

    public SubAreaDrawable v0(float v) {
        this.v0 = MathUtils.clamp(v, 0f, 1f);
        return this;
    }

    public SubAreaDrawable v1(float v) {
        this.v1 = MathUtils.clamp(v, 0f, 1f);
        return this;
    }

    public SubAreaDrawable uv(float u0, float v0, float u1, float v1) {
        return u0(u0).v0(v0).u1(u1).v1(v1);
    }

    public SubAreaDrawable u(float u0, float u1) {
        return u0(u0).u1(u1);
    }

    public SubAreaDrawable v(float v0, float v1) {
        return v0(v0).v1(v1);
    }

    public SubAreaDrawable drawable(@Nullable IDrawable drawable) {
        setDrawable(drawable);
        return this;
    }
}
