package org.meteordev.starscript.utils;

import org.meteordev.starscript.compiler.Expr;
import org.meteordev.starscript.compiler.Lexer;
import org.meteordev.starscript.compiler.Parser;
import org.meteordev.starscript.compiler.Token;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** Provides a list of {@link SemanticToken}s for a given Starscript source input. The main use case for semantic tokens is syntax highlighting. */
public class SemanticTokenProvider {
    /** See {@link SemanticTokenProvider}. The tokens are added to the list (which is automatically cleared) passed to this function. */
    public static void get(String source, List<SemanticToken> tokens) {
        tokens.clear();

        // Lexer
        Lexer lexer = new Lexer(source);

        lexer.next();
        while (lexer.token != Token.EOF) {
            switch (lexer.token) {
                case Dot:
                    tokens.add(new SemanticToken(SemanticTokenType.Dot, lexer.start, lexer.current));
                    break;

                case Comma:
                    tokens.add(new SemanticToken(SemanticTokenType.Comma, lexer.start, lexer.current));
                    break;

                case EqualEqual:
                case BangEqual:
                case Greater:
                case GreaterEqual:
                case Less:
                case LessEqual:
                case Plus:
                case Minus:
                case Star:
                case Slash:
                case Percentage:
                case UpArrow:
                case Bang:
                case QuestionMark:
                case Colon:
                case Ampersand:
                case VBar:
                case DoubleUpArrow:
                case DoubleLess:
                case DoubleGreater:
                case TripleGreater:
                case Tilde:
                    tokens.add(new SemanticToken(SemanticTokenType.Operator, lexer.start, lexer.current));
                    break;

                case String:
                    if (lexer.isInExpression()) tokens.add(new SemanticToken(SemanticTokenType.String, lexer.start, lexer.current));
                    break;

                case Number:
                    tokens.add(new SemanticToken(SemanticTokenType.Number, lexer.start, lexer.current));
                    break;

                case Null:
                case True:
                case False:
                case And:
                case Or:
                    tokens.add(new SemanticToken(SemanticTokenType.Keyword, lexer.start, lexer.current));
                    break;

                case LeftParen:
                case RightParen:
                    tokens.add(new SemanticToken(SemanticTokenType.Paren, lexer.start, lexer.current));
                    break;

                case LeftBrace:
                case RightBrace:
                    tokens.add(new SemanticToken(SemanticTokenType.Brace, lexer.start, lexer.current));
                    break;

                case Section:
                    tokens.add(new SemanticToken(SemanticTokenType.Section, lexer.start, lexer.current));
                    break;
            }

            lexer.next();
        }

        // Parser
        Parser.Result result = Parser.parse(source);

        if (result.hasErrors()) {
            Error error = result.errors.get(0);

            // Remove tokens at the same position or after the error
            // noinspection Java8CollectionRemoveIf
            for (Iterator<SemanticToken> it = tokens.iterator(); it.hasNext();) {
                SemanticToken token = it.next();

                if (token.end > error.character) it.remove();
            }

            // Add the error token starting at the error position going to the end of the source
            tokens.add(new SemanticToken(SemanticTokenType.Error, error.character, source.length()));
        }
        else {
            result.accept(new Visitor(tokens));
        }

        // Sort tokens
        tokens.sort(Comparator.comparingInt(token -> token.start));
    }

    private static class Visitor extends AbstractExprVisitor {
        private final List<SemanticToken> tokens;

        public Visitor(List<SemanticToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public void visitVariable(Expr.Variable expr) {
            if (!(expr.parent instanceof Expr.Get)) {
                tokens.add(new SemanticToken(SemanticTokenType.Identifier, expr.end - expr.name.length(), expr.end));
            }

            super.visitVariable(expr);
        }

        @Override
        public void visitGet(Expr.Get expr) {
            if (expr.getObject() instanceof Expr.Variable) {
                Expr.Variable varExpr = (Expr.Variable) expr.getObject();
                tokens.add(new SemanticToken(SemanticTokenType.Map, varExpr.start, varExpr.end));
            }
            else if (expr.getObject() instanceof Expr.Get) {
                Expr.Get getExpr = (Expr.Get) expr.getObject();
                tokens.add(new SemanticToken(SemanticTokenType.Map, getExpr.end - getExpr.name.length(), getExpr.end));
            }

            if (!(expr.parent instanceof Expr.Get)) {
                tokens.add(new SemanticToken(SemanticTokenType.Identifier, expr.end - expr.name.length(), expr.end));
            }

            super.visitGet(expr);
        }
    }
}
