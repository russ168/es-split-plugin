package org.elasticsearch.search.highlight;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.highlight.Formatter;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.io.FastStringReader;
import org.elasticsearch.common.lucene.search.XFilteredQuery;
import org.elasticsearch.common.text.StringText;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.fieldvisitor.CustomFieldsVisitor;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.FetchPhaseExecutionException;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.search.lookup.SearchLookup;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * User: Dong ai hua
 * Date: 13-5-20
 * Time: 上午11:02
 * To change this template use File | Settings | File Templates.
 */
public class SplitHighlighter implements Highlighter {

    private static final String CACHE_KEY = "highlight-split";

    @Override
    public String[] names() {
        return new String[] { "split", "split-highlighter" };
    }

    public HighlightField highlight(HighlighterContext highlighterContext) {
        SearchContextHighlight.Field field = highlighterContext.field;
        SearchContext context = highlighterContext.context;
        FetchSubPhase.HitContext hitContext = highlighterContext.hitContext;
        FieldMapper<?> mapper = highlighterContext.mapper;

        Encoder encoder = Encoders.DEFAULT;

        if (!hitContext.cache().containsKey(CACHE_KEY)) {
            Map<FieldMapper, Highlighter> mappers = Maps.newHashMap();
            hitContext.cache().put(CACHE_KEY, mappers);
        }
        Map<FieldMapper, org.apache.lucene.search.highlight.Highlighter> cache = (Map<FieldMapper, org.apache.lucene.search.highlight.Highlighter>) hitContext.cache().get(CACHE_KEY);

        org.apache.lucene.search.highlight.Highlighter entry = cache.get(mapper);
        if (entry == null) {
            // Don't use the context.query() since it might be rewritten, and we need to pass the non rewritten queries to
            // let the highlighter handle MultiTerm ones
            Query query = context.parsedQuery().query();

            //Start of Jackie
            Query newQuery = rewrite(context, query, field, mapper);
            QueryScorer queryScorer = new CustomQueryScorer(newQuery, field.requireFieldMatch() ? mapper.names().indexName() : null);
            //End of Jackie

            queryScorer.setExpandMultiTermQuery(true);
            Fragmenter fragmenter;
            if (field.numberOfFragments() == 0) {
                fragmenter = new NullFragmenter();
            } else if (field.fragmenter() == null) {
                fragmenter = new SimpleSpanFragmenter(queryScorer, field.fragmentCharSize());
            } else if ("simple".equals(field.fragmenter())) {
                fragmenter = new SimpleFragmenter(field.fragmentCharSize());
            } else if ("span".equals(field.fragmenter())) {
                fragmenter = new SimpleSpanFragmenter(queryScorer, field.fragmentCharSize());
            } else {
                throw new ElasticSearchIllegalArgumentException("unknown fragmenter option [" + field.fragmenter() + "] for the field [" + highlighterContext.fieldName + "]");
            }
            Formatter formatter = new SimpleHTMLFormatter(field.preTags()[0], field.postTags()[0]);

            entry = new org.apache.lucene.search.highlight.Highlighter(formatter, encoder, queryScorer);
            entry.setTextFragmenter(fragmenter);
            // always highlight across all data
            entry.setMaxDocCharsToAnalyze(Integer.MAX_VALUE);

            cache.put(mapper, entry);
        }

        List<Object> textsToHighlight;
        if (mapper.fieldType().stored()) {
            try {
                CustomFieldsVisitor fieldVisitor = new CustomFieldsVisitor(ImmutableSet.of(mapper.names().indexName()), false);
                hitContext.reader().document(hitContext.docId(), fieldVisitor);
                textsToHighlight = fieldVisitor.fields().get(mapper.names().indexName());
            } catch (Exception e) {
                throw new FetchPhaseExecutionException(context, "Failed to highlight field [" + highlighterContext.fieldName + "]", e);
            }
        } else {
            SearchLookup lookup = context.lookup();
            lookup.setNextReader(hitContext.readerContext());
            lookup.setNextDocId(hitContext.docId());
            textsToHighlight = lookup.source().extractRawValues(mapper.names().sourcePath());
        }

        // a HACK to make highlighter do highlighting, even though its using the single frag list builder
        int numberOfFragments = field.numberOfFragments() == 0 ? 1 : field.numberOfFragments();
        ArrayList<TextFragment> fragsList = new ArrayList<TextFragment>();
        try {
            for (Object textToHighlight : textsToHighlight) {
                String text = textToHighlight.toString();
                Analyzer analyzer = context.mapperService().documentMapper(hitContext.hit().type()).mappers().indexAnalyzer();
                TokenStream tokenStream = analyzer.tokenStream(mapper.names().indexName(), new FastStringReader(text));
                tokenStream.reset();
                TextFragment[] bestTextFragments = entry.getBestTextFragments(tokenStream, text, false, numberOfFragments);
                for (TextFragment bestTextFragment : bestTextFragments) {
                    //Start of Jackie
                    //if (bestTextFragment != null && bestTextFragment.getScore() > 0) {
                    if (bestTextFragment != null) {
                        //End of Jackie
                        fragsList.add(bestTextFragment);
                    }
                }
            }
        } catch (Exception e) {
            throw new FetchPhaseExecutionException(context, "Failed to highlight field [" + highlighterContext.fieldName + "]", e);
        }
        if (field.scoreOrdered()) {
            Collections.sort(fragsList, new Comparator<TextFragment>() {
                public int compare(TextFragment o1, TextFragment o2) {
                    return Math.round(o2.getScore() - o1.getScore());
                }
            });
        }
        String[] fragments = null;
        // number_of_fragments is set to 0 but we have a multivalued field
        if (field.numberOfFragments() == 0 && textsToHighlight.size() > 1 && fragsList.size() > 0) {
            fragments = new String[fragsList.size()];
            for (int i = 0; i < fragsList.size(); i++) {
                fragments[i] = fragsList.get(i).toString();
            }
        } else {
            // refine numberOfFragments if needed
            numberOfFragments = fragsList.size() < numberOfFragments ? fragsList.size() : numberOfFragments;
            fragments = new String[numberOfFragments];
            for (int i = 0; i < fragments.length; i++) {
                //Start of Jackie
                fragments[i] = fragsList.get(i).toString();
                try {
                    fragments[i] = split(context, mapper, fragsList.get(i).toString());
                } catch (IOException e) {
                    fragments[i] = fragsList.get(i).toString();
                }
                //End of Jackie
            }
        }

        if (fragments != null && fragments.length > 0) {
            return new HighlightField(highlighterContext.fieldName, StringText.convertFromStringArray(fragments));
        }

        return null;
    }

    private static class Encoders {
        public static Encoder DEFAULT = new DefaultEncoder();
        public static Encoder HTML = new SimpleHTMLEncoder();
    }

    //Start of Jackie
    private Query rewrite(SearchContext context, Query query, SearchContextHighlight.Field field, FieldMapper mapper) {
        if (!field.requireFieldMatch()) return query;
        Query newQuery = query;
        String fd = mapper.names().indexName();
        if (query instanceof FilteredQuery) {
            newQuery = ((FilteredQuery) query).getQuery();
        } else if (query instanceof XFilteredQuery) {
            newQuery = ((XFilteredQuery) query).getQuery();
        }
        return rewriteQuery(context, newQuery, fd);
    }

    private Query rewriteQuery(SearchContext context, Query query, String field) {
        if (query instanceof TermQuery) {
            String field1 = ((TermQuery) query).getTerm().field();
            if (field1.equals("_all")) {
                String value = ((TermQuery) query).getTerm().text();
                TermQueryBuilder queryBuilder = new TermQueryBuilder(field, value);
                return context.queryParserService().parse(queryBuilder).query();
            }
        } else if (query instanceof BooleanQuery) {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            BooleanClause[] clauses = booleanQuery.getClauses();
            BooleanQuery newBooleanQuery = new BooleanQuery(booleanQuery.isCoordDisabled());
            for (BooleanClause clause : clauses) {
                Query clauseQuery = clause.getQuery();
                BooleanClause.Occur occur = clause.getOccur();
                newBooleanQuery.add(new BooleanClause(rewriteQuery(context, clauseQuery, field), occur));
            }
            return newBooleanQuery;
        } else if (query instanceof PhraseQuery) {
            Term[] terms = ((PhraseQuery) query).getTerms();
            PhraseQuery newPhraseQuery = new PhraseQuery();
            for (Term term : terms) {
                if (term.field().equals("_all")) {
                    newPhraseQuery.add(new Term(field, term.text()));
                } else {
                    newPhraseQuery.add(term);
                }
            }
            return newPhraseQuery;
        }

        return query;
    }

    private String split(SearchContext context, FieldMapper mapper, String content) throws IOException {
        if (!mapper.fieldType().tokenized()) {
            if (content.startsWith("<em>")) {
                return content;
            } else {
                return "<i>" + content + "</i>";
            }
        }
        AnalysisService analysisService = context.analysisService();
        Analyzer analyzer = analysisService.analyzer("split");
        TokenStream stream = analyzer.tokenStream(mapper.names().indexName(), new StringReader(content));
        stream.reset();
        List<String> list = new ArrayList<String>();
        while (stream.incrementToken()) {
            CharTermAttribute ta = stream.getAttribute(CharTermAttribute.class);
            list.add(ta.toString());
        }
        Joiner joiner = Joiner.on("");
        return joiner.join(list);
    }
    //End of Jackie
}
