/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

package ecmwf.common.ai;

/**
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon <syi@ecmwf.int>, ECMWF.
 * @version 7.4.0
 * @since 2026-03-12
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.VectorSimilarityFunction;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;

/**
 * <p>
 * A Lucene-based Retrieval-Augmented Generation (RAG) builder and search engine. Supports BM25 keyword search, vector
 * similarity search, and hybrid ranking using Reciprocal Rank Fusion (RRF). Designed for memory-efficient, operational
 * indexing of large document collections.
 * </p>
 *
 * <h2>Features:</h2>
 * <ul>
 * <li>BM25 keyword search with Lucene {@link StandardAnalyzer} and {@link QueryParser}</li>
 * <li>Vector similarity search using embeddings and cosine similarity</li>
 * <li>Hybrid ranking via {@link #reciprocalRankFusion(ScoreDoc[], ScoreDoc[], int)}</li>
 * <li>Incremental, memory-efficient indexing using paragraph splitting and batch embedding</li>
 * <li>Metadata support including filename, source path, paragraph index, and preview (first sentence)</li>
 * <li>Failure-tolerant indexing: logs individual document/paragraph errors instead of throwing</li>
 * <li>Configurable RRF parameter to adjust fusion weighting</li>
 * </ul>
 *
 * <h2>Thread-safety:</h2>
 * <ul>
 * <li>{@link #search(String, int)} is thread-safe (read-only IndexSearcher)</li>
 * <li>Indexing is single-threaded and not safe for concurrent writes</li>
 * </ul>
 *
 * <h2>Typical usage:</h2>
 *
 * <pre>{@code
 * Path indexPath = Path.of("data/lucene-index");
 * EmbeddingModel embeddingModel = ...; // your embedding model
 * RagBuilder rag = new RagBuilder(indexPath, embeddingModel, 64, 60);
 * rag.buildIfNeeded(Path.of("data/docs"), path -> path.toString().endsWith(".md"));
 * List<TextSegment> results = rag.search("example query", 5);
 * }</pre>
 *
 * <p>
 * Designed for moderate to large collections. For very large corpora, consider incremental or distributed
 * indexing/search solutions.
 * </p>
 *
 * @author Laurent Gougeon
 */
public class RagBuilder {

    private static final Logger LOG = LogManager.getLogger(RagBuilder.class);

    /**
     * The embedding model used to generate vector representations of text segments
     */
    private final EmbeddingModel embeddingModel;

    /**
     * Batch size used for embedding segments; larger batch improves throughput but increases memory usage
     */
    private final int embeddingBatch;

    /**
     * Smoothing parameter for Reciprocal Rank Fusion (RRF); higher values give more weight to top-ranked docs
     */
    private final int rrfK;

    /** Lucene index directory */
    private Directory directory;

    /** Lucene searcher for read-only queries */
    private IndexSearcher searcher;

    /** Analyzer used for BM25 keyword search */
    private final Analyzer analyzer = new StandardAnalyzer();

    /** Query parser for BM25 keyword queries */
    private final QueryParser queryParser;

    /**
     * Constructs a new RagBuilder.
     *
     * @param indexPath
     *            path to store the Lucene index
     * @param embeddingModel
     *            embedding model for semantic search
     * @param embeddingBatch
     *            batch size for embedding segments
     * @param rrfK
     *            smoothing parameter for Reciprocal Rank Fusion
     */
    public RagBuilder(final Path indexPath, final EmbeddingModel embeddingModel, final int embeddingBatch,
            final int rrfK) {
        this.embeddingModel = embeddingModel;
        this.embeddingBatch = embeddingBatch;
        this.queryParser = new QueryParser("content", analyzer);
        this.rrfK = rrfK;
        try {
            Files.createDirectories(indexPath);
            directory = FSDirectory.open(indexPath);
            if (DirectoryReader.indexExists(directory)) {
                LOG.info("Loading existing RAG index");
                searcher = new IndexSearcher(DirectoryReader.open(directory));
                searcher.setSimilarity(new BM25Similarity(1.2f, 0.9f));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to initialize RAG index at " + indexPath, e);
        }
    }

    /**
     * Builds the RAG index if it does not already exist.
     *
     * <p>
     * Uses paragraph splitting, batch embedding, and incremental indexing. Each Lucene document stores the text,
     * embedding, and metadata including preview.
     * </p>
     *
     * @param docsPath
     *            the root directory containing documents
     * @param filter
     *            predicate to select files; if null, all files are indexed
     */
    public void buildIfNeeded(final Path docsPath, final Predicate<Path> filter) {
        if (searcher != null) {
            LOG.info("RAG index already available");
            return;
        }
        LOG.info("Building RAG index from {}", docsPath);
        buildIndex(docsPath, filter);
    }

    /**
     * Builds the Lucene index from documents.
     *
     * <p>
     * Splits documents into paragraphs, embeds segments in batches, and indexes both text and vectors. Stores metadata
     * including filename, sourcePath, paragraph index, and first sentence preview.
     * </p>
     *
     * @param docsPath
     *            document root path
     * @param filter
     *            file filter predicate
     */
    private void buildIndex(final Path docsPath, final Predicate<Path> filter) {
        final var splitter = new DocumentByParagraphSplitter(900, 150);
        try (var writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
            try (var paths = Files.walk(docsPath)) {
                paths.filter(Files::isRegularFile).filter(filter != null ? filter : _ -> true).forEach(path -> {
                    try {
                        LOG.debug("Processing file: {}", path);
                        final var content = Files.readString(path);
                        final var doc = dev.langchain4j.data.document.Document.from(content);
                        final var segments = splitter.split(doc);
                        for (var i = 0; i < segments.size(); i += embeddingBatch) {
                            final var batchStart = i;
                            final var batch = segments.subList(i, Math.min(i + embeddingBatch, segments.size()));
                            final var embeddings = embeddingModel.embedAll(batch).content();
                            IntStream.range(0, batch.size()).forEach(j -> {
                                final var seg = batch.get(j);
                                final var vector = normalize(embeddings.get(j).vector());
                                final var luceneDoc = new org.apache.lucene.document.Document();
                                final var enrichedText = "FILE: " + path.getFileName() + " | PARAGRAPH: "
                                        + (batchStart + j) + "\nPATH: " + path + "\n\n" + seg.text();
                                final var preview = seg.text().split("\\.")[0] + ".";
                                luceneDoc.add(new TextField("content", enrichedText, Field.Store.YES));
                                luceneDoc.add(
                                        new KnnFloatVectorField("embedding", vector, VectorSimilarityFunction.COSINE));
                                luceneDoc.add(new StoredField("sourcePath", path.toString()));
                                luceneDoc.add(
                                        new StringField("filename", path.getFileName().toString(), Field.Store.YES));
                                luceneDoc.add(new IntPoint("paragraphIndex", batchStart + j));
                                luceneDoc.add(new StoredField("paragraphIndex", batchStart + j));
                                luceneDoc.add(new StoredField("preview", preview));
                                try {
                                    writer.addDocument(luceneDoc);
                                } catch (final IOException e) {
                                    LOG.error("Failed to add document from {} paragraph {}", path, batchStart + j, e);
                                }
                            });
                        }
                    } catch (final IOException e) {
                        LOG.warn("Failed reading {}", path, e);
                    }
                });
            }
            writer.commit();
            searcher = new IndexSearcher(DirectoryReader.open(directory));
            searcher.setSimilarity(new BM25Similarity(1.2f, 0.9f));
            LOG.info("RAG index built successfully");
        } catch (final IOException e) {
            LOG.error("Failed to walk documents path {}", docsPath, e);
            return; // skip building
        }
    }

    /**
     * Performs hybrid search over BM25 and vector embeddings.
     *
     * <p>
     * Dynamically adjusts candidate pool size for longer queries. Returns top-k segments ranked by combined relevance.
     * Each segment includes metadata: filename, sourcePath, paragraph index, and preview.
     * </p>
     *
     * @param query
     *            the user query
     * @param topK
     *            maximum number of results to return
     *
     * @return list of top-k TextSegments; empty if search fails or query is blank
     */
    public List<TextSegment> search(final String query, final int topK) {
        if (searcher == null || query == null || query.isBlank())
            return List.of();
        // ---- dynamic candidateK ----
        final var baseK = Math.max(topK * 4, 20);
        // scale candidate pool slightly for longer queries
        final var candidateK = (int) (baseK * (1.0 + Math.min(query.length() / 100.0, 1.0)));
        try {
            // ---- BM25 search ----
            final var textQuery = queryParser.parse(QueryParser.escape(query));
            final var textHits = searcher.search(textQuery, candidateK).scoreDocs;
            // ---- Vector search ----
            final var embedding = normalize(embeddingModel.embed(query).content().vector());
            final Query vectorQuery = new KnnFloatVectorQuery("embedding", embedding, candidateK);
            final var vectorHits = searcher.search(vectorQuery, candidateK).scoreDocs;
            // ---- Fusion ----
            return reciprocalRankFusion(textHits, vectorHits, topK);
        } catch (final Exception e) {
            LOG.error("Hybrid search failed (query='{}')", query, e);
            return List.of();
        }
    }

    /**
     * Combines BM25 and vector search results using Reciprocal Rank Fusion (RRF).
     *
     * <p>
     * Computes RRF score as: score(doc) = sum_i 1 / (rrfK + rank_i). Returns top-k segments including metadata:
     * filename, sourcePath, paragraph, and preview.
     * </p>
     *
     * @param bm25
     *            BM25 hits
     * @param vectorHits
     *            vector hits
     * @param topK
     *            number of top segments to return
     *
     * @return merged list of TextSegments sorted by RRF score
     *
     * @throws IOException
     *             if a Lucene document cannot be retrieved
     */
    private List<TextSegment> reciprocalRankFusion(final ScoreDoc[] bm25, final ScoreDoc[] vectorHits, final int topK)
            throws IOException {
        final var scores = new java.util.HashMap<Integer, Double>();
        for (var rank = 0; rank < bm25.length; rank++) {
            final var docId = bm25[rank].doc;
            scores.merge(docId, 1.0 / (rrfK + rank + 1), Double::sum);
        }
        for (var rank = 0; rank < vectorHits.length; rank++) {
            final var docId = vectorHits[rank].doc;
            scores.merge(docId, 1.0 / (rrfK + rank + 1), Double::sum);
        }
        final var stored = searcher.storedFields();
        return scores.entrySet().stream().sorted(Map.Entry.<Integer, Double> comparingByValue().reversed()).limit(topK)
                .map(e -> {
                    try {
                        final var luceneDoc = stored.document(e.getKey());
                        return TextSegment.from(luceneDoc.get("content"),
                                Metadata.from(Map.of("filename", luceneDoc.get("filename"), "sourcePath",
                                        luceneDoc.get("sourcePath"), "paragraph", luceneDoc.get("paragraphIndex"),
                                        "preview", luceneDoc.get("preview"))));
                    } catch (final IOException ex) {
                        LOG.error("Failed to read Lucene doc {}", e.getKey(), ex);
                        return null; // mark as null
                    }
                }).filter(seg -> seg != null).toList();
    }

    /**
     * Normalizes a float vector for cosine similarity.
     *
     * @param vector
     *            input vector
     *
     * @return normalized vector with unit length
     */
    private float[] normalize(final float[] vector) {
        var norm = 0f;
        for (final float v : vector)
            norm += v * v;
        if (norm == 0f)
            return vector;
        norm = (float) Math.sqrt(norm);
        final var normalized = new float[vector.length];
        for (var i = 0; i < vector.length; i++)
            normalized[i] = vector[i] / norm;
        return normalized;
    }
}