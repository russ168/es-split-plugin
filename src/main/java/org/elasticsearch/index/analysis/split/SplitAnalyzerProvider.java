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
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.analysis.Analysis;
import org.elasticsearch.index.settings.IndexSettings;

public class SplitAnalyzerProvider extends AbstractIndexAnalyzerProvider<SplitAnalyzer> {

    private final SplitAnalyzer analyzer;

    @Inject
    public SplitAnalyzerProvider(Index index, @IndexSettings Settings indexSettings, Environment env, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);
        CharArraySet stopWords = Analysis.parseStopWords(env, settings, SplitAnalyzer.getDefaultStopSet(), version);
        analyzer = new SplitAnalyzer(version, stopWords);
    }

    @Override
    public SplitAnalyzer get() {
        return this.analyzer;
    }
}
