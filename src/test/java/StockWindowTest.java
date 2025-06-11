import cn.oopcoder.b2m.bean.StockDataBean;
import cn.oopcoder.b2m.bean.TableColumnInfo;
import cn.oopcoder.b2m.enums.ShowMode;

import org.junit.Test;

import java.util.List;

public class StockWindowTest {


    @Test
    public void getTableColumns() {
        List<TableColumnInfo> list = StockDataBean.getTableColumnInfos(ShowMode.Hidden);
        for (TableColumnInfo info : list) {
            System.out.println(info);
        }
    }
}
