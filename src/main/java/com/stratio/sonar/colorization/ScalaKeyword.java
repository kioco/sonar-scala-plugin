package com.stratio.sonar.colorization;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;
import org.sonar.sslr.grammar.GrammarRuleKey;

public enum ScalaKeyword implements TokenType, GrammarRuleKey {

	// Keywords
	ABSTRACT("abstract"),
	ASSERT("assert"),
	CASE("case"),
	CATCH("catch"),
	CLASS("class"),
	DEF("def"),
	DO("do"),
	ELSE("else"),
	EXTENDS("extends"),
	FALSE("false"),
	FINAL("final"),
	FINALLY("finally"),
	FOR("for"),
	FORSOME("forSome"),
	IF("if"),
	IMPLICIT("implicit"),
	IMPORT("import"),
	LAZY("lazy"),
	MATCH("match"),
	NEW("new"),
	NULL("null"),
	OBJECT("object"),
	OVERRIDE("override"),
	PACKAGE("package"),
	PRIVATE("private"),
	PROTECTED("protected"),
	REQUIRES("requires"),
	RETURN("return"),
	SEALED("sealed"),
	SUPER("super"),
	THIS("this"),
	THROW("throw"),
	TRAIT("trait"),
	TRUE("true"),
	TRY("try"),
	TYPE("type"),
	VAL("val"),
	VAR("var"),
	WHILE("while"),
	WITH("with"),
	YIELD("yield"),
	UNDERSCORE("_"),
	COLON(":"),
	EQUAL("="),
	RIGHT_ARROW("=>"),
	LEFT_ARROW("<-"),
	SYMBOL1("<:"),
	SYMBOL2("<%"),
	SYMBOL3(">:"),
	HASH("#");
	
	private final String value;
	
	private ScalaKeyword(String value) {
		this.value = value;
	}
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean hasToBeSkippedFromAst(AstNode node) {
		return false;
	}
	
	public static String[] keywordValues() {
		ScalaKeyword[] keywordsEnum = ScalaKeyword.values();
		String[] keywords = new String[keywordsEnum.length];
		for (int i = 0; i < keywords.length; i++) {
			keywords[i] = keywordsEnum[i].getValue();
		}
		return keywords;
	}
	
}
