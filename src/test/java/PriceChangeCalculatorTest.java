import org.junit.Test;
import cn.oopcoder.b2m.dataSource.history.PriceChange;
import cn.oopcoder.b2m.dataSource.history.PriceChangeCalculator;
import cn.oopcoder.b2m.utils.NumUtil;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by oopcoder at 2025/6/18 23:06 .
 */

public class PriceChangeCalculatorTest {

    @Test
    public void testUpdatePrice() throws Exception {
        PriceChangeCalculator calculator = PriceChangeCalculator.getInstance();

        // 模拟每3秒更新一次数据
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {

            long currentTime = System.currentTimeMillis();

            double randomPrice = 100 + (Math.random() * 100);
            String currentPrice = String.format("%.2f", randomPrice);

            // 更新数据
            String symbol = "sz300059";
            PriceChange changes = calculator.updatePrice(symbol, currentTime, currentPrice);

            System.out.println("1分钟涨幅: " + changes.changePercentOfMin1 + "%");
            System.out.println("3分钟涨幅: " + changes.changePercentOfMin3 + "%");
            System.out.println("5分钟涨幅: " + changes.changePercentOfMin5 + "%");
        }, 0, 3, TimeUnit.SECONDS);
    }
}
