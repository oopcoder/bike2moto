package cn.oopcoder.b2m.factory;

import cn.oopcoder.b2m.window.tool.StockWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * idea启动后，第一次打开项目时创建实例，多个项目共享一个实例
 */
public class B2mToolWindowFactory implements ToolWindowFactory {

    // 使用静态变量是为了 多个项目共享一个窗口
    static StockWindow stockWindow;


    public B2mToolWindowFactory() {
        stockWindow = new StockWindow();
    }

    /**
     * 项目打开时会调用
     */
    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
    }


    /**
     * 插件窗口第一次可见时调用
     * 1、第一次点击插件图标，
     * 2、之前已经打开过窗口，关掉项目后，再次打开项目，也会调用（窗口打开状态有记录）
     *
     * @param toolWindow 项目窗口，新打开一个项目，toolWindow是不一样的
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory factory = contentManager.getFactory();

        Content stockContent = factory.createContent(stockWindow.rootPanel, "good luck", true);
        contentManager.addContent(stockContent);

    }

}
