package com.stratio.sonar.colorization;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.source.Highlightable;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.impl.Lexer;

public class ScalaHighlighter {

	 private static final Logger LOGGER = LoggerFactory.getLogger(ScalaHighlighter.class);
	 
	 private Lexer lexer;
	 private Charset charset;
	 
	 public ScalaHighlighter(ScalaConfiguration conf){
		 this.lexer = ScalaLexer.create(conf);
		 this.charset = conf.getCharset();
	 }
	 
	 public void highlight(Highlightable highlightable, File file) {
		 SourceFileOffsets offsets = new SourceFileOffsets(file, charset);
		 List<Token> tokens = lexer.lex(file);
		 doHighlight(highlightable, tokens, offsets);
	 }
	 
	 public void highlight(Highlightable highlightable, String string) {
		 SourceFileOffsets offsets = new SourceFileOffsets(string);
		 List<Token> tokens = lexer.lex(string);
		 doHighlight(highlightable, tokens, offsets);
	 }
	 
	 private void doHighlight(Highlightable highlightable, List<Token> tokens, SourceFileOffsets offsets) {
		 Highlightable.HighlightingBuilder highlighting = highlightable.newHighlighting();
		 highlightStringsAndKeywords(highlighting, tokens, offsets);
		 highlightComments(highlighting, tokens, offsets);
		 highlighting.done();
	 }
	 
	 private static void highlightComments(Highlightable.HighlightingBuilder highlighting, List<Token> tokens, SourceFileOffsets offsets) {
		 String code;
		 for (Token token : tokens) {
			 if (!token.getTrivia().isEmpty()) {
				 for (Trivia trivia : token.getTrivia()) {
					 if (trivia.getToken().getValue().startsWith("/**")) {
						 code = "j";
					 } else {
						 code = "cd";
					 }
					 highlight(highlighting, offsets.startOffset(trivia.getToken()), offsets.endOffset(trivia.getToken()), code);
				 }
			 }
		 }
	 }
	 
	 private void highlightStringsAndKeywords(Highlightable.HighlightingBuilder highlighting, List<Token> tokens, SourceFileOffsets offsets) {
		 for (Token token : tokens) {
			 if (GenericTokenType.LITERAL.equals(token.getType())) {
				 highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "s");
			 }
			 if (isKeyword(token.getType())) {
				 highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "k");
			 }
			 if (ScalaTokenType.ANNOTATION.equals(token.getType())) {
				 highlight(highlighting, offsets.startOffset(token), offsets.endOffset(token), "a");
			 }
		 }
	 }
	 
	 private static void highlight(Highlightable.HighlightingBuilder highlighting, int startOffset, int endOffset, String code) {
		 if (endOffset > startOffset) {
			 highlighting.highlight(startOffset, endOffset, code);
		 }
	 }
	 
	 public boolean isKeyword(TokenType type) {
		 for (TokenType keywordType : ScalaKeyword.values()) {
			 if (keywordType.equals(type)) {
				 return true;
			 }
		 }
		 return false;
	 }
	
}
