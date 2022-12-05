package org.meteordev.starscript.compiler;

/** A type of a token produces by {@link Lexer}. */
public enum Token {
    String, Identifier, Number,

    Null,
    True, False,
    And, Or,

    EqualEqual, BangEqual,
    Greater, GreaterEqual,
    Less, LessEqual,

    Plus, Minus,
    Star, Slash, Percentage, UpArrow,
    Bang,

    Dot, Comma,
    QuestionMark, Colon,
    LeftParen, RightParen,
    LeftBrace, RightBrace,

    Section,

    Error, EOF
}
