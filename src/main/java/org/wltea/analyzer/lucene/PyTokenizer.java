package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.py.IPyStrategy;
import org.wltea.analyzer.py.PyStrategyFactory;

public final class PyTokenizer extends Tokenizer {
    private IKSegmenter             _IKImplement;
    private final CharTermAttribute termAtt;
    private final OffsetAttribute   offsetAtt;
    private final TypeAttribute     typeAtt;
    private int                     endPosition;
    private IPyStrategy             pyStrategy;

    public PyTokenizer(final Reader in, final boolean useSmart, String type) {
        super(in);
        this.offsetAtt = this.addAttribute(OffsetAttribute.class);
        this.termAtt = this.addAttribute(CharTermAttribute.class);
        this.typeAtt = this.addAttribute(TypeAttribute.class);
        this._IKImplement = new IKSegmenter(this.input, useSmart);
        this.pyStrategy = PyStrategyFactory.getInstance(type);
    }

    @Override
    public boolean incrementToken() throws IOException {
        this.clearAttributes();
        final Lexeme nextLexeme = this._IKImplement.next();
        if (nextLexeme != null) {
            if ((nextLexeme.getLexemeType() == Lexeme.TYPE_CNCHAR) || (nextLexeme.getLexemeType() == Lexeme.TYPE_CNWORD) || (nextLexeme.getLexemeType() == Lexeme.TYPE_CNUM)) {
                String py = this.pyStrategy.toPy(nextLexeme.getLexemeText());
                this.termAtt.append(py);
                this.termAtt.setLength(py.length());
            } else {
                this.termAtt.append(nextLexeme.getLexemeText());
                this.termAtt.setLength(nextLexeme.getLength());
            }
            this.offsetAtt.setOffset(nextLexeme.getBeginPosition(), nextLexeme.getEndPosition());
            this.endPosition = nextLexeme.getEndPosition();
            this.typeAtt.setType(nextLexeme.getLexemeTypeString());
            return true;
        }
        return false;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this._IKImplement.reset(this.input);
    }

    @Override
    public final void end() {
        final int finalOffset = this.correctOffset(this.endPosition);
        this.offsetAtt.setOffset(finalOffset, finalOffset);
    }
}
