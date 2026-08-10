package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.utils.ParseResult;

@FunctionalInterface
public interface INumberParser {

    ParseResult parse(String expr, double defaultValue);
}
