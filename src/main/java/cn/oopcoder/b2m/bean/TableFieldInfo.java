package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.utils.JacksonUtil;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;

public record TableFieldInfo(String fieldName, String displayName, List<Color> displayColor, int order) {

    @Override
    public @NotNull String toString() {
        return JacksonUtil.toJson(this);
    }
}
