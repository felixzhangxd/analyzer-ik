package org.wltea.analyzer.py;

public class PyStrategyFirst implements IPyStrategy {
    public PyStrategyFirst() {}

    @Override
    public String toPy(String chinese) {
        return PyUtils.getPyFirst(chinese).toString();
    }
}
