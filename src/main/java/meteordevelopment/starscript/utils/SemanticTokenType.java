package meteordevelopment.starscript.utils;

/** The type of a {@link SemanticToken}. Can be used to determine the token's color for syntax highlighting. */
public enum SemanticTokenType {
    Dot,
    Comma,
    Operator,
    String,
    Number,
    Keyword,
    Paren,
    Brace,
    Identifier,
    Map,
    Section,
    Error
}
