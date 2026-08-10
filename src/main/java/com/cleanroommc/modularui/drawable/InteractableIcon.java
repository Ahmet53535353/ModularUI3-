package com.cleanroommc.modularui.drawable;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IIcon;
import com.cleanroommc.modularui.api.widget.IGuiAction;
import com.cleanroommc.modularui.api.widget.Interactable;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class InteractableIcon extends DelegateIcon implements Interactable {

    private IGuiAction.MousePressed mousePressed;
    private IGuiAction.MouseReleased mouseReleased;
    private IGuiAction.MousePressed mouseTapped;
    private IGuiAction.MouseScroll mouseScroll;
    private IGuiAction.KeyPressed keyPressed;
    private IGuiAction.KeyReleased keyReleased;
    private IGuiAction.KeyPressed keyTapped;

    public InteractableIcon(IIcon icon) {
        super(icon);
    }

    public void playClickSound() {
        //if (this.playClickSound) {
        Interactable.playButtonClickSound();
        //}
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (this.mousePressed != null && this.mousePressed.press(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return this.mouseReleased != null && this.mouseReleased.release(mouseButton);
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        if (this.mouseTapped != null && this.mouseTapped.press(mouseButton)) {
            playClickSound();
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public @NotNull Result onKeyPressed(char typedChar, int keyCode) {
        if (this.keyPressed != null && this.keyPressed.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.ACCEPT;
    }

    @Override
    public boolean onKeyRelease(char typedChar, int keyCode) {
        return this.keyReleased != null && this.keyReleased.release(typedChar, keyCode);
    }

    @NotNull
    @Override
    public Result onKeyTapped(char typedChar, int keyCode) {
        if (this.keyTapped != null && this.keyTapped.press(typedChar, keyCode)) {
            return Result.SUCCESS;
        }
        return Result.IGNORE;
    }

    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        return this.mouseScroll != null && this.mouseScroll.scroll(scrollDirection, amount);
    }

    public InteractableIcon onMousePressed(IGuiAction.MousePressed mousePressed) {
        this.mousePressed = mousePressed;
        return this;
    }

    public InteractableIcon onMouseReleased(IGuiAction.MouseReleased mouseReleased) {
        this.mouseReleased = mouseReleased;
        return this;
    }

    public InteractableIcon onMouseTapped(IGuiAction.MousePressed mouseTapped) {
        this.mouseTapped = mouseTapped;
        return this;
    }

    public InteractableIcon onMouseScrolled(IGuiAction.MouseScroll mouseScroll) {
        this.mouseScroll = mouseScroll;
        return this;
    }

    public InteractableIcon onKeyPressed(IGuiAction.KeyPressed keyPressed) {
        this.keyPressed = keyPressed;
        return this;
    }

    public InteractableIcon onKeyReleased(IGuiAction.KeyReleased keyReleased) {
        this.keyReleased = keyReleased;
        return this;
    }

    public InteractableIcon onKeyTapped(IGuiAction.KeyPressed keyTapped) {
        this.keyTapped = keyTapped;
        return this;
    }


    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof InteractableIcon that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mousePressed, that.mousePressed) && Objects.equals(mouseReleased, that.mouseReleased) &&
                Objects.equals(mouseTapped, that.mouseTapped) && Objects.equals(mouseScroll, that.mouseScroll) &&
                Objects.equals(keyPressed, that.keyPressed) && Objects.equals(keyReleased, that.keyReleased) &&
                Objects.equals(keyTapped, that.keyTapped);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(mousePressed);
        result = 31 * result + Objects.hashCode(mouseReleased);
        result = 31 * result + Objects.hashCode(mouseTapped);
        result = 31 * result + Objects.hashCode(mouseScroll);
        result = 31 * result + Objects.hashCode(keyPressed);
        result = 31 * result + Objects.hashCode(keyReleased);
        result = 31 * result + Objects.hashCode(keyTapped);
        return result;
    }
}
