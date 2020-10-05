package gg.valgo.gradian;

import gg.valgo.gradian.parsers.*;
import gg.valgo.gradian.parsers.binary.BinaryParser;
import gg.valgo.gradian.parsers.binary.ExactBinaryParser;
import gg.valgo.gradian.util.Coroutine;

/**
 * The main Gradian class, with static variables/methods to create and combine parsers.
 */
public class Gradian {
    /**
     * A parser which matches a digit. This parser results in a string. This parser fails if the next character in the input is not a digit.
     * This parser returns a string.
     */
    public static final Parser<String> digit = regex("^[0-9]").setParserName("digit");

    /**
     * A parser which matches 1 or more digits. This parser results in a string. This parser fails if the next character in the input is not a digit. Otherwise, it will match digits until the next character is not a digit.
     * This parser returns a string.
     */
    public static final Parser<String> digits = regex("^[0-9]+").setParserName("digits");

    /**
     * A parser which matches a letter. This parser results in a string. This parser fails if the next character in the input is not a letter.
     * This parser returns a string.
     */
    public static final Parser<String> letter = regex("^[A-Za-z]").setParserName("letter");

    /**
     * A parser which parses 1 or more letters. This parser results in a string. This parser fails if the next character in the input is not a letter. Otherwise, it will match letters until the next character is not a letter.
     * This parser returns a string.
     */
    public static final Parser<String> letters = regex("^[A-Za-z]+").setParserName("letters");

    /**
     * A parser which matches any whitespace characters, up until the next non-whitespace character. This parser fails if the next character in the input is not a whitespace character. If you wish for whitespace to be optional, use `Gradian.optionalWhitespace` instead. The parser will match whitespace characters until the next character is not a whitespace character.
     * This parser returns a string.
     */
    public static final Parser<String> whitespace = regex("^\\s+").setParserName("whitespace");

    /**
     * A parser which optionally matches any whitespace character. If there is no whitespace present, it returns an empty string. Otherwise, it will return a string with the whitespace. This parser will match all whitespace characters, up until a non-whitespace character.
     * This parser returns a string.
     */
    public static final Parser<String> optionalWhitespace = maybe(whitespace).valueIfAbsent("").mapType();

    /**
     * A parser which matches any character. It results in a character, or a string if `.asString()` is called. This parser will only fail if the end of input has been reached.
     * This parser returns a character, or a string if `.asString()` is called.
     */
    public static final AnyCharacterParser anyCharacter = new AnyCharacterParser();

    /**
     * This parser matches the end of the input. It results in a null value. This parser will fail if the end of input has not been reached.
     * This parser returns a null value.
     */
    public static final Parser<Object> endOfInput = new EndOfInputParser();

    /**
     * Returns a parser that matches a specific string. This parser will fail if it cannot match the string.
     * The parser returns a string.
     * @param string The string to match.
     * @return The string parser.
     */
    public static StringParser string(String string) {
        return new StringParser(string);
    }

    /**
     * Returns a parser that matches a single character. Use `.asString()` if you want to receive a string as the result. This parser will fail if it cannot match the specified character.
     * This parser returns a character, or a string if `.asString()` is called on it.
     * @param character The character to match.
     * @return The character parser.
     */
    public static CharacterParser character(char character) {
        return new CharacterParser(character);
    }

    /**
     * Returns a choice parser that matches one character from the specified string. This parser results in a character. This parser will fail if it cannot match one of the specified characters.
     * This parser returns a character.
     * @param chars The character string.
     * @return The anyOfString parser.
     */
    public static Parser<Character> anyOfString(String chars) {
        return choiceOfCharacters(chars.toCharArray()).setParserName("anyOfString");
    }

    /**
     * Returns a choice parser that matches one of a list of characters. This parser results in a character. This parser will fail if it cannot match one of the specified characters.
     * This parser returns a character.
     * @param chars An array of characters to choose from.
     * @return The choiceOfCharacters parser.
     */
    public static Parser<Character> choiceOfCharacters(char... chars) {
        CharacterParser[] characterParsers = new CharacterParser[chars.length];

        int index = 0;
        for (char character : chars) {
            characterParsers[index++] = character(character);
        }

        return choice(characterParsers).setParserName("choiceOfCharacters").mapType();
    }

    /**
     * A parser which matches a regular expression. This parser will fail if it cannot match the pattern.
     * This parser returns a string.
     * @param pattern The regex pattern to match.
     * @return The regex parser, with flags defaulting to 0
     */
    public static RegexParser regex(String pattern) {
        return regex(pattern, 0);
    }

    /**
     * A parser which matches a regular expression. This parser will fail if it cannot match the pattern.
     * This parser returns a string.
     * @param pattern The regex pattern to match.
     * @param flags The regex flags.
     * @return The regex parser.
     */
    public static RegexParser regex(String pattern, int flags) {
        return new RegexParser(pattern, flags);
    }

    /**
     * Returns a parser which optionally matches another parser. If a match cannot be made, the parser has a result of null. If you wish to ignore the result if it is absent, use `.ignoreIfAbsent()`. If you wish to return a specific value if the result is absent, use `.valueIfAbsent(Object value)`. This parser cannot fail.
     * This parser returns the result of its child parser if the child parser succeeded. If not, it returns null, or the result is ignored if `.ignoreIfAbsent()` is called.
     * @param parser The parser to possibly match.
     * @return The maybe parser.
     */
    public static <ResultType> MaybeParser<ResultType> maybe(Parser<ResultType> parser) {
        return new MaybeParser<>(parser);
    }

    /**
     * Returns a parser which matches another parser, but ignores the result. Useful in parsers such as sequence, in order to omit unnecessary data. This parser cannot fail.
     * This parser returns null.
     * @param parser The parser to ignore the result of.
     * @return The ignore parser.
     */
    public static <ResultType> IgnoreParser<ResultType> ignore(Parser<ResultType> parser) {
        return new IgnoreParser<>(parser);
    }

    /**
     * A parser which parses a sequence of parsers. If any parser in the sequence fails, then the sequence parser fails. By default, this parser results in an array. If you would like to receive an ArrayList back, use `.asArrayList()`. If you would like to join the resulting values, use `.join(String delimiter)`.
     * This parser returns an array or ArrayList of results from the results of the elements in its sequence.
     * @param parsers A list of parsers, which will be used in the sequence.
     * @return The sequence parser.
     */
    public static SequenceParser sequence(Parser<?>... parsers) {
        return new SequenceParser(parsers);
    }

    /**
     * A parser which parses a child parser between two other parsers. This parser will fail in the same way as a sequence parser. The result of the middle parser is returned.
     * This parser returns the result of the "middle" parser.
     * @param left The left parser, which will be parsed before the middle parser.
     * @param right The right parser, which will be parsed after the middle parser.
     * @param parser The middle parser, which is the parser whose result will be returned.
     * @return The between parser.
     */
    public static <ResultType> Parser<Object> between(Parser<?> left, Parser<?> right, Parser<ResultType> parser) {
        return sequence(left, parser, right).map(array -> array[1]).setParserName("between");
    }

    /**
     * A parser which attempts to parse each "choice" it is given. The first parser that doesn't fail is the one that is chosen, and the result from that parser is used. If every choice fails, then this parser will fail.
     * This parser returns the result of the first parser that "passes".
     * @param parsers A list of choices, which will be attempted in that order.
     * @return The choice parser.
     */
    public static Parser<Object> choice(Parser<?>... parsers) {
        return new ChoiceParser(parsers).setParserName("choice");
    }

    /**
     * A parser which "peeks" ahead in the string, without consuming any input. This parser will peek at the next character. If the end of input has been reached, this parser will result in an empty string. This parser cannot fail.
     * This parser returns a string with a single "peeked" character, or an empty string.
     */
    public static final PeekParser peek1 = peek(1);

    /**
     * A parser which "peeks" ahead in the string, without consuming any input. This parser will peek at the next n chars, with n being the input to this method. If the input has less characters left than the amount of characters, the result will be truncated. This parser cannot fail.
     * This parser returns the "peeked" characters, a string.
     * @param chars The amount of characters to peek.
     * @return The peek parser.
     */
    public static PeekParser peek(int chars) {
        return new PeekParser(chars);
    }

    /**
     * A parser which attempts to parse its child parser, without consuming input. This parser will fail if its child parser fails.
     * This parser results in the result type of its child parser.
     * @param parser The parser to look ahead with.
     * @param <ResultType> The result type of the child parser.
     * @return The lookAhead parser.
     */
    public static <ResultType> LookAheadParser<ResultType> lookAhead(Parser<ResultType> parser) {
        return new LookAheadParser<>(parser);
    }

    /**
     * A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called.  If you would like to join the resulting values, use `.join(String delimiter)`.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @return The many parser.
     */
    public static <ResultType> ManyParser<ResultType> many(Parser<ResultType> parser) {
        return (ManyParser<ResultType>) new ManyParser<>(parser, -1, -1).setParserName("many");
    }

    /**
     * A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser cannot parse enough instances, it fails.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @param minimumCount The minimum amount of repetitions to allow.
     * @return The atLeast parser.
     */
    public static <ResultType> ManyParser<ResultType> atLeast(Parser<ResultType> parser, int minimumCount) {
        return (ManyParser<ResultType>) new ManyParser<>(parser, minimumCount, -1).setParserName("atLeast");
    }

    /**
     * A parser which parses at least one instance of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser cannot parse at least one instance, it fails.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @return The atLeastOne parser.
     */
    public static <ResultType> ManyParser<ResultType> atLeastOne(Parser<ResultType> parser) {
        return (ManyParser<ResultType>) atLeast(parser, 1).setParserName("atLeastOne");
    }

    /**
     * A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser parses too many instances, it fails.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @param maximumCount The maximum amount of repetitions to allow.
     * @return The atMost parser.
     */
    public static <ResultType> ManyParser<ResultType> atMost(Parser<ResultType> parser, int maximumCount) {
        return (ManyParser<ResultType>) new ManyParser<>(parser, -1, maximumCount).setParserName("atMost");
    }

    /**
     * A parser which parses multiple instances of the parser passed into it, the amount of which will be in a range. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser parses too many of too few instances, it fails.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @param minimumCount The minimum amount of repetitions to allow.
     * @param maximumCount The maximum amount of repetitions to allow.
     * @return The manyBetween parser.
     */
    public static <ResultType> ManyParser<ResultType> manyBetween(Parser<ResultType> parser, int minimumCount, int maximumCount) {
        return (ManyParser<ResultType>) new ManyParser<>(parser, minimumCount, maximumCount).setParserName("manyBetween");
    }

    /**
     * A parser which parses a certain amount of instances of the parser passes into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser doesn't parse the right amount of instances, it fails.
     * This parser returns an array of results, an ArrayList if `.asArrayList()` is called, or a String if `.join(String delimiter)` is called.
     * @param parser The parser to repeat.
     * @param count The amount of repetitions to parse.
     * @return The exactly parser.
     */
    public static <ResultType> ManyParser<ResultType> exactly(Parser<ResultType> parser, int count) {
        return (ManyParser<ResultType>) manyBetween(parser, count, count).setParserName("exactly");
    }

    /**
     * A parser which parses values, separated by a separator. This parser will fail if the separator is not followed by a value. Empty "lists" are allowed. An array of values, separated by the separator, is returned. If you would like to receive an ArrayList back, use `.asArrayList()`. If you would like to join the resulting values, use `.join(String delimiter)`.
     * This parser returns an array or ArrayList of values, separated by the separator, or a String if `.join(String delimiter) is called`.
     * @param separator The separator between values.
     * @param values The values to parse between separators.
     * @return A separatedBy parser.
     */
    public static <ResultType> SeparatedByParser<ResultType> separatedBy(Parser<?> separator, Parser<ResultType> values) {
        return new SeparatedByParser<>(separator, values);
    }

    /**
     * A parser which matches everything up until the specified parser. If this parser reaches the end of input, it will fail. Otherwise, the result is everything matched up until the specified parser.
     * This parser returns a string.
     * @param parser The parser to match everything up until.
     * @return The everythingUntil parser.
     */
    public static EverythingUntilParser everythingUntil(Parser<?> parser) {
        return new EverythingUntilParser(parser);
    }

    /**
     * A parser which matches anything except the parser passed into it. If the child parser passes, this parser will fail. Otherwise, the result is the current character in the string.
     * This parser returns a character.
     * @param parser The parser to not match.
     * @return The anythingExcept parser.
     */
    public static AnythingExceptParser anythingExcept(Parser<?> parser) {
        return new AnythingExceptParser(parser);
    }

    /**
     * Creates a coroutine parser, allowing you to run custom logic in a parser. This is an advanced parser. It will fail if any of the parsers used inside of it fail. Otherwise, it will result in the value returned from the lambda.
     * @param executor A lambda taking in a context, with a `.yield()` method. When you want to parse a value, use `.yield(parser)` to parse that parser, and get its result back. The context also has a `.reject()` method, which will exit out of the coroutine and fail the parser.
     * @return A coroutine parser.
     */
    public static <ResultType> CoroutineParser<ResultType> coroutine(Coroutine.CoroutineExecutor<ResultType, Parser<?>, Object> executor) {
        return new CoroutineParser<>(executor);
    }

    /**
     * A parser which always fails with the specified message. Useful for failing a coroutine or other complex logic with a custom message.
     * This parser always fails, meaning it never has a result.
     * @param message The message to fail with.
     * @return The fail parser.
     */
    public static FailParser fail(String message) {
        return new FailParser(message);
    }

    /**
     * A parser which always succeeds with the specified result.
     * @param result The result to succeed with.
     * @param <ResultType> The type of the result.
     * @return The succeedWith parser.
     */
    public static <ResultType> SucceedWithParser<ResultType> succeedWith(ResultType result) {
        return new SucceedWithParser<>(result);
    }

    /**
     * Parses an unsigned 1-byte binary value. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser u8 = binary(1, false, false);

    /**
     * Parses a signed 1-byte binary value. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser s8 = binary(1, true, false);

    /**
     * Parses an unsigned 2-byte binary value, big-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser u16BE = binary(2, false, false);

    /**
     * Parses an unsigned 2-byte binary value, little-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser u16LE = binary(2, false, true);

    /**
     * Parses a signed 2-byte binary value, big-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser s16BE = binary(2, true, false);

    /**
     * Parses a signed 2-byte binary value, little-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser s16LE = binary(2, true, true);

    /**
     * Parses an unsigned 4-byte binary value, big-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser u32BE = binary(4, false, false);

    /**
     * Parses an unsigned 4-byte binary value, little-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser u32LE = binary(4, false, true);

    /**
     * Parses a signed 4-byte binary value, big-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser s32BE = binary(4, true, false);

    /**
     * Parses a signed 4-byte binary value, little-endian. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     */
    public static final BinaryParser s32LE = binary(4, true, true);

    /**
     * Parses a binary value. This parser results in a long. This parser will fail if not enough bytes are left in the input.
     * @param bytes The amount of bytes to parse.
     * @param signed Whether the result should be signed or not.
     * @param littleEndian Whether the result should be little-endian or not.
     * @return The binary parser.
     */
    public static BinaryParser binary(int bytes, boolean signed, boolean littleEndian) {
        return new BinaryParser(bytes, signed, littleEndian);
    }

    /**
     * Parses an exact unsigned 1-byte binary value. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactU8 parser.
     */
    public static ExactBinaryParser exactU8(long value) {
        return exactBinary(1, false, false, value);
    }

    /**
     * Parses an exact signed 1-byte binary value. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactS8 parser.
     */
    public static ExactBinaryParser exactS8(long value) {
        return exactBinary(1, true, false, value);
    }

    /**
     * Parses an exact unsigned 2-byte binary value, big-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactU16BE parser.
     */
    public static ExactBinaryParser exactU16BE(long value) {
        return exactBinary(2, false, false, value);
    }

    /**
     * Parses an exact unsigned 2-byte binary value, little-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactU16LE parser.
     */
    public static ExactBinaryParser exactU16LE(long value) {
        return exactBinary(2, false, true, value);
    }

    /**
     * Parses an exact signed 2-byte binary value, big-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactS16BE parser.
     */
    public static ExactBinaryParser exactS16BE(long value) {
        return exactBinary(2, true, false, value);
    }

    /**
     * Parses an exact signed 2-byte binary value, little-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactS16LE parser.
     */
    public static ExactBinaryParser exactS16LE(long value) {
        return exactBinary(2, true, true, value);
    }

    /**
     * Parses an exact unsigned 4-byte binary value, big-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactU32BE parser.
     */
    public static ExactBinaryParser exactU32BE(long value) {
        return exactBinary(4, false, false, value);
    }

    /**
     * Parses an exact unsigned 4-byte binary value, little-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactU32LE parser.
     */
    public static ExactBinaryParser exactU32LE(long value) {
        return exactBinary(4, false, true, value);
    }

    /**
     * Parses an exact signed 4-byte binary value, big-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactS32BE parser.
     */
    public static ExactBinaryParser exactS32BE(long value) {
        return exactBinary(4, true, false, value);
    }

    /**
     * Parses an exact signed 4-byte binary value, little-endian. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param value The expected value.
     * @return The exactS32LE parser.
     */
    public static ExactBinaryParser exactS32LE(long value) {
        return exactBinary(4, true, true, value);
    }

    /**
     * Parses an exact binary value. This parser results in a long. This parser will fail if it cannot parse the correct value.
     * @param bytes The amount of bytes to parse.
     * @param signed Whether the result should be signed or not.
     * @param littleEndian Whether the result should be little-endian or not.
     * @param value The expected value.
     * @return The exactU32BE parser.
     */
    public static ExactBinaryParser exactBinary(int bytes, boolean signed, boolean littleEndian, long value) {
        return new ExactBinaryParser(bytes, signed, littleEndian, value);
    }

    /**
     * Used to create recursive parsers.
     * @param producer A lambda which returns a parser.
     * @return A parser which acts exactly the same as the returned parser.
     */
    public static <ResultType> Parser<ResultType> recursive(ParserProducer<ResultType> producer) {
        return new Parser<>() {
            private Parser<ResultType> parser;

            private void produce() {
                if (parser == null) {
                    parser = producer.produce();
                }
            }

            @Override
            public ParserState<ResultType> parse(ParserState<?> state) {
                produce();
                return parser.parse(state);
            }

            @Override
            public String getParserName() {
                produce();
                return parser.getParserName();
            }

            @Override
            public String getExpectedValueName() {
                produce();
                return "[recursive parser...]";
            }
        };
    }

    public interface ParserProducer<ResultType> {
        Parser<ResultType> produce();
    }
}