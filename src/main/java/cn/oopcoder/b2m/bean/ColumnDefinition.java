package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.utils.JacksonUtil;

import lombok.Data;

import java.awt.Color;
import java.util.List;

@Data
public class ColumnDefinition {

    private String fieldName;
    private String displayName;
    private List<Color> displayColor;
    private int order;
    private boolean enableNumberSorter;
    private boolean editable;
    private Integer preferredWidth;
    double colorThreshold;


    public ColumnDefinition(String fieldName, String displayName, double colorThreshold,
                            List<Color> displayColor, int order,
                            boolean enableNumberSorter, boolean editable, Integer preferredWidth) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.colorThreshold = colorThreshold;
        this.displayColor = displayColor;
        this.order = order;
        this.enableNumberSorter = enableNumberSorter;
        this.editable = editable;
        this.preferredWidth = preferredWidth;
    }

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}
