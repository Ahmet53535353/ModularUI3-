package com.cleanroommc.modularui.drawable.progress;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import lombok.Getter;

@Getter
public abstract class AbstractProgressDrawable<D extends AbstractProgressDrawable<D>> extends BaseProgressDrawable<D> {

    private IDrawable filledTexture;

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        super.draw(context, x, y, width, height, widgetTheme);
        if (this.filledTexture == null) return;
        float p = getCurrentProgress(width, height);
        if (p == 0f) return;
        if (p < 1f) pushProgressStencil(p, context, x, y, width, height, widgetTheme);
        this.filledTexture.draw(context, x, y, width, height, widgetTheme);
        if (p < 1f) Stencil.remove();
    }

    protected abstract void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme);

    /**
     * Sets the filled texture which is partially drawn based on the current progress.
     *
     * @param drawable filled texture.
     * @return this
     */
    public D filledTexture(IDrawable drawable) {
        this.filledTexture = drawable;
        return self();
    }

}
