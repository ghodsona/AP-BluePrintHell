package com.BluePrintHell.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class Point2DMixin {
    @JsonCreator
    public Point2DMixin(@JsonProperty("x") double x, @JsonProperty("y") double y) {
    }
}