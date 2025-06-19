package cn.oopcoder.b2m.bean;

import cn.oopcoder.b2m.config.StockConfig;
import cn.oopcoder.b2m.dataSource.history.PriceChange;
import cn.oopcoder.b2m.dataSource.history.PriceChangeCalculator;
import cn.oopcoder.b2m.dataSource.StockData;
import cn.oopcoder.b2m.enums.ShowMode;
import cn.oopcoder.b2m.utils.JacksonUtil;

import cn.oopcoder.b2m.utils.NumUtil;
import cn.oopcoder.b2m.utils.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.intellij.ui.JBColor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cn.oopcoder.b2m.consts.ColorHexConst.coralRed;
import static cn.oopcoder.b2m.consts.ColorHexConst.lightRed;
import static cn.oopcoder.b2m.consts.ColorHexConst.mossGreen;
import static cn.oopcoder.b2m.consts.ColorHexConst.softGreen;
import static cn.oopcoder.b2m.enums.ShowMode.Hidden;
import static cn.oopcoder.b2m.enums.ShowMode.Normal;
import static cn.oopcoder.b2m.utils.ReflectUtil.setFieldValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockDataBean {

    public static final String STOCK_CODE_FIELD_NAME = "symbol";
    public static final String CHANGE_PERCENT_FIELD_NAME = "changePercent";
    public static final String CHANGE_FIELD_NAME = "change";
    public static final String RANGE_PERCENT_FIELD_NAME = "rangePercent";
    public static final String LOW_PERCENT_FIELD_NAME = "lowPercent";
    public static final String HIGH_PERCENT_FIELD_NAME = "highPercent";
    public static final String Min1_FIELD_NAME = "changePercentOfMin1";
    public static final String Min3_FIELD_NAME = "changePercentOfMin3";
    public static final String Min5_FIELD_NAME = "changePercentOfMin5";
    public static final String Threshold1_FIELD_NAME = "reminderThresholdOfMin1";
    public static final String Threshold3_FIELD_NAME = "reminderThresholdOfMin3";
    public static final String Threshold5_FIELD_NAME = "reminderThresholdOfMin5";

    // 调试用，不需要时直接注释
    // @Column(name = "行号", order = 4)
    private int index;

    @Column(name = "名称", order = 6, showMode = {Normal})
    private String name;

    @Column(name = "代码", order = 8)
    private String symbol;

    // 隐蔽模式名，最好是英文
    @Column(name = "马赛克", order = 10, showMode = {Hidden}, hiddenModeName = "mask", editable = true)
    private String maskName;

    @Column(name = "别名", order = 15, showMode = {Normal}, editable = true)
    private String alias;

    @Column(name = "当前价", order = 20, hiddenModeName = "now", enableNumberComparator = true)
    private String currentPrice;

    @Column(name = "涨跌", order = 25, enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String change;

    @Column(name = "涨跌幅", order = 30, hiddenModeName = "change(%)", enableNumberComparator = true,
            foreground = {lightRed, softGreen}, hiddenModeForeground = {lightRed, softGreen})
    private String changePercent;

    @Column(name = "最高价", order = 35, enableNumberComparator = true)
    private String high;

    @Column(name = "最大涨跌幅", order = 36, hiddenModeName = "high(%)", enableNumberComparator = true)
    private String highPercent;

    @Column(name = "最低价", order = 40, enableNumberComparator = true)
    private String low;

    @Column(name = "最小涨跌幅", order = 41, hiddenModeName = "low(%)", enableNumberComparator = true)
    private String lowPercent;

    @Column(name = "开盘价", order = 45, enableNumberComparator = true, showMode = {Normal})
    private String open;

    // 昨日收盘价, 今天收盘价 就是现价
    @Column(name = "昨收价", order = 50, enableNumberComparator = true, showMode = {Normal})
    private String preClose;

    // 价格差 = 最高 - 最低
    @Column(name = "价格差", order = 55, enableNumberComparator = true)
    private String range;

    // 振幅百分比
    @Column(name = "振幅", order = 60, hiddenModeName = "range(%)", enableNumberComparator = true)
    private String rangePercent;

    @Column(name = "1分钟涨幅", order = 70, hiddenModeName = "1min", enableNumberComparator = true,
            foreground = {coralRed, mossGreen}, hiddenModeForeground = {coralRed, mossGreen}, colorThreshold = 2)
    private String changePercentOfMin1;

    @Column(name = "3分钟涨幅", order = 80, hiddenModeName = "3min", enableNumberComparator = true,
            foreground = {coralRed, mossGreen}, hiddenModeForeground = {coralRed, mossGreen}, colorThreshold = 2.5)
    private String changePercentOfMin3;

    @Column(name = "5分钟涨幅", order = 90, hiddenModeName = "5min", enableNumberComparator = true,
            foreground = {coralRed, mossGreen}, hiddenModeForeground = {coralRed, mossGreen}, colorThreshold = 3.5)
    private String changePercentOfMin5;

    @Column(name = "1分钟阈值", order = 71, hiddenModeName = "1Thr", enableNumberComparator = true, showMode = {Normal}, editable = true)
    public Double reminderThresholdOfMin1;

    @Column(name = "3分钟阈值", order = 81, hiddenModeName = "3Thr", enableNumberComparator = true, showMode = {Normal}, editable = true)
    public Double reminderThresholdOfMin3;

    @Column(name = "5分钟阈值", order = 91, hiddenModeName = "5Thr", enableNumberComparator = true, showMode = {Normal}, editable = true)
    public Double reminderThresholdOfMin5;

    @Column(name = "时间", order = 100, showMode = {Normal})
    private String time;

    // 固定在顶部
    // @Column(name = "固定", order = 45)
    private boolean pinTop = false;

    public static List<ColumnDefinition> getTableColumnInfos(ShowMode showMode) {
        boolean isHidden = Hidden == showMode;

        return Arrays.stream(StockDataBean.class.getDeclaredFields())
                .filter(field -> {
                    if (!field.isAnnotationPresent(Column.class)) {
                        return false;
                    }
                    ShowMode[] showModes = field.getAnnotation(Column.class).showMode();
                    return Arrays.stream(showModes).anyMatch(sm -> sm == showMode);
                })
                .map(field -> {
                    Column tc = field.getAnnotation(Column.class);
                    String displayName = isHidden ? tc.hiddenModeName() : tc.name();
                    if (StringUtils.isEmpty(displayName)) {
                        displayName = field.getName();
                    }

                    String[] foreground = isHidden ? tc.hiddenModeForeground() : tc.foreground();
                    List<Color> displayColor = new ArrayList<>();
                    if (foreground != null && foreground.length > 0) {
                        for (String color : foreground) {
                            if (StringUtils.isEmpty(color)) {
                                displayColor.add(null);
                            } else {
                                displayColor.add(JBColor.decode(color));
                            }
                        }
                    }
                    return new ColumnDefinition(field.getName(), displayName, tc.colorThreshold(), displayColor, tc.order(),
                            tc.enableNumberComparator(), tc.editable(), isHidden ? 60 : 70);
                })
                .sorted(Comparator.comparingInt(ColumnDefinition::getOrder))
                .collect(Collectors.toList());
    }

    public StockDataBean(StockConfig stockConfig, int index) {
        this.index = index;
        ReflectUtil.copy(stockConfig, this);
    }

    public void calculate() {
        try {

            double d = NumUtil.toDouble(high) - NumUtil.toDouble(low);
            range = NumUtil.formatDecimal(d, 2, 3);

            double hp = (NumUtil.toDouble(high) - NumUtil.toDouble(preClose)) / NumUtil.toDouble(preClose);
            highPercent = NumUtil.formatDecimal(hp * 100, 2, 2);

            double lp = (NumUtil.toDouble(low) - NumUtil.toDouble(preClose)) / NumUtil.toDouble(preClose);
            lowPercent = NumUtil.formatDecimal(lp * 100, 2, 2);

            // 计算1/3/5分钟涨跌幅
            PriceChangeCalculator calculator = PriceChangeCalculator.getInstance();
            SimpleDateFormat smt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = smt.parse("1970-01-01 " + time);

            // test start
            // double randomPrice = 100 + (Math.random() * 100);
            // currentPrice = String.format("%.2f", randomPrice);
            // Date date = new Date();
            // test end

            PriceChange changes = calculator.updatePrice(this.getSymbol(), date.getTime(), this.currentPrice);
            this.changePercentOfMin1 = changes.changePercentOfMin1;
            this.changePercentOfMin3 = changes.changePercentOfMin3;
            this.changePercentOfMin5 = changes.changePercentOfMin5;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }
}