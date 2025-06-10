package cn.oopcoder.b2m.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * 默认主题： fill=#6E6E6E
 * dark主题： fill="#AFB1B3"
 *
 * svg 可以修改填充的颜色
 */

public interface Icons {

    Icon ICON_MOVE_TOP = IconLoader.getIcon("/icons/move_top.svg", Icons.class);
    Icon ICON_MOVE_BOTTOM = IconLoader.getIcon("/icons/move_bottom.svg", Icons.class);

    Icon ICON_MOVE_UP = IconLoader.getIcon("/icons/move_up.svg", Icons.class);
    Icon ICON_MOVE_DOWN = IconLoader.getIcon("/icons/move_down.svg", Icons.class);

    Icon ICON_PIN_TOP = IconLoader.getIcon("/icons/pin_top.svg", Icons.class);
    Icon ICON_RESET_DEFAULT_CONFIG = IconLoader.getIcon("/icons/reset_default_config.svg", Icons.class);

    Icon ICON_CHECKBOX_SELECTED = IconLoader.getIcon("/icons/checkbox_selected.svg", Icons.class);
    Icon ICON_CHECKBOX_UNSELECTED = IconLoader.getIcon("/icons/checkbox_unselected.svg", Icons.class);

}

