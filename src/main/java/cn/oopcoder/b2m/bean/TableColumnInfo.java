package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.utils.JacksonUtil;

import lombok.Data;

import java.awt.Color;
import java.util.List;


@Data
public class TableColumnInfo {

    private String fieldName;
    private String displayName;
    private List<Color> displayColor;
    private int order;
    private boolean enableNumberSorter;
    private boolean editable;

    public TableColumnInfo(String fieldName, String displayName,
                           List<Color> displayColor, int order,
                           boolean enableNumberSorter, boolean editable) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.displayColor = displayColor;
        this.order = order;
        this.enableNumberSorter = enableNumberSorter;
        this.editable = editable;
    }

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}
