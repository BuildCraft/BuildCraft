package buildcraft.lib.expression;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import buildcraft.lib.expression.Tokenizer.Token;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;

public final class FunctionSignature {
    private static final Tokenizer FUNCTION_TOKENIZER = new Tokenizer(
        TokenizerDefaults.GOBBLER_WORD, TokenizerDefaults.GOBBLER_NON_WHITESPACE, TokenizerDefaults.GOBBLER_DISCARD
    );

    public final String name;
    public final Argument[] args;
    public final @Nullable String func;

    public FunctionSignature(String name, Argument[] args, String func) {
        this.name = name;
        this.args = args;
        this.func = func;
    }

    public static FunctionSignature parse(String desc) throws InvalidExpressionException {
        try {
            return parse0(desc);
        } catch (InvalidExpressionException iee) {
            throw new InvalidExpressionException("Invalid function signature '" + desc + "'", iee);
        } catch (IllegalStateException ise) {
            throw new IllegalStateException("Very badly broken function signature '" + desc + "'", ise);
        }
    }

    private static FunctionSignature parse0(String desc) throws InvalidExpressionException {
        Tokenizer t = FUNCTION_TOKENIZER;

        String name = null;
        List<Argument> args = new ArrayList<>();

        /*
         * States:
         * 
         * 0: Need "("
         * 1: Need Arg Type of ")"
         * 2: Need Arg Type
         * 3: Need Arg Name
         * 4: Need either "," or ")"
         * 5: Need EOF or "{"
         * 6: Finished, but need to grab function and "}"
         */
        int state = 0;
        Class<?> argType = null;

        for (Token token : t.tokenizeWithInfo(desc)) {
            if (name == null) {
                if (token.gobbler == TokenizerDefaults.GOBBLER_WORD) {
                    name = token.text;
                } else {
                    throw new InvalidExpressionException("Expected to find a name, but actually found " + token.text);
                }
                continue;
            }

            switch (state) {
                case 0: {
                    if ("(".equals(token.text)) {
                        state = 1;
                        continue;
                    }
                    throw new InvalidExpressionException("Expected '(', but found " + token.text);
                }
                case 1: {
                    if (")".equals(token.text)) {
                        state = 5;
                        continue;
                    } else {
                        // Continue into "case 2"
                        state = 2;
                    }
                }
                //$FALL-THROUGH$
                case 2: {
                    argType = NodeTypes.parseType(token.text);
                    state = 3;
                    continue;
                }
                case 3: {
                    if (token.gobbler == TokenizerDefaults.GOBBLER_WORD) {
                        args.add(new Argument(token.text, argType));
                        state = 4;
                        continue;
                    } else {
                        throw new InvalidExpressionException(
                            "Expected to find a name, but actually found " + token.text
                        );
                    }
                }
                case 4: {
                    if (",".equals(token.text)) {
                        state = 2;
                        continue;
                    } else if (")".equals(token.text)) {
                        state = 5;
                        continue;
                    } else {
                        throw new InvalidExpressionException(
                            "Expected to find either ',' or ')', but found '" + token.text + "'"
                        );
                    }
                }
                case 5: {
                    if ("{".equals(token.text)) {
                        state = 6;
                        break;
                    } else {
                        throw new InvalidExpressionException(
                            "Expected to find either the end of the function signature, or a '{', but found '"
                                + token.text + "'"
                        );
                    }
                }
                default: {
                    throw new IllegalStateException("Unknown state " + state + "!");
                }
            }
        }

        if (state == 5) {
            // No function
            return new FunctionSignature(name, args.toArray(new Argument[0]), null);
        } else if (state == 6) {
            // A function
            int idxOpen = desc.indexOf("{");
            int idxClose = desc.lastIndexOf("}");
            if (!desc.trim().endsWith("}")) {
                throw new InvalidExpressionException("Expected to find '}' at the end of the function!");
            }
            return new FunctionSignature(name, args.toArray(new Argument[0]), desc.substring(idxOpen + 1, idxClose));
        } else {
            throw new InvalidExpressionException("Missing more tokens!");
        }
    }
}
