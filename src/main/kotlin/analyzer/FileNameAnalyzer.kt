package analyzer

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenFilter
import org.apache.lucene.analysis.core.LowerCaseFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

class FileNameAnalyzer : Analyzer() {

    override fun createComponents(fieldName: String?): TokenStreamComponents {
        val tokenizer = StandardTokenizer()
        
        val filter: TokenFilter = LowerCaseFilter(tokenizer) 
        val filter1 = ASCIIFoldingFilter(filter)
        return TokenStreamComponents(tokenizer, filter1)
    }
}
