package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.expression.Tokeniser.*;

public class TokeniserDefaults {
    // Lots of gobblers.
    // They like to "gobble" up bits of text.
    public static final ITokenizerGobbler GOBBLER_QUOTE = (ctx) -> {
        // Quoted gobbler, takes a length of text in quotes. Does NOT allow EOL.
        int length = 1;
        if (ctx.getCharAt(0) != '\'') return ResultSpecific.IGNORE;
        while (true) {
            char c = ctx.getCharAt(length);
            if (c == '\\') {
                length++;
            } else if (c == '\'') {
                return new ResultConsume(length + 1);
            } else if (c == Tokeniser.END_OF_LINE) {
                return new ResultInvalid(length + 1);
            }
            length++;
        }
    };

    private static final String[] MATH_OPS_2_CHAR = { "||", "&&", "<=", ">=", "==", "!=" };
    public static final ITokenizerGobbler GOBBLER_MATH_OPERATOR = (ctx) -> {
        String possible = ctx.get(2);
        for (String s : MATH_OPS_2_CHAR) {
            if (s.equals(possible)) {
                return ResultConsume.TWO;
            }
        }
        return ResultSpecific.IGNORE;
    };
    public static final ITokenizerGobbler GOBBLER_NUMBER = (ctx) -> {
        int i = 0;
        boolean dot = false;
        boolean moreAfterDot = false;
        while (true) {
            char c = ctx.getCharAt(i);
            if (c == '.') {
                if (dot) {
                    break;
                }
                dot = true;
            } else {
                if (!Character.isDigit(c)) break;
                moreAfterDot = dot;
            }
            i++;
        }
        if (i > 1 && dot && !moreAfterDot) {
            return new ResultConsume(i - 1);
        }
        return i == 0 ? ResultSpecific.IGNORE : new ResultConsume(i);
    };
    public static final ITokenizerGobbler GOBBLER_WORD = (ctx) -> {
        int i = 0;
        while (true) {
            char c = ctx.getCharAt(i);
            if (!Character.isAlphabetic(c)) break;
            i++;
        }
        return i == 0 ? ResultSpecific.IGNORE : new ResultConsume(i);
    };
    public static final ITokenizerGobbler GOBBLER_NON_WHITESPACE = (ctx) -> {
        // Non-special char gobbler, takes a single of not-EOL and not-EOF and not-whitespace
        char c = ctx.getCharAt(0);
        if (c == Tokeniser.END_OF_LINE) {
            return ResultSpecific.IGNORE;
        }
        if (Character.isWhitespace(c)) {
            return ResultSpecific.IGNORE;
        }
        return ResultConsume.ONE;
    };
    public static final ITokenizerGobbler GOBBLER_DISCARD = (ctx) -> ResultDiscard.SINGLE;

    public static List<ITokenizerGobbler> createParts() {
        List<ITokenizerGobbler> list = new ArrayList<>();
        list.add(GOBBLER_QUOTE);
        list.add(GOBBLER_MATH_OPERATOR);
        list.add(GOBBLER_NUMBER);
        list.add(GOBBLER_WORD);
        list.add(GOBBLER_NON_WHITESPACE);
        list.add(GOBBLER_DISCARD);
        return list;
    }

    public static Tokeniser createTokensizer() {
        return new Tokeniser(createParts());
    }
}
