package com.cleanroommc.modularui.widgets;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.value.IDoubleValue;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.drawable.progress.BaseProgressDrawable;
import com.cleanroommc.modularui.drawable.progress.CircularProgressDrawable;
import com.cleanroommc.modularui.drawable.progress.ProgressDrawable;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.widget.Widget;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget<ProgressWidget> {

    private BaseProgressDrawable<?> progressDrawable;
    private IDoubleValue<?> value;

    // Legacy fields kept for the old texture/direction API
    @Getter private final UITexture[] fullTexture = new UITexture[4];
    @Getter private UITexture emptyTexture;
    @Getter private Direction direction = Direction.RIGHT;
    @Getter private int imageSize = -1;

    public ProgressWidget() {}

    public ProgressWidget(BaseProgressDrawable<?> progress) {
        this.progressDrawable = progress;
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IDoubleValue.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castNullable(IDoubleValue.class);
        if (this.value != null && this.progressDrawable != null) {
            this.progressDrawable.progress(this.value::getDoubleValue);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.progressDrawable != null) {
            this.progressDrawable.drawAtZeroPadded(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    /**
     * Sets a custom progress drawable. This replaces the legacy texture API.
     *
     * @param progress progress drawable
     * @return this
     */
    public ProgressWidget progress(BaseProgressDrawable<?> progress) {
        if (this.progressDrawable != null) {
            this.progressDrawable.progress(null);
        }
        this.progressDrawable = progress;
        this.emptyTexture = null;
        this.fullTexture[0] = null;
        this.imageSize = -1;
        if (this.value != null && this.progressDrawable != null) {
            this.progressDrawable.progress(this.value::getDoubleValue);
        }
        return this;
    }

    public ProgressWidget value(IDoubleValue<?> doubleValue) {
        setSyncOrValue(ISyncOrValue.orEmpty(doubleValue));
        return this;
    }

    public ProgressWidget clientValue(DoubleSupplier doubleValue) {
        return value(new DoubleValue.Dynamic(doubleValue, null));
    }

    @ApiStatus.ScheduledForRemoval(inVersion = "3.3.0")
    @Deprecated
    public ProgressWidget progress(DoubleSupplier progress) {
        return clientValue(progress);
    }

    public ProgressWidget progress(double progress) {
        return value(new DoubleValue(progress));
    }

    /**
     * Modern texture API. Builds a {@link ProgressDrawable} from the given textures and direction.
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     * @param direction    direction in which the bar fills
     * @return this
     */
    public ProgressWidget texture(IDrawable emptyTexture, IDrawable fullTexture, ProgressDrawable.Direction direction) {
        return progress(new ProgressDrawable()
                .emptyTexture(emptyTexture)
                .filledTexture(fullTexture)
                .direction(direction));
    }

    /**
     * Modern texture API for a stacked texture where the empty and full bar are stacked on top of each other.
     */
    public ProgressWidget texture(IDrawable texture, ProgressDrawable.Direction direction) {
        return texture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), direction);
    }

    /**
     * Legacy texture API. Builds a {@link ProgressDrawable} or {@link CircularProgressDrawable} from the given textures.
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     * @param imageSize    legacy image size hint (ignored by the new drawable system)
     */
    public ProgressWidget texture(UITexture emptyTexture, UITexture fullTexture, int imageSize) {
        this.emptyTexture = emptyTexture;
        this.fullTexture[0] = fullTexture;
        this.imageSize = imageSize;
        rebuildProgressDrawable();
        return this;
    }

    /**
     * Legacy texture API for a stacked texture where the empty and full bar are stacked on top of each other.
     */
    public ProgressWidget texture(UITexture texture, int imageSize) {
        return texture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), imageSize);
    }

    public ProgressWidget direction(Direction direction) {
        this.direction = direction;
        if (this.fullTexture[0] != null) {
            rebuildProgressDrawable();
        }
        return this;
    }

    private void rebuildProgressDrawable() {
        if (this.fullTexture[0] == null) return;
        BaseProgressDrawable<?> pd;
        if (this.direction == Direction.CIRCULAR_CW) {
            pd = new CircularProgressDrawable()
                    .emptyTexture(this.emptyTexture)
                    .filledTexture(this.fullTexture[0])
                    .clockwise();
        } else {
            pd = new ProgressDrawable()
                    .emptyTexture(this.emptyTexture)
                    .filledTexture(this.fullTexture[0])
                    .direction(mapLegacyDirection(this.direction));
        }
        if (this.value != null) {
            pd.progress(this.value::getDoubleValue);
        }
        this.progressDrawable = pd;
    }

    private static ProgressDrawable.Direction mapLegacyDirection(Direction direction) {
        switch (direction) {
            case LEFT: return ProgressDrawable.Direction.LEFT;
            case UP: return ProgressDrawable.Direction.UP;
            case DOWN: return ProgressDrawable.Direction.DOWN;
            case RIGHT:
            case CIRCULAR_CW:
            default: return ProgressDrawable.Direction.RIGHT;
        }
    }

    @Override
    public @Nullable IDoubleValue<?> getValue() {
        return value;
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN, CIRCULAR_CW
    }
}
