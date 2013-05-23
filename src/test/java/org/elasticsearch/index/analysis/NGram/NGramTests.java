package org.elasticsearch.index.analysis.NGram;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Dong ai hua
 * Date: 13-5-22
 * Time: 下午6:10
 * To change this template use File | Settings | File Templates.
 */
public class NGramTests {

    @Test
    public void testTokenFilter() throws IOException {
        String[] strings = new String[]
                {
                        "abcdefghijklmnopqrstuvwxyz0123456789"
                };
        String[] expected = new String[]
                {

                };

        Analyzer analyzer = new NGramAnalyzer();

        for (int i = 0, len = strings.length; i < len; i++) {
            StringReader sr = new StringReader(strings[i]);
            TokenStream stream = analyzer.tokenStream("f", sr);
            stream.reset();
            List<String> list = new ArrayList<String>();
            while (stream.incrementToken()) {
                CharTermAttribute ta = stream.getAttribute(CharTermAttribute.class);
                list.add(ta.toString());
                System.out.println(ta.toString());
            }
            //Joiner joiner = Joiner.on("");
            //System.out.println("Result:" + joiner.join(list));
            //Assert.assertEquals(joiner.join(list), expected[i]);
        }
    }
}