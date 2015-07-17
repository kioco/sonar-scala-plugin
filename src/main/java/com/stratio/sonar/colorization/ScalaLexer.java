package com.stratio.sonar.colorization;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.impl.Lexer;
import com.sonar.sslr.impl.channel.BlackHoleChannel;
import com.sonar.sslr.impl.channel.IdentifierAndKeywordChannel;
import com.sonar.sslr.impl.channel.PunctuatorChannel;
import com.sonar.sslr.impl.channel.UnknownCharacterChannel;

import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.commentRegexp;
import static com.sonar.sslr.impl.channel.RegexpChannelBuilder.regexp;

public class ScalaLexer {

	public static final String INT = "[0-9]++";
	public static final String FLOAT = "[0-9]++\\.[0-9]++";
	
	public static final String NUMERIC_LITERAL = "(?:" + INT + "|" + FLOAT + ")";
	
	public static final String LITERAL = "(?:"
			+ "\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\""
			+ "|'([^'\\\\]*+(\\\\[\\s\\S])?+)*+'"
			+ ")";
	
	public static final String ANNOTATION = "@[a-zA-Z]++";
	
	public static final String SINGLE_LINE_COMMENT = "//[^\\n\\r]*+|<!--[^\\n\\r]*+";
	public static final String MULTI_LINE_COMMENT = "/\\*[\\s\\S]*?\\*/";
	public static final String MULTI_LINE_COMMENT_NO_LB = "/\\*[^\\n\\r]*?\\*/";
	
	public static final String COMMENT = "(?:" + SINGLE_LINE_COMMENT + "|" + MULTI_LINE_COMMENT + ")";
	
	private static final String HEX_DIGIT = "[0-9a-fA-F]";
	private static final String UNICODE_ESCAPE_SEQUENCE = "u" + HEX_DIGIT + HEX_DIGIT + HEX_DIGIT + HEX_DIGIT;
	private static final String UNICODE_LETTER = "\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Nl}";
	private static final String UNICODE_COMBINING_MARK = "\\p{Mn}\\p{Mc}";
	private static final String UNICODE_DIGIT = "\\p{Nd}";
	private static final String UNICODE_CONNECTOR_PUNCTUATION = "\\p{Pc}";
	private static final String IDENTIFIER_START = "(?:[$_" + UNICODE_LETTER + "]|\\\\" + UNICODE_ESCAPE_SEQUENCE + ")";
	private static final String IDENTIFIER_PART = "(?:" + IDENTIFIER_START + "|[" + UNICODE_COMBINING_MARK + UNICODE_DIGIT + UNICODE_CONNECTOR_PUNCTUATION + "])";
	public static final String IDENTIFIER = IDENTIFIER_START + IDENTIFIER_PART + "*+";
	
	/**
	* LF, CR, LS, PS
	*/
	public static final String LINE_TERMINATOR = "\\n\\r\\u2028\\u2029";
	/**
	* Tab, Vertical Tab, Form Feed, Space, No-break space, Byte Order Mark, Any other Unicode "space separator"
	*/
	public static final String WHITESPACE = "\\t\\u000B\\f\\u0020\\u00A0\\uFEFF\\p{Zs}";
	
	private ScalaLexer() {
	}
	
	public static Lexer create(ScalaConfiguration conf) {
		return Lexer.builder()
				.withCharset(conf.getCharset())
				.withFailIfNoChannelToConsumeOneCharacter(true)
				// Channels, which consumes more frequently should come first.
				// Whitespace character occurs more frequently than any other, and thus come first:
				.withChannel(new BlackHoleChannel("[" + LINE_TERMINATOR + WHITESPACE + "]++"))
				// Comments
				.withChannel(commentRegexp(COMMENT))
				// String Literals
				.withChannel(regexp(GenericTokenType.LITERAL, LITERAL))
				.withChannel(regexp(ScalaTokenType.ANNOTATION, ANNOTATION))
				// Regular Expression Literals				
				.withChannel(regexp(ScalaTokenType.NUMERIC_LITERAL, NUMERIC_LITERAL))
				.withChannel(new IdentifierAndKeywordChannel(IDENTIFIER, true, ScalaKeyword.values()))
				.withChannel(new PunctuatorChannel(ScalaPunctuator.values()))
				.withChannel(new UnknownCharacterChannel())
				.build();
	}
	
}
