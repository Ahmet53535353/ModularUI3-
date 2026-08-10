package com.cleanroommc.modularui.drawable.progress;

import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.Platform;

import lombok.Getter;

import net.minecraft.client.renderer.GlStateManager;

/**
 * A progress texture which translates the progress into a circular angle. This works with any {@link com.cleanroommc.modularui.api.drawable.IDrawable}.
 */
@Getter
public class CircularProgressDrawable extends AbstractProgressDrawable<CircularProgressDrawable> {

    private Direction direction = Direction.CW;

    @Override
    protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        float p = progress;
        float angle = p * MathUtils.PI2;
        Stencil.apply(() -> {
            Platform.setupDrawColor();
            Platform.startDrawing(Platform.DrawMode.TRIANGLE_FAN, Platform.VertexFormat.POS, buffer -> {
                float wHalf = width / 2f, hHalf = height / 2f;
                float xc = x + wHalf, yc = y + hHalf;

                if (this.direction == Direction.CW) {
                    buffer.pos(xc, yc, 0).endVertex(); // center

                    if (p > 7 / 8f) {
                        float tan = (float) Math.tan(angle - MathUtils.PI2);
                        buffer.pos(x + wHalf + tan * wHalf, y, 0).endVertex();
                        buffer.pos(x, y, 0).endVertex(); // top left
                    }
                    if (p > 5 / 8f) {
                        if (p < 7 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI - MathUtils.PI_HALF);
                            buffer.pos(x, y + hHalf - tan * hHalf, 0).endVertex();
                        }
                        buffer.pos(x, y + height, 0).endVertex(); // bottom left
                    }
                    if (p > 3 / 8f) {
                        if (p < 5 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI);
                            buffer.pos(x + wHalf - tan * wHalf, y + height, 0).endVertex();
                        }
                        buffer.pos(x + width, y + height, 0).endVertex(); // bottom right
                    }
                    if (p > 1 / 8f) {
                        if (p < 3 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI_HALF);
                            buffer.pos(x + width, y + hHalf + tan * hHalf, 0).endVertex();
                        }
                        buffer.pos(x + width, y, 0).endVertex(); // top right
                    } else {
                        float tan = (float) Math.tan(angle);
                        buffer.pos(x + wHalf + tan * wHalf, y, 0).endVertex();
                    }
                    buffer.pos(xc, y, 0).endVertex(); // top center, starting angle
                } else {
                    buffer.pos(xc, y, 0).endVertex(); // top center, starting angle
                    if (p < 1 / 8f) {
                        float tan = (float) Math.tan(angle);
                        buffer.pos(x + wHalf - tan * wHalf, y, 0).endVertex();
                    }
                    if (p > 1 / 8f) {
                        buffer.pos(x, y, 0).endVertex(); // top left
                        if (p < 3 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI - MathUtils.PI_HALF);
                            buffer.pos(x, y + hHalf + tan * hHalf, 0).endVertex();
                        }
                    }
                    if (p > 3 / 8f) {
                        buffer.pos(x, y + height, 0).endVertex(); // bottom left
                        if (p < 5 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI);
                            buffer.pos(x + wHalf + tan * wHalf, y + height, 0).endVertex();
                        }
                    }
                    if (p > 5 / 8f) {
                        buffer.pos(x + width, y + height, 0).endVertex(); // bottom right
                        if (p < 7 / 8f) {
                            float tan = (float) Math.tan(angle - MathUtils.PI_HALF);
                            buffer.pos(x + width, y + hHalf - tan * hHalf, 0).endVertex();
                        }
                    }
                    if (p > 7 / 8f) {
                        buffer.pos(x + width, y, 0).endVertex(); // top right
                        float tan = (float) Math.tan(angle);
                        buffer.pos(x + wHalf - tan * wHalf, y, 0).endVertex();
                    }
                }
            });
            GlStateManager.enableTexture2D();
        }, x, y, width, height, context);
    }

    public CircularProgressDrawable direction(Direction direction) {
        this.direction = direction == null ? Direction.CW : direction;
        return this;
    }

    public CircularProgressDrawable clockwise() {
        return direction(Direction.CW);
    }

    public CircularProgressDrawable counterClockwise() {
        return direction(Direction.CCW);
    }

    public enum Direction {
        CW, CCW
    }
}
