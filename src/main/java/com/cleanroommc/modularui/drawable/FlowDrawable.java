package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.sizer.Box;

import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * A {@link com.cleanroommc.modularui.widgets.layout.Flow Flow} as a drawable. This version calculates the children positions on each frame.
 */
public class FlowDrawable implements IDrawable {

    public static FlowDrawable row() {
        return new FlowDrawable(GuiAxis.X);
    }

    public static FlowDrawable column() {
        return new FlowDrawable(GuiAxis.Y);
    }

    @Getter private final GuiAxis axis;
    @Getter private final List<IIcon> icons = new ArrayList<>();
    private Alignment.MainAxis maa = Alignment.MainAxis.START;
    private Alignment.CrossAxis caa = Alignment.CrossAxis.CENTER;

    public FlowDrawable(GuiAxis axis) {
        this.axis = axis;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.icons.isEmpty()) return;
        if (this.icons.size() == 1) {
            this.icons.getFirst().draw(context, x, y, width, height, widgetTheme);
            return;
        }

        GuiAxis otherAxis = this.axis.getOther();
        int size = this.axis.isHorizontal() ? width : height;
        int crossSize = this.axis.isHorizontal() ? height : width;
        int pos = this.axis.isHorizontal() ? x : y;
        @Range(from = 2, to = Integer.MAX_VALUE) int amount = this.icons.size();
        int childrenSize = 0;
        int expandedAmount = 0;

        for (val icon : this.icons) {
            int s = icon.getSize(this.axis);
            if (s <= 0) {
                expandedAmount++;
            } else {
                childrenSize += s;
            }
        }
        Alignment.MainAxis maa = this.maa;
        int space = 0;
        int startOffset = 0;
        final int spaceCount = Math.max(amount - 1, 0);
        if (maa == Alignment.MainAxis.SPACE_BETWEEN || maa == Alignment.MainAxis.SPACE_AROUND) {
            if (expandedAmount > 0) {
                maa = Alignment.MainAxis.START;
            } else {
                int free = size - childrenSize;
                if (maa == Alignment.MainAxis.SPACE_BETWEEN) {
                    space = free / spaceCount;
                } else { // SPACE_AROUND
                    int unit = free / (2 * amount); // one edge gap
                    startOffset = unit;
                    space = 2 * unit;
                }
            }
        }

        int lastP = pos + startOffset;
        if (maa == Alignment.MainAxis.CENTER) {
            lastP += (int) (size / 2f - childrenSize / 2f);
        } else if (maa == Alignment.MainAxis.END) {
            lastP += size - childrenSize;
        }

        int expanderSize = expandedAmount > 0 ? (size - childrenSize) / expandedAmount : 0;
        int is, ics, ip, icp; // s = size, p = pos, c = cross
        for (IIcon icon : this.icons) {
            Box margin = icon.getMargin();
            int s = icon.getSize(this.axis);
            int cs = icon.getSize(otherAxis);
            is = s;
            ics = cs;
            ip = lastP;
            icp = 0;
            if (s <= 0) {
                is = expanderSize;
                if (margin != null) ip += margin.getStart(this.axis);
            }
            if (cs <= 0) {
                ics = crossSize;
                if (margin != null) {
                    ics -= margin.getTotal(otherAxis);
                    icp = margin.getStart(otherAxis);
                }
            } else {
                if (this.caa == Alignment.CrossAxis.CENTER) {
                    icp = (crossSize - cs) / 2;
                } else if (this.caa == Alignment.CrossAxis.END) {
                    icp = crossSize - cs;
                }
            }
            if (this.axis.isHorizontal()) {
                icon.draw(context, ip, icp, is, ics, widgetTheme);
            } else {
                icon.draw(context, icp, ip, ics, is, widgetTheme);
            }
            lastP += is + space;
        }
    }

    @Override
    public int getDefaultWidth() {
        return this.axis.isHorizontal() ? getMainAxisDefaultSize() : getCrossAxisDefaultSize();
    }

    @Override
    public int getDefaultHeight() {
        return this.axis.isHorizontal() ? getCrossAxisDefaultSize() : getMainAxisDefaultSize();
    }

    public int getMainAxisDefaultSize() {
        int s = 0;
        for (IIcon icon : this.icons) {
            int is = icon.getSize(this.axis);
            if (is <= 0) is = this.axis.isHorizontal() ? icon.getDefaultWidth() : icon.getDefaultHeight();
            if (is <= 0) is = 10;
            s += is + icon.getMargin().getTotal(this.axis);
        }
        return s;
    }

    public int getCrossAxisDefaultSize() {
        int s = 0;
        GuiAxis axis = this.axis.getOther();
        for (IIcon icon : this.icons) {
            int is = icon.getSize(axis);
            if (is <= 0) is = axis.isHorizontal() ? icon.getDefaultWidth() : icon.getDefaultHeight();
            if (is <= 0) is = 10;
            s = Math.max(s, is + icon.getMargin().getTotal(axis));
        }
        return s;
    }

    public FlowDrawable mainAxisAlignment(Alignment.MainAxis maa) {
        this.maa = maa;
        return this;
    }

    public FlowDrawable crossAxisAlignment(Alignment.CrossAxis caa) {
        this.caa = caa;
        return this;
    }

    public FlowDrawable icon(IIcon icon) {
        this.icons.add(icon);
        return this;
    }

    public FlowDrawable icons(int amount, IntFunction<IIcon> func) {
        for (int i = 0; i < amount; i++) {
            icon(func.apply(i));
        }
        return this;
    }

    public <T> FlowDrawable icons(Iterable<T> it, Function<T, IIcon> func) {
        for (T t : it) {
            icon(func.apply(t));
        }
        return this;
    }

    public FlowDrawable icons(Collection<IIcon> icons) {
        this.icons.addAll(icons);
        return this;
    }

    public FlowDrawable removeIcon(IIcon icon) {
        this.icons.remove(icon);
        return this;
    }

    public FlowDrawable removeAll() {
        this.icons.clear();
        return this;
    }


    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof FlowDrawable that)) return false;
        return axis == that.axis && icons.equals(that.icons) &&
                maa == that.maa && caa == that.caa;
    }

    @Override
    public int hashCode() {
        int result = axis.hashCode();
        result = 31 * result + icons.hashCode();
        result = 31 * result + maa.hashCode();
        result = 31 * result + caa.hashCode();
        return result;
    }
}
