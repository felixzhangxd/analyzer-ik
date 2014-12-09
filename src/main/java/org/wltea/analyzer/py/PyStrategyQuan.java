package org.wltea.analyzer.py;

public class PyStrategyQuan implements IPyStrategy {
    public PyStrategyQuan() {}

    /**
     * 中文 => 全拼
     */
    @Override
    public String toPy(String chinese) {
        return PyUtils.getPyQuan(chinese).toString();
    }
}
