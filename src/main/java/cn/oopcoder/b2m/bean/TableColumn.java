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
@interface TableColumn {

    // 列顺序
    int order() default Integer.MAX_VALUE;

    // 列显示名称
    String name();

    // 显示模式
    ShowMode[] showMode() default {Hidden, Normal};

    // 隐藏模式下，列名称, 不配置的话默认取英文字段名
    String hiddenModeName() default "";
}