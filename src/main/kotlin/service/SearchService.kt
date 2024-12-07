package service

import analyzer.FileNameAnlayzer
import dto.Filetype
import dto.FoundDocument
import dto.SearchResult
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.*
import org.apache.lucene.search.*
import org.apache.lucene.store.ByteBuffersDirectory
import java.io.File
import kotlin.time.measureTime


class SearchService(val pathsToIndex: List<String>, val excludePaths: List<String>) {

    val byteBufferDir = ByteBuffersDirectory()
    val analyzer: Analyzer = FileNameAnlayzer()
    var reader: IndexReader? = null
    var searcher: IndexSearcher? = null

    fun indexDirectories() {

        val iwc = IndexWriterConfig(analyzer)
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

        val writer = IndexWriter(byteBufferDir, iwc)

        writer.use { localWriter ->

            val indexTime = measureTime {
                pathsToIndex.forEach { path ->
                    val directory = File(path)
                    indexFiles(directory, localWriter, excludePaths)

                }
            }

            println("indexed documents: ${localWriter.pendingNumDocs}")
            println("indexing directory$pathsToIndex took $indexTime")

        }
        reader = DirectoryReader.open(byteBufferDir)

        searcher = IndexSearcher(reader)

    }

    private fun indexFiles(directory: File, writer: IndexWriter, excludedPaths: List<String>) {

        if (directory.exists() && directory.isDirectory) {


//            if (System.currentTimeMillis() % 1000 == 0L) {
//                println("indexed documents: ${writer.pendingNumDocs}")
//                println("current folder: ${directory.absolutePath}")
//            }

            val (directories, files) = (directory.listFiles() ?: emptyArray()).partition { it.isDirectory }

            files.forEach {
                println("indexing file: ${it.name}->${it.absolutePath}")
                indexDoc(writer, it, "file")
            }
            directories.filter { !excludedPaths.contains(it.absolutePath) }.parallelStream().forEach {
                indexDoc(writer, it, "directory")
                println("indexing directory: ${it.absolutePath}")
                indexFiles(it, writer, excludedPaths)
            }

        } else {
            println("The provided path is not a directory or does not exist.")
        }
    }


    fun indexDoc(writer: IndexWriter, file: File, type: String) {
        val doc = Document()
        doc.add(TextField("name", file.name, Field.Store.YES))
        doc.add(TextField("path", file.absolutePath, Field.Store.YES))
        doc.add(TextField("type", type, Field.Store.YES))
        writer.addDocument(doc)
    }

    fun searchIndex(searchTerm: List<String>, numberOfDocs: Int = 10): SearchResult {

        val boostedQuery1: Query = buildQuery(listOf("name", "path"), searchTerm, TermQuery(Term("type", "directory")), 1.0f)
        val boostedQuery2: Query = buildQuery(listOf("name", "path"), searchTerm, TermQuery(Term("type", "file")), 1.0f)
        val booleanQuery = BooleanQuery.Builder()
            .add(boostedQuery1, BooleanClause.Occur.SHOULD) // SHOULD for optional match
            .add(boostedQuery2, BooleanClause.Occur.SHOULD) // SHOULD for optional match
            .build()

        val foundDocs = searcher?.search(booleanQuery, numberOfDocs) ?: return SearchResult(emptyList(), 0)

        val searchResult = ArrayList<FoundDocument>()
        if (searcher != null && searcher?.indexReader == null)
            return SearchResult(emptyList(), 0)

        val storedFields = searcher?.indexReader?.storedFields() ?: return SearchResult(emptyList(), 0)

        for (scoreDoc in foundDocs.scoreDocs) {
            val docId = scoreDoc.doc
            val doc: Document = storedFields.document(docId) // Retrieve stored fields
            FoundDocument(doc.get("name") ?: "", doc.get("path") ?: "", Filetype.valueOf(doc.get("type").uppercase()), scoreDoc.score).apply { searchResult.add(this) }
        }
        return SearchResult(searchResult, foundDocs.totalHits.value)
    }

    private fun buildQuery(fieldNames: List<String>, searchTerms: List<String>, filterQuery: TermQuery, boost: Float = 1.0f): Query {

        val mainQuery = BooleanQuery.Builder()
        fieldNames.forEach { fieldName ->
            searchTerms.forEach { searchTerm ->
                val fieldQuery = WildcardQuery(Term(fieldName, "*" + searchTerm.lowercase() + "*"))
                mainQuery.add(fieldQuery, BooleanClause.Occur.MUST)

            }
        }

        val build: BooleanQuery = mainQuery.build()

        val combinedQuery1 = mainQuery
            .add(build, BooleanClause.Occur.MUST) // Match the name
            .add(filterQuery, BooleanClause.Occur.FILTER) // Match the type
            .build()

        val boostedQuery1: Query = BoostQuery(combinedQuery1, boost)
        return boostedQuery1
    }


}
