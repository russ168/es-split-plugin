package org.elasticsearch.index.analysis.NGram;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
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
public class NGram2Tests {

    @Test
    public void testTokenFilter() throws IOException {
        String[] strings = new String[]
                {
                        "abcdefghijklmnopqrstuvwxyz_-0123456789"
                };
        String[] expected = new String[]
                {

                };

         int totalLength = 8;
        String characters = "_-0123456789";
        for (int i = 0, len = strings.length; i < len; i++) {
            StringReader reader = new StringReader(strings[i]);
            final StandardTokenizer source =   new StandardTokenizer(Version.LUCENE_43, reader);
            TokenStream stream = new NGramTokenFilter(source, 3, totalLength);
            stream.reset();
            List<String> list = new ArrayList<String>();
            while (stream.incrementToken()) {
                CharTermAttribute ta = stream.getAttribute(CharTermAttribute.class);
                list.add(ta.toString());
                System.out.println(ta.toString() + genSuffix(totalLength - ta.toString().length(), characters));
            }
            //Joiner joiner = Joiner.on("");
            //System.out.println("Result:" + joiner.join(list));
            //Assert.assertEquals(joiner.join(list), expected[i]);
        }
    }

    private String genSuffix(int len, String characters) {
        StringBuffer buffer = new StringBuffer();
        int charactersLength = characters.length();
        for (int i = 0; i < len; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int)index));
        }
        return  buffer.toString();
    }
}