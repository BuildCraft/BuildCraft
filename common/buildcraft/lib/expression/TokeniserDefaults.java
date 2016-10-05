package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

import buildcraft.lib.expression.Tokeniser.*;

public class TokeniserDefaults {
    // Lots of gobblers.
    // They like to "gobble" up bits of text.
    public static final ITokenizerGobbler GOBBLER_SINGLE_QUOTE = (ctx) -> {
        // single quote gobbler, takes a length of text in quotes. Does NOT allow EOL.
        String text = ctx.get(4);// Maximum length for single quotes - '\\'
        if (!text.startsWith("'")) return ResultSpecific.IGNORE;
        if (text.charAt(1) == '\\') {
            if (text.charAt(3) == '\'') {
                return new ResultConsume(4);
            } else {
                return new ResultInvalid(4);
            }
        } else if (text.charAt(2) == '\'') {
            return new ResultConsume(3);
        } else {
            return new ResultInvalid(3);
        }
    };
    public static final ITokenizerGobbler GOBBLER_DOUBLE_QUOTE = (ctx) -> {
        // Quoted gobbler, takes a length of text in quotes. Does NOT allow EOL.
        String text = ctx.get(3);// Min valid length
        int i = 3;
        int s = 1;
        if (!text.startsWith("\"")) return ResultSpecific.IGNORE;
        while (true) {
            if (s == i) {
                text += ctx.get(i, i + 4);
                i += 4;

            }
            char c = text.charAt(s);
            if (c == '\\') {
                s++;
            } else if (c == '"') {
                return new ResultConsume(s + 1);
            } else if (c == Tokeniser.END_OF_LINE) {
                return new ResultInvalid(s + 1);
            }
            s++;
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
            String str = ctx.get(i, i + 1);
            char c = str.charAt(0);
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
            String c = ctx.get(i, i + 1);
            if (!Character.isAlphabetic(c.charAt(0))) break;
            i++;
        }
        return i == 0 ? ResultSpecific.IGNORE : new ResultConsume(i);
    };
    public static final ITokenizerGobbler GOBBLER_NON_WHITESPACE = (ctx) -> {
        // Non-special char gobbler, takes a single of not-EOL and not-EOF and not-whitespace
        String s = ctx.get(1);
        char c = s.charAt(0);
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
        list.add(GOBBLER_SINGLE_QUOTE);
        list.add(GOBBLER_DOUBLE_QUOTE);
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
