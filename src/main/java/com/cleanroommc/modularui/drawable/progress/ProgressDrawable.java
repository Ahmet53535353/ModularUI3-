package com.cleanroommc.modularui.drawable.progress;

import com.cleanroommc.modularui.ModularUIConfig;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

import lombok.Getter;

@Getter
public class ProgressDrawable extends AbstractProgressDrawable<ProgressDrawable> {

    private Direction direction = Direction.RIGHT;
    private float progressPixelStepSize = 0;

    @Override
    public void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        float u0 = 0, u1 = 1, v0 = 0, v1 = 1;
        switch (this.direction) {
            case LEFT:
                u0 = 1 - progress;
                break;
            case RIGHT:
                u1 = progress;
                break;
            case UP:
                v0 = 1 - progress;
                break;
            case DOWN:
                v1 = progress;
                break;
        }
        Stencil.apply((int) (x + u0 * width), (int) (y + v0 * height), (int) ((u1 - u0) * width), (int) ((v1 - v0) * height), context);
    }

    @Override
    protected float getCurrentProgressStepSize(int width, int height) {
        float stepSize = this.progressStepSize;
        float pixelStepSize = this.progressPixelStepSize;
        if (stepSize == 0 && pixelStepSize == 0 && !ModularUIConfig.smoothProgressBar && getFilledTexture() instanceof UITexture) {
            pixelStepSize = 1;
        }
        if (pixelStepSize > 0) {
            float s;
            switch (this.direction) {
                case LEFT:
                case RIGHT:
                    s = width;
                    break;
                case UP:
                case DOWN:
                default:
                    s = height;
                    break;
            }
            stepSize = pixelStepSize / s;
        }
        return stepSize;
    }

    /**
     * Sets the direction in which the progress should move.
     *
     * @param direction direction
     * @return this
     */
    public ProgressDrawable direction(Direction direction) {
        this.direction = direction == null ? Direction.RIGHT : direction;
        return this;
    }

    /**
     * Sets the progress to move right to left.
     *
     * @return this
     */
    public ProgressDrawable left() {
        return direction(Direction.LEFT);
    }

    /**
     * Sets the progress to move left to right.
     *
     * @return this
     */
    public ProgressDrawable right() {
        return direction(Direction.RIGHT);
    }

    /**
     * Sets the progress to move down to up.
     *
     * @return this
     */
    public ProgressDrawable up() {
        return direction(Direction.UP);
    }

    /**
     * Sets the progress to move up to down.
     *
     * @return this
     */
    public ProgressDrawable down() {
        return direction(Direction.DOWN);
    }

    /**
     * Sets a progress step size. The displayed progress will be clamped to the closest multiple of this value.
     * Small values are smooth and high values are choppy. Values higher than 1 means the displayed progress is always 0.
     *
     * @param progressStepSize progress step size
     * @return this.
     */
    @Override
    public ProgressDrawable progressStepSize(float progressStepSize) {
        this.progressPixelStepSize = 0;
        return super.progressStepSize(progressStepSize);
    }

    /**
     * Sets a pixel progress step size. This is similar to {@link #progressStepSize(float)}, but this in units of pixel.
     * This can be useful when you have an actual texture.
     *
     * @param progressPixelStepSize pixel progress size
     * @return this
     * @see #progressStepSize(float)
     */
    public ProgressDrawable progressPixelStepSize(float progressPixelStepSize) {
        this.progressPixelStepSize = progressPixelStepSize;
        this.progressStepSize = 0;
        return this;
    }

    @Override
    public ProgressDrawable smooth() {
        return super.smooth().progressPixelStepSize(-1);
    }

    public enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}
