package org.wltea.analyzer.py;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具类
 * 
 * @author felix
 */
class PyUtils {
    /** 拼音格式: */
    private static final HanyuPinyinOutputFormat PY_FORMAT = new HanyuPinyinOutputFormat(); ;
    static {
        PyUtils.PY_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);// 小写字母
        PyUtils.PY_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 无声调
        PyUtils.PY_FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);// ü 转 v
    }

    /**
     * c(unicode) 是否为中文
     * 例:'中' => true
     */
    private static boolean isChinese(char c) {
        return String.valueOf(c).matches("[\\u4E00-\\u9FFF]+");
    }

    /**
     * 中文 转 拼音首字母
     * 例: 天安门 => tam
     */
    static StringBuilder getPyFirst(String chinese) {
        StringBuilder builder = new StringBuilder(chinese.length());
        for (char c : chinese.toCharArray()) {
            if (PyUtils.isChinese(c)) {
                builder.append(PinyinHelper.toHanyuPinyinStringArray(c)[0].charAt(0));
            }
        }
        return builder;
    }

    /**
     * 中文 转 全拼
     * 例: 天安门 => tiananmen
     */
    static StringBuilder getPyQuan(String chinese) {
        StringBuilder builder = new StringBuilder();
        for (char c : chinese.toCharArray()) {
            if (PyUtils.isChinese(c)) {
                try {
                    builder.append(PinyinHelper.toHanyuPinyinStringArray(c, PyUtils.PY_FORMAT)[0]);
                } catch (BadHanyuPinyinOutputFormatCombination e) {}
            }
        }
        return builder;
    }

    /**
     * 中文 转 拼音首字母+全拼
     * 例: 天安门 => tiananmen tam
     */
    static StringBuilder getPyAll(String chinese) {
        StringBuilder builder = PyUtils.getPyFirst(chinese).append(" ");
        builder.append(PyUtils.getPyQuan(chinese));
        return builder;
    }
}
