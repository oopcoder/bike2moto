package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.utils.JacksonUtil;
import org.jetbrains.annotations.NotNull;

public record TableFieldInfo(String fieldName, String displayName, int order) {


    @Override
    public @NotNull String toString() {
        return JacksonUtil.toJson(this);
    }
}
