package com.stratio.sonar.colorization;

import org.sonar.sslr.grammar.GrammarRuleKey;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.TokenType;

public enum ScalaTokenType implements TokenType, GrammarRuleKey {
	IDENTIFIER,
	NUMERIC_LITERAL,
	REGULAR_EXPRESSION_LITERAL,
	ANNOTATION,
	EMPTY;
	
	@Override
	public String getName() {
		return name();
	}
	
	@Override
	public String getValue() {
		return name();
	}
	
	@Override
	public boolean hasToBeSkippedFromAst(AstNode node) {
		return false;
	}
}
