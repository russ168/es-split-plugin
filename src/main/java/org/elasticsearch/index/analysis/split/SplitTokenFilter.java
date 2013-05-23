/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Author: dong ai hua <dongaihua1201@nhn.com>
 */

package org.elasticsearch.index.analysis.split;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.util.Set;

public class SplitTokenFilter extends TokenFilter {

    private final CharArraySet stopWords;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final TypeAttribute type = addAttribute(TypeAttribute.class);

    private static ESLogger logger = Loggers.getLogger("split");

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        if (type.type().equals("<HIGHLIGHT>")) return true;
        if (type.type().equals("<SPLIT>")) {
            String str = termAtt.toString();
            termAtt.setEmpty();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(htmlEncode(str));
            termAtt.resizeBuffer(stringBuilder.length());
            termAtt.append(stringBuilder.toString());
            termAtt.setLength(stringBuilder.length());
            return true;
        }

        String str = termAtt.toString();
        if (stopWords.contains(str.toLowerCase())) return true;
        termAtt.setEmpty();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<i>");
        stringBuilder.append(str);
        stringBuilder.append("</i>");

        termAtt.resizeBuffer(stringBuilder.length());
        termAtt.append(stringBuilder.toString());
        termAtt.setLength(stringBuilder.length());

        return true;
    }

    public SplitTokenFilter(TokenStream in, Set<?> stopWords) {
        super(in);
        this.stopWords = (CharArraySet) stopWords;
    }

    /**
     * Encode string into HTML
     */
    public final String htmlEncode(String plainText)
    {
        if (plainText == null || plainText.length() == 0)
        {
            return "";
        }

        StringBuilder result = new StringBuilder(plainText.length());

        for (int index=0; index<plainText.length(); index++)
        {
            char ch = plainText.charAt(index);

            switch (ch)
            {
                case '"':
                    result.append("&quot;");
                    break;

                case '&':
                    result.append("&amp;");
                    break;

                case '<':
                    result.append("&lt;");
                    break;

                case '>':
                    result.append("&gt;");
                    break;

                default:
                    result.append(ch);
            }
        }

        return result.toString();
    }
}