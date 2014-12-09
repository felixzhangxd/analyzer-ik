package org.wltea.analyzer.py;

public class PyStrategyFactory {
    public static final IPyStrategy getInstance(String type) {
        String packageName = PyStrategyFactory.class.getPackage().getName();
        String className = "PyStrategy" + type;
        try {
            return (IPyStrategy) Class.forName(packageName + "." + className).newInstance();
        } catch (Exception e) {}
        return new PyStrategyAll();
    }
}
