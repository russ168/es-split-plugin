/*
* Licensed to ElasticSearch and Shay Banon under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. ElasticSearch licenses this
* file to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

/**
 * Author: dong ai hua <dongaihua1201@nhn.com>
 */

package org.elasticsearch.index.analysis.split;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.lucene.Lucene;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.hamcrest.MatcherAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.hamcrest.Matchers.instanceOf;

/**
 */
public class AnalysisTests {

    @Test
    public void testSplitAnalysis() {
        Index index = new Index("NGram");

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(EMPTY_SETTINGS), new EnvironmentModule(new Environment(EMPTY_SETTINGS))).createInjector();
        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, EMPTY_SETTINGS),
                new IndexNameModule(index),
                new AnalysisModule(EMPTY_SETTINGS, parentInjector.getInstance(IndicesAnalysisService.class)).addProcessor(new SplitAnalysisBinderProcessor()))
                .createChildInjector(parentInjector);

        AnalysisService analysisService = injector.getInstance(AnalysisService.class);

        Analyzer analyzer = analysisService.analyzer("split").analyzer();
        MatcherAssert.assertThat(analyzer, instanceOf(SplitAnalyzer.class));

        TokenizerFactory tokenizerFactory = analysisService.tokenizer("split");
        MatcherAssert.assertThat(tokenizerFactory, instanceOf(SplitTokenizerFactory.class));
    }

    @Test
    public void testTokenFilter() throws IOException {
        String[] strings = new String[]
                {
                        "<em>abc</em>def",
                        "<em>this is just a NGram</em>abc<em>def</em>This is",
                        "刘德华 dong ai hua <em>just</em> the NGram",
                        "xsflsy02.sa.nhnsystem.com",
                        "nomad::Job::ReturnAnswer:163",
                        "2013-01-10 06:29:07 +0000",
                        "<123>456",
                        "<em>NNB</em>" +
                                "=ULPUSFCVXBNFC " +
                                "NB=GYYDSNJYGIYTEMBW npic=" +
                                "<em>SFCuenZVHbx0RFZFoh+a0WALs7qRAYM/3vD26gfSTs4O8u/7rIqsl9I5OnJV9LgnCA</em> " +
                                "page_uid=RT+2zc5Y7tlssvof8wCsssssstZ-140745 " +
                                "BMR= nid_inf=438366115 NID_MATCH_M=1 " +
                                "NID_AUT=<em>95R9DDUsQ6SpQrid2Qpfe0s5BsyH6VRO0jBmpZ/Nmq4TrgPddxY8gUzhTVFyhECwFBBH6tnpd8YslNUK+ARdKEOJSwxM7HOspmslEoVHHHDgTqdfF60lI8opO9JKWVFaAnswVnIFNHTdHUdaCeFvSQ<em> " +
                                "NID_SES=<em>AAABRQjdQk1opAW5ceebr50CEmXN7HrzMrImW4FrXlJACJ1QU2fYDyjIpO/cO/2/+iM0BwZTnLgX4EClbkFwar9MJDr/+0dfX91dMvXV+8WuyiCiWxCWd4FwrsCMHcXthwQGV+C1bCrbU+5C/qeOeGuJCGwVt769y8+Tuy+KBuTGbKDMuUF/SyRq5IwNQ3YL1pMGs+cAnFN2xqFplgJtZvlhI8+8f3GfMxZqEHlXmSSlSpCWkZZYzz9wx2WarU+WtU4WGpnW0Y+Kc347mW2mNaVIDq+AHf4HXE8JHsPqvlzNWlkyS5AHw3tc5bWFy0MhxngOnyG7VqTheb4yxPRhTY0D6fF4TDPr7fjsJ5tuA9oxH+BGuoy6uYIs8uoRI1+HULgI0WCQpiNeVtI1eskacsENBnqECJ3OOyAFzAcr9msv7pr8LYtx0TsNVlLWVS7ug1uH5w</em> " +
                                "ncvid=#vid#_118.217.45.216p3Lj WMONID=DEZg20K2BGS ncvc2=7c1ce4c094a2a31133c5b88ab14e2e56eda35ebba8bf21da60ba865aeeca2ee728d016cd172bbf93e37c2bf73b9136e8073a1f11e2d0ab9cf43394518fbf0ec3adaba8a9b6abb4aba4a0a3a4a1a6b615 nci4=0337dafeeaa7c87a25cb8c9b96771b78d997768ada8665b7478abf4dfaff3ac3c336f650f4ba5c697e8fb3613570e67cd88ff44bafb0f9e0ca00aa61b78337fa95b1bc9bba8bb9b7b691b485cdbeae8da997b3aba285a091e6919cbc98a9ea9c93b78ebff2838aad88b9878b82a580ce8083848988888b8cb9 JSESSIONID=E365D0634FED26492BFFD5DEEE789B66 personaconmain|ektmfrl645=AE8BC98FD74D619FF7B13C83191E1F5EAFCD0F25C43D6BDC693E26D777419A2F845E79DA02B04219 personacon|ektmfrl645= cafeCookieToken=5KCBru-K8k8aHwkbio4dPmLlMyK6WlPYqN0319U4UeImDS9UVPpo70IVLHK9eybq6eJc-rNfllMgB5Fk_i2j-rKM1mCuoOqZ ncu=82b94171693746ae8766724d5696dc1a83e17aed"

                };
        String[] expected = new String[]
                {
                        "<em>abc</em><i>def</i>",
                        "<em>this is just a NGram</em><i>abc</i><em>def</em>This is",
                        "<i>刘</i><i>德</i><i>华</i> <i>dong</i> <i>ai</i> <i>hua</i> <em>just</em> the <i>NGram</i>",
                        "<i>xsflsy02</i>.<i>sa.nhnsystem.com</i>",
                        "<i>nomad</i>::<i>Job</i>::<i>ReturnAnswer</i>:<i>163</i>",
                        "<i>2013</i>-<i>01</i>-<i>10</i> <i>06</i>:<i>29</i>:<i>07</i> +<i>0000</i>",
                        "&lt;<i>123</i>&gt;<i>456</i>",
                        "<em>NNB</em>=<i>ULPUSFCVXBNFC</i> <i>NB</i>=<i>GYYDSNJYGIYTEMBW</i> <i>npic</i>=<em>SFCuenZVHbx0RFZFoh+a0WALs7qRAYM/3vD26gfSTs4O8u/7rIqsl9I5OnJV9LgnCA</em> <i>page_uid</i>=<i>RT</i>+<i>2zc5Y7tlssvof8wCsssssstZ</i>-<i>140745</i> <i>BMR</i>= <i>nid_inf</i>=<i>438366115</i> <i>NID_MATCH_M</i>=<i>1</i> <i>NID_AUT</i>= <i>ncvid</i>=#<i>vid</i>#<i>_118.217.45.216p3Lj</i> <i>WMONID</i>=<i>DEZg20K2BGS</i> <i>ncvc2</i>=<i>7c1ce4c094a2a31133c5b88ab14e2e56eda35ebba8bf21da60ba865aeeca2ee728d016cd172bbf93e37c2bf73b9136e8073a1f11e2d0ab9cf43394518fbf0ec3adaba8a9b6abb4aba4a0a3a4a1a6b615</i> <i>nci4</i>=<i>0337dafeeaa7c87a25cb8c9b96771b78d997768ada8665b7478abf4dfaff3ac3c336f650f4ba5c697e8fb3613570e67cd88ff44bafb0f9e0ca00aa61b78337fa95b1bc9bba8bb9b7b691b485cdbeae8da997b3aba285a091e6919cbc98a9ea9c93b78ebff2838aad88b9878b82a580ce8083848988888b8cb9</i> <i>JSESSIONID</i>=<i>E365D0634FED26492BFFD5DEEE789B66</i> <i>personaconmain</i>|<i>ektmfrl645</i>=<i>AE8BC98FD74D619FF7B13C83191E1F5EAFCD0F25C43D6BDC693E26D777419A2F845E79DA02B04219</i> <i>personacon</i>|<i>ektmfrl645</i>= <i>cafeCookieToken</i>=<i>5KCBru</i>-<i>K8k8aHwkbio4dPmLlMyK6WlPYqN0319U4UeImDS9UVPpo70IVLHK9eybq6eJc</i>-<i>rNfllMgB5Fk_i2j</i>-<i>rKM1mCuoOqZ</i> <i>ncu</i>=<i>82b94171693746ae8766724d5696dc1a83e17aed</i>"

                };

       Analyzer analyzer = new SplitAnalyzer(Lucene.ANALYZER_VERSION);

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
            Joiner joiner = Joiner.on("");
            System.out.println("Result:" + joiner.join(list));
            Assert.assertEquals(joiner.join(list), expected[i]);
        }
    }
}