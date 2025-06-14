package cn.oopcoder.b2m.factory;

import cn.oopcoder.b2m.window.tool.StockWindow;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 暂时不用
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectHolder {

    private static List<ProjectHolder> projectHolderList = new ArrayList<>();

    private Project project;

    private StockWindow stockWindow;

    public static ProjectHolder removeProjectHolder(Project project) {
        System.out.println(project.getName() + " 项目从缓存清除");
        ProjectHolder projectHolder = getProjectHolder(project);
        if (projectHolder == null) {
            return null;
        }
        projectHolderList.remove(projectHolder);
        return projectHolder;
    }


    public static void addProjectHolder(Project project, StockWindow stockWindow) {
        System.out.println(project.getName() + " 项目被缓存起来了");
        projectHolderList.add(new ProjectHolder(project, stockWindow));
    }

    private static ProjectHolder getProjectHolder(Project project) {
        for (ProjectHolder projectHolder : projectHolderList) {
            if (projectHolder.getProject().equals(project)) {
                return projectHolder;
            }
        }
        return null;
    }

    private static ProjectHolder getProjectHolder(StockWindow stockWindow) {
        for (ProjectHolder projectHolder : projectHolderList) {
            if (projectHolder.getStockWindow().equals(stockWindow)) {
                return projectHolder;
            }
        }
        return null;
    }

    public static List<ProjectHolder> getProjectHolderExclude(StockWindow stockWindow) {
        return projectHolderList.stream()
                .filter(projectHolder -> !projectHolder.getStockWindow().equals(stockWindow))
                .collect(Collectors.toList());
    }

    public static  ProjectHolder  getFirstProjectHolderExclude(StockWindow stockWindow) {
        return projectHolderList.stream()
                .filter(projectHolder -> !projectHolder.getStockWindow().equals(stockWindow))
                .findFirst(). orElse(null);

    }


}
