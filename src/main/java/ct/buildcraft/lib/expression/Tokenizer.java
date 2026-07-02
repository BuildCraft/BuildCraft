/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import ct.buildcraft.lib.expression.api.InvalidExpressionException;

public class Tokenizer {
    public static final char END_OF_LINE = '\n';

    private final List<ITokenizerGobbler> tokenizers;

    public Tokenizer(List<ITokenizerGobbler> tokenizers) {
        this.tokenizers = new ArrayList<>(tokenizers);
    }

    public Tokenizer(ITokenizerGobbler... tokenizers) {
        this(Arrays.asList(tokenizers));
    }

    public String[] tokenize(String src) throws InvalidExpressionException {
        return tokenizeInternal(src, (str, g) -> str, new String[0]);
    }

    public Token[] tokenizeWithInfo(String src) throws InvalidExpressionException {
        return tokenizeInternal(src, Token::new, new Token[0]);
    }

    private <T> T[] tokenizeInternal(String src, BiFunction<String, ITokenizerGobbler, T> fn, T[] array)
        throws InvalidExpressionException {

        List<T> tokens = new ArrayList<>();

        int index = 0;
        while (index < src.length()) {
            final int contextStart = index;
            ITokenizingContext ctx = ITokenizingContext.createFromString(contextStart, src);
            boolean consumed = false;
            for (ITokenizerGobbler token : tokenizers) {
                TokenResult res = token.tokenizePart(ctx);
                if (res == ResultSpecific.IGNORE) continue;
                if (res == ResultSpecific.INVALID) {
                    throw new InvalidExpressionException("Invalid src \"" + ctx.get(10).replace("\n", "\\n") + "\"");
                }
                if (res instanceof ResultInvalid) {
                    throw new InvalidExpressionException(
                        "Invalid src \"" + ctx.get(((ResultInvalid) res).length).replace("\n", "\\n") + "\""
                    );
                }
                if (res instanceof ResultDiscard) {
                    int discardLength = ((ResultDiscard) res).length;
                    index += discardLength;
                    consumed = true;
                    break;
                }
                if (res instanceof ResultConsume) {
                    int consumedLength = ((ResultConsume) res).length;
                    String at = ctx.get(consumedLength);
                    tokens.add(fn.apply(at, token));
                    index += consumedLength;
                    consumed = true;
                    break;
                }
            }
            if (!consumed) {
                throw new InvalidExpressionException("Did not consume:" + ctx.get(50));
            }
        }
        return tokens.toArray(array);
    }

    @FunctionalInterface
    public interface ITokenizingContext {
        String get(int relStart, int relEnd);

        default String get(int length) {
            return get(0, length);
        }

        default char getCharAt(int rel) {
            return get(rel, rel + 1).charAt(0);
        }

        public static ITokenizingContext createFromString(String src) {
            return createFromString(0, src);
        }

        public static ITokenizingContext createFromString(int contextStart, String src) {
            return (relStart, relEnd) -> {
                int start = contextStart + relStart;
                int end = contextStart + relEnd;
                int stringEnd = src.length();
                StringBuilder gotten = new StringBuilder(src.substring(start, Math.min(end, stringEnd)));
                while (gotten.length() < end - start) {
                    gotten.append(END_OF_LINE);
                }
                return gotten.toString();
            };
        }
    }

    @FunctionalInterface
    public interface ITokenizerGobbler {
        TokenResult tokenizePart(ITokenizingContext ctx);
    }

    public interface TokenResult {}

    public enum ResultSpecific implements TokenResult {
        IGNORE,
        INVALID
    }

    public static class ResultInvalid implements TokenResult {
        public final int length;

        public ResultInvalid(int length) {
            this.length = length;
        }
    }

    /** Consumes a length of input. */
    public static class ResultConsume implements TokenResult {
        public static final ResultConsume ONE = new ResultConsume(1);
        public static final ResultConsume TWO = new ResultConsume(2);

        public final int length;

        public ResultConsume(int length) {
            this.length = length;
        }
    }

    /** Discards a given length of input. */
    public static class ResultDiscard implements TokenResult {
        public static final ResultDiscard SINGLE = new ResultDiscard(1);

        public final int length;

        public ResultDiscard(int length) {
            this.length = length;
        }
    }

    public static final class Token {
        public final String text;
        public final ITokenizerGobbler gobbler;

        public Token(String text, ITokenizerGobbler gobbler) {
            this.text = text;
            this.gobbler = gobbler;
        }
    }
}
