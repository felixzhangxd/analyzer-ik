package org.wltea.analyzer.py;

public interface IPyStrategy {
    /**
     * 中文 => 拼音
     */
    String toPy(String chinese);
}
