package com.cleanroommc.modularui.drawable.progress;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widgets.ProgressWidget;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

@Getter
public abstract class BaseProgressDrawable<D extends BaseProgressDrawable<D>> implements IDrawable {

    private IDrawable emptyBackground;
    private DoubleSupplier progress;
    protected float progressStepSize = 0;

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.emptyBackground != null) this.emptyBackground.draw(context, x, y, width, height, widgetTheme);
    }

    protected float getCurrentProgress(int width, int height) {
        float p = this.progress == null ? 1f : (float) this.progress.getAsDouble();
        p = MathUtils.clamp(p, 0, 1);
        float stepSize = getCurrentProgressStepSize(width, height);
        if (stepSize > 0) {
            int c = (int) (p / stepSize);
            p = c * stepSize;
        }
        return p;
    }

    protected float getCurrentProgressStepSize(int width, int height) {
        return this.progressStepSize;
    }

    /**
     * Sets the displayed progress value. The progress is clamped between 0 (empty) and 1 (filled).
     *
     * @param progress progress supplier
     * @return this
     */
    public D progress(DoubleSupplier progress) {
        this.progress = progress;
        return self();
    }

    /**
     * Sets a fixed progress value to display. The progress is clamped between 0 (empty) and 1 (filled).
     *
     * @param progress progress
     * @return this
     */
    public D progress(double progress) {
        return progress(() -> progress);
    }

    /**
     * Sets a progress supplier which linearly increases from 0 to 1 with the given duration in milliseconds.
     *
     * @param durationMilliSeconds duration in milliseconds
     * @return this
     */
    public D progressDuration(int durationMilliSeconds) {
        return progress(() -> System.currentTimeMillis() % durationMilliSeconds / (double) durationMilliSeconds);
    }

    /**
     * Sets a progress supplier which linearly increases from 0 to 1 with the given duration.
     *
     * @param duration duration
     * @param unit     time unit of the previous duration argument
     * @return this
     */
    public D progressDuration(long duration, TimeUnit unit) {
        return progressDuration((int) unit.toMillis(duration));
    }

    /**
     * Sets a progress step size. The displayed progress will be clamped to the closest multiple of this value.
     * Small values are smooth and high values are choppy. Values higher than 1 means the displayed progress is always 0.
     *
     * @param progressStepSize progress step size
     * @return this.
     */
    public D progressStepSize(float progressStepSize) {
        this.progressStepSize = progressStepSize;
        return self();
    }

    public D smooth() {
        return progressStepSize(-1);
    }

    /**
     * Sets the empty texture which is always fully displayed.
     *
     * @param drawable empty texture
     * @return this
     */
    public D emptyTexture(IDrawable drawable) {
        this.emptyBackground = drawable;
        return self();
    }

    @SuppressWarnings("unchecked")
    protected D self() {
        return (D) this;
    }

    @Override
    public ProgressWidget asWidget() {
        ProgressWidget widget = new ProgressWidget(this);
        if (this.progress != null) widget.clientValue(this.progress);
        return widget;
    }
}
