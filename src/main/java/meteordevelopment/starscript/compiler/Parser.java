package meteordevelopment.starscript.compiler;

import meteordevelopment.starscript.utils.Error;

import java.util.ArrayList;
import java.util.List;

/** Parser that produces AST (abstract syntax tree) from starscript code and reports errors. */
public class Parser {
    private final Lexer lexer;

    private final TokenData previous = new TokenData();
    private final TokenData current = new TokenData();

    private int expressionDepth;

    private Parser(String source) {
        lexer = new Lexer(source);
    }

    private Result parse_() {
        Result result = new Result();

        advance();

        while (!isAtEnd()) {
            try {
                result.exprs.add(statement());
            } catch (ParseException e) {
                result.errors.add(e.error);
                synchronize();
            }
        }

        return result;
    }

    /** Parses starscript code and returns {@link Result}. */
    public static Result parse(String source) {
        return new Parser(source).parse_();
    }

    // Statements

    private Expr statement() {
        if (match(Token.Section)) {
            if (previous.lexeme.isEmpty()) error("Expected section index.");

            int index = Integer.parseInt(previous.lexeme);
            if (index > 255) error("Section index cannot be larger than 255.");
            return new Expr.Section(index, expression());
        }

        return expression();
    }

    // Expressions

    private Expr expression() {
        return conditional();
    }

    private Expr conditional() {
        Expr expr = and();

        if (match(Token.QuestionMark)) {
            Expr trueExpr = statement();
            consume(Token.Colon, "Expected ':' after first part of condition.");
            Expr falseExpr = statement();
            expr = new Expr.Conditional(expr, trueExpr, falseExpr);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = or();

        while (match(Token.And)) {
            Expr right = or();
            expr = new Expr.Logical(expr, Token.And, right);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = equality();

        while (match(Token.Or)) {
            Expr right = equality();
            expr = new Expr.Logical(expr, Token.Or, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(Token.EqualEqual, Token.BangEqual)) {
            Token op = previous.token;
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(Token.Greater, Token.GreaterEqual, Token.Less, Token.LessEqual)) {
            Token op = previous.token;
            Expr right = term();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(Token.Plus, Token.Minus)) {
            Token op = previous.token;
            Expr right = factor();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(Token.Star, Token.Slash, Token.Percentage, Token.UpArrow)) {
            Token op = previous.token;
            Expr right = unary();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(Token.Bang, Token.Minus)) {
            Token op = previous.token;
            Expr right = unary();
            return new Expr.Unary(op, right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(Token.LeftParen)) {
                expr = finishCall(expr);
            }
            else if (match(Token.Dot)) {
                TokenData name = consume(Token.Identifier, "Expected field name after '.'.");
                expr = new Expr.Get(expr, name.lexeme);
            }
            else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>(2);

        if (!check(Token.RightParen)) {
            do {
                args.add(expression());
            } while (match(Token.Comma));
        }

        consume(Token.RightParen, "Expected ')' after function arguments.");
        return new Expr.Call(callee, args);
    }

    private Expr primary() {
        if (match(Token.Null)) return new Expr.Null();
        if (match(Token.String)) return new Expr.String(previous.lexeme);
        if (match(Token.True, Token.False)) return new Expr.Bool(previous.lexeme.equals("true"));
        if (match(Token.Number)) return new Expr.Number(Double.parseDouble(previous.lexeme));
        if (match(Token.Identifier)) return new Expr.Variable(previous.lexeme);

        if (match(Token.LeftParen)) {
            Expr expr = statement();
            consume(Token.RightParen, "Expected ')' after expression.");
            return new Expr.Group(expr);
        }

        if (expressionDepth == 0 && match(Token.LeftBrace)) {
            expressionDepth++;
            Expr expr = statement();
            consume(Token.RightBrace, "Expected '}' after expression.");
            expressionDepth--;
            return new Expr.Block(expr);
        }

        error("Expected expression.");
        return null;
    }

    // Helpers

    private void synchronize() {
        while (!isAtEnd()) {
            if (match(Token.LeftBrace)) expressionDepth++;
            else if (match(Token.RightBrace)) {
                expressionDepth--;
                if (expressionDepth == 0) return;
            }
            else advance();
        }
    }

    private void error(String message) {
        throw new ParseException(new Error(current.line, current.character, current.ch, message));
    }

    private TokenData consume(Token token, String message) {
        if (check(token)) return advance();
        error(message);
        return null;
    }

    private boolean match(Token... tokens) {
        for (Token token : tokens) {
            if (check(token)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(Token token) {
        if (isAtEnd()) return false;
        return current.token == token;
    }

    private TokenData advance() {
        previous.set(current);

        lexer.next();
        current.set(lexer.token, lexer.lexeme, lexer.line, lexer.character, lexer.ch);

        return previous;
    }

    private boolean isAtEnd() {
        return current.token == Token.EOF;
    }

    // Token data

    private static class TokenData {
        public Token token;
        public String lexeme;
        public int line, character;
        public char ch;

        public void set(Token token, String lexeme, int line, int character, char ch) {
            this.token = token;
            this.lexeme = lexeme;
            this.line = line;
            this.character = character;
            this.ch = ch;
        }

        public void set(TokenData data) {
            set(data.token, data.lexeme, data.line, data.character, data.ch);
        }

        @Override
        public String toString() {
            return String.format("%s '%s'", token, lexeme);
        }
    }

    // Parse Exception

    private static class ParseException extends RuntimeException {
        public final Error error;

        public ParseException(Error error) {
            this.error = error;
        }
    }

    // Result

    /** A class that holds the parsed AST (abstract syntax tree) and any errors that could have been found. */
    public static class Result {
        public final List<Expr> exprs = new ArrayList<>();
        public final List<Error> errors = new ArrayList<>();

        /** Helper method that returns true if there was 1 or more errors. */
        public boolean hasErrors() {
            return errors.size() > 0;
        }
    }
}
