package org.wltea.analyzer.solr;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.wltea.analyzer.lucene.IKTokenizer;

/**
 * IK中文分词 Solr分词器工厂实现
 * 
 * @author felix
 */
public class IKTokenizerFactory extends TokenizerFactory {
    private boolean useSmart = false;

    @Override
    public void init(final Map<String, String> params) {
        super.init(params);
        this.useSmart = Boolean.parseBoolean(params.get("useSmart"));
    }

    @Override
    public Tokenizer create(final Reader in) {
        return new IKTokenizer(in, this.useSmart);
    }
}
