/**
 * IK 中文分词 版本 5.0
 * IK Analyzer release 5.0
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 源代码由林良益(linliangyi2005@gmail.com)提供
 * 版权声明 2012，乌龙茶工作室
 * provided by Linliangyi and copyright 2012 by Oolong studio
 */
package org.wltea.analyzer.dic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

import org.wltea.analyzer.cfg.Configuration;

/**
 * 词典管理类,单子模式
 */
public class Dictionary {
    /*
     * 词典单子实例
     */
    private static Dictionary singleton;
    /*
     * 主词典对象
     */
    private DictSegment       _MainDict;
    /*
     * 停止词词典
     */
    private DictSegment       _StopWordDict;
    /*
     * 量词词典
     */
    private DictSegment       _QuantifierDict;
    /**
     * 配置对象
     */
    private Configuration     cfg;

    private Dictionary(final Configuration cfg) {
        this.cfg = cfg;
        this.loadMainDict();
        this.loadStopWordDict();
        this.loadQuantifierDict();
    }

    /**
     * 词典初始化
     * 由于IK Analyzer的词典采用Dictionary类的静态方法进行词典初始化
     * 只有当Dictionary类被实际调用时，才会开始载入词典，
     * 这将延长首次分词操作的时间
     * 该方法提供了一个在应用加载阶段就初始化字典的手段
     * 
     * @return Dictionary
     */
    public static Dictionary initial(final Configuration cfg) {
        if (Dictionary.singleton == null) {
            synchronized (Dictionary.class) {
                if (Dictionary.singleton == null) {
                    Dictionary.singleton = new Dictionary(cfg);
                    return Dictionary.singleton;
                }
            }
        }
        return Dictionary.singleton;
    }

    /**
     * 获取词典单子实例
     * 
     * @return Dictionary 单例对象
     */
    public static Dictionary getSingleton() {
        if (Dictionary.singleton == null) {
            throw new IllegalStateException("词典尚未初始化，请先调用initial方法");
        }
        return Dictionary.singleton;
    }

    /**
     * 批量加载新词条
     * 
     * @param words
     *            Collection<String>词条列表
     */
    public void addWords(final Collection<String> words) {
        if (words != null) {
            for (final String word : words) {
                if (word != null) {
                    // 批量加载词条到主内存词典中
                    Dictionary.singleton._MainDict.fillSegment(word.trim().toLowerCase().toCharArray());
                }
            }
        }
    }

    /**
     * 批量移除（屏蔽）词条
     * 
     * @param words
     */
    public void disableWords(final Collection<String> words) {
        if (words != null) {
            for (final String word : words) {
                if (word != null) {
                    // 批量屏蔽词条
                    Dictionary.singleton._MainDict.disableSegment(word.trim().toLowerCase().toCharArray());
                }
            }
        }
    }

    /**
     * 检索匹配主词典
     * 
     * @param charArray
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(final char[] charArray) {
        return Dictionary.singleton._MainDict.match(charArray);
    }

    /**
     * 检索匹配主词典
     * 
     * @param charArray
     * @param begin
     * @param length
     * @return Hit 匹配结果描述
     */
    public Hit matchInMainDict(final char[] charArray, final int begin, final int length) {
        return Dictionary.singleton._MainDict.match(charArray, begin, length);
    }

    /**
     * 检索匹配量词词典
     * 
     * @param charArray
     * @param begin
     * @param length
     * @return Hit 匹配结果描述
     */
    public Hit matchInQuantifierDict(final char[] charArray, final int begin, final int length) {
        return Dictionary.singleton._QuantifierDict.match(charArray, begin, length);
    }

    /**
     * 从已匹配的Hit中直接取出DictSegment，继续向下匹配
     * 
     * @param charArray
     * @param currentIndex
     * @param matchedHit
     * @return Hit
     */
    public Hit matchWithHit(final char[] charArray, final int currentIndex, final Hit matchedHit) {
        final DictSegment ds = matchedHit.getMatchedDictSegment();
        return ds.match(charArray, currentIndex, 1, matchedHit);
    }

    /**
     * 判断是否是停止词
     * 
     * @param charArray
     * @param begin
     * @param length
     * @return boolean
     */
    public boolean isStopWord(final char[] charArray, final int begin, final int length) {
        return Dictionary.singleton._StopWordDict.match(charArray, begin, length).isMatch();
    }

    /**
     * 加载主词典及扩展词典
     */
    private void loadMainDict() {
        // 建立一个主词典实例
        this._MainDict = new DictSegment((char) 0);
        // 读取主词典文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(this.cfg.getMainDictionary());
        if (is == null) {
            throw new RuntimeException("Main Dictionary not found!!!");
        }
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if ((theWord != null) && !"".equals(theWord.trim())) {
                    this._MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);
        } catch (final IOException ioe) {
            System.err.println("Main Dictionary loading exception.");
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        // 加载扩展词典
        this.loadExtDict();
    }

    /**
     * 加载用户配置的扩展词典到主词库表
     */
    private void loadExtDict() {
        // 加载扩展词典配置
        final List<String> extDictFiles = this.cfg.getExtDictionarys();
        if (extDictFiles != null) {
            InputStream is = null;
            for (final String extDictName : extDictFiles) {
                // 读取扩展词典文件
                System.out.println("加载扩展词典：" + extDictName);
                is = this.getClass().getClassLoader().getResourceAsStream(extDictName);
                // 如果找不到扩展的字典，则忽略
                if (is == null) {
                    continue;
                }
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                    String theWord = null;
                    do {
                        theWord = br.readLine();
                        if ((theWord != null) && !"".equals(theWord.trim())) {
                            // 加载扩展词典数据到主内存词典中
                            // System.out.println(theWord);
                            this._MainDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    } while (theWord != null);
                } catch (final IOException ioe) {
                    System.err.println("Extension Dictionary loading exception.");
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载用户扩展的停止词词典
     */
    private void loadStopWordDict() {
        // 建立一个主词典实例
        this._StopWordDict = new DictSegment((char) 0);
        // 加载扩展停止词典
        final List<String> extStopWordDictFiles = this.cfg.getExtStopWordDictionarys();
        if (extStopWordDictFiles != null) {
            InputStream is = null;
            for (final String extStopWordDictName : extStopWordDictFiles) {
                System.out.println("加载扩展停止词典：" + extStopWordDictName);
                // 读取扩展词典文件
                is = this.getClass().getClassLoader().getResourceAsStream(extStopWordDictName);
                // 如果找不到扩展的字典，则忽略
                if (is == null) {
                    continue;
                }
                try {
                    final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
                    String theWord = null;
                    do {
                        theWord = br.readLine();
                        if ((theWord != null) && !"".equals(theWord.trim())) {
                            // System.out.println(theWord);
                            // 加载扩展停止词典数据到内存中
                            this._StopWordDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                        }
                    } while (theWord != null);
                } catch (final IOException ioe) {
                    System.err.println("Extension Stop word Dictionary loading exception.");
                    ioe.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                            is = null;
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 加载量词词典
     */
    private void loadQuantifierDict() {
        // 建立一个量词典实例
        this._QuantifierDict = new DictSegment((char) 0);
        // 读取量词词典文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(this.cfg.getQuantifierDicionary());
        if (is == null) {
            throw new RuntimeException("Quantifier Dictionary not found!!!");
        }
        try {
            final BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
            String theWord = null;
            do {
                theWord = br.readLine();
                if ((theWord != null) && !"".equals(theWord.trim())) {
                    this._QuantifierDict.fillSegment(theWord.trim().toLowerCase().toCharArray());
                }
            } while (theWord != null);
        } catch (final IOException ioe) {
            System.err.println("Quantifier Dictionary loading exception.");
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                    is = null;
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
