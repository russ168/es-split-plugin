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

package org.elasticsearch.plugin.splithighlight;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.split.SplitAnalysisBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.highlight.HighlightModule;
import org.elasticsearch.search.highlight.SplitHighlighter;

/**
 */
public class SplitHighlightPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "highlight-split";
    }

    @Override
    public String description() {
        return "highlight and split function";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new SplitAnalysisBinderProcessor());
    }

    public void onModule(HighlightModule module) {
        module.registerHighlighter(SplitHighlighter.class);
    }

}
