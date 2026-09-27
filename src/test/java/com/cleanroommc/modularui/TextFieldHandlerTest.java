package com.cleanroommc.modularui;

import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextFieldHandlerTest {

    private static final class TestField extends TextFieldWidget {
        TestField() {
            // renderer/scrollArea need a live GL font + gui context, irrelevant for handler logic
            this.handler.setRenderer(null);
            this.handler.setScrollArea(null);
        }

        TextFieldHandler h() {
            return this.handler;
        }
    }

    @Test
    void pasteOverSelectionReplacesIt() {
        TestField field = new TestField();
        field.h().setMaxLines(10);
        field.setText("hello world");

        // select "world"
        field.h().setOffsetCursor(0, 6);
        field.h().setMainCursor(0, 11, false);

        field.h().insert("bye", false);

        assertEquals(List.of("hello bye"), field.h().getText());
        assertEquals(0, field.h().getMainCursor().y);
        assertEquals(9, field.h().getMainCursor().x);
    }

    @Test
    void multilinePasteOverSelectionReplacesIt() {
        TestField field = new TestField();
        field.h().setMaxLines(10);
        field.h().setText(List.of("aaa", "bbb", "ccc", "ddd"));

        // selection from line 1 col 1 to line 3 col 1 (covers "bb", "ccc" and the newline after it)
        field.h().setMainCursor(1, 1, false);
        field.h().setOffsetCursor(3, 1);

        field.h().insert("X\nY", false);

        assertEquals(List.of("aaa", "bX", "Ydd"), field.h().getText());
        assertEquals(2, field.h().getMainCursor().y);
        assertEquals(1, field.h().getMainCursor().x);
    }
}
