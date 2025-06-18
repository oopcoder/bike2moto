import cn.oopcoder.b2m.utils.NumUtil;
import cn.oopcoder.b2m.utils.StockDataUtil;
import org.junit.Test;

public class NumUtilTest {


    @Test
    public void testFormatDecimal() throws Exception {
        System.out.println(NumUtil.formatDecimal(25, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.05, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.080, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.01566, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.05486, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.05412, 2, 3));
        System.out.println(NumUtil.formatDecimal(0.06992, 2, 3));
    }
}
