package org.wltea.analyzer.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;

public final class PyAnalyzer extends Analyzer {
    private boolean useSmart;
    /**
     * @see org.wltea.analyzer.py.PyStrategyFactory
     */
    private String  type;

    public PyAnalyzer(final boolean useSmart, String type) {
        this.useSmart = useSmart;
        this.type = type;
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader in) {
        final Tokenizer pyTokenizer = new PyTokenizer(in, this.useSmart, this.type);
        return new TokenStreamComponents(pyTokenizer);
    }
}
