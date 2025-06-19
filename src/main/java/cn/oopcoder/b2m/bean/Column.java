package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.enums.ShowMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;

// 新增表格列注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface Column {

    // 列顺序
    int order() default Integer.MAX_VALUE;

    // 显示模式，默认隐藏模式和正常模式都显示
    ShowMode[] showMode() default {Hidden, Normal};

    // 列显示名称
    String name();

    // 隐藏模式下，列名称, 不配置的话默认取英文字段名
    String hiddenModeName() default "";

    // 修改颜色的阈值
    double colorThreshold() default 0.5;

    // 列前景色, 涨 跌 平
    String[] foreground() default {};

    // 隐藏模式下列前景色, 涨 跌 平
    String[] hiddenModeForeground() default {};

    // 启用数值比较器，涨跌字段是字符串类型，需要转换成数值进行比较
    boolean enableNumberComparator() default false;

    // 是否可修改
    boolean editable() default false;

}