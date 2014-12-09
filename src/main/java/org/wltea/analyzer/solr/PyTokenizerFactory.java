package org.wltea.analyzer.solr;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.wltea.analyzer.lucene.PyTokenizer;

public class PyTokenizerFactory extends TokenizerFactory {
    private boolean useSmart = false;
    /**
     * @see org.wltea.analyzer.py.IPyStrategy的实现类命名
     */
    private String  type     = "All";

    @Override
    public void init(final Map<String, String> params) {
        super.init(params);
        this.useSmart = Boolean.parseBoolean(params.get("useSmart"));
        if (params.containsKey("type")) {
            this.type = params.get("type");
        }
    }

    @Override
    public Tokenizer create(final Reader in) {
        return new PyTokenizer(in, this.useSmart, this.type);
    }
}
