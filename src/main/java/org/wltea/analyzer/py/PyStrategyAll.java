package org.wltea.analyzer.py;

public class PyStrategyAll implements IPyStrategy {
    public PyStrategyAll() {}

    /**
     * 中文 => 拼音字字母 + 全拼
     */
    @Override
    public String toPy(String chinese) {
        return PyUtils.getPyAll(chinese).toString();
    }
}
