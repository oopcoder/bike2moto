package cn.oopcoder.b2m.factory;

import cn.oopcoder.b2m.window.tool.StockWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * idea启动后，第一次打开项目时创建实例，多个项目共享一个实例
 */
public class B2mToolWindowFactory implements ToolWindowFactory {


    public B2mToolWindowFactory() {
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
        StockWindow stockWindow = new StockWindow();

        Content stockContent = factory.createContent(stockWindow.rootPanel, "good luck", true);
        contentManager.addContent(stockContent);

        ProjectHolder.addProjectHolder(project, stockWindow);

        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                // 项目关闭前的回调
                System.out.println("项目即将关闭: " + project.getName());

                stockWindow.projectClosing(project);

                ProjectHolder projectHolder = ProjectHolder.removeProjectHolder(project);
                if (projectHolder != null) {
                    // projectHolder.stockWindow.projectClosing(project);
                }
            }

            @Override
            public void projectClosed(@NotNull Project project) {
                // 项目完全关闭后的回调
                System.out.println("项目已关闭: " + project.getName());
            }
        });

    }

}
