import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableFieldInfo;
import cn.oopcoder.b2m.enums.ShowMode;

import org.junit.Test;

import java.util.List;

public class StockWindowTest {


    @Test
    public void getTableColumns() {
        List<TableFieldInfo> list = StockDataBean.getTableColumns(ShowMode.Hidden);
        for (TableFieldInfo info : list) {
            System.out.println(info);
        }
    }
}
