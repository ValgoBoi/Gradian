# Gradian
Gradian is a [Parser Combinator](https://en.wikipedia.org/wiki/Parser_combinator) library for Java. It is based on Haskell's Parsec library, and the [arcsecond](https://github.com/francisrstokes/arcsecond) node.js package.

## What are Parser Combinators?
With parser combinator libraries, parsers are built up from smaller, simpler parsers. Parsers can be combined in a number of ways to produce more complex behavior. Gradian takes full advantage of this idea, providing a variety of "parser-combinators".

## Naming
This library is named Gradian because of the naming of the node package it is based on, [arcsecond](https://github.com/francisrstokes/arcsecond). Both an arcsecond and a gradian are (somewhat obscure) units of angle.

## Reference
### Parser Methods
- [`.run(String input)`](#parserrunstring--bytes-input---parserstate)
- [`.getResult(String input)`](#parsergetresultstring--bytes-input---)
- [`.fork(String | byte[] input, ErrorTransformer errorTransformer, SuccessTransformer successTransformer)`](#parserforkstring--byte-input-errortransformer-errortransformer-successtransformer-successtransformer---parserstate)
- [`.map(ResultMapper mapper)`](#parsermapresultmapper-mapper---parser)
- [`.<NewResultType>mapType()`](#parsernewresulttypemaptype---parser)
- [`.mapState(StateMapper mapper)`](#parsermapstatestatemapper-mapper---parser)
- [`.asString()`](#parserasstring---parser)

### Parsers
- [`.digit`](#gradiandigit---string)
- [`.digits`](#gradiandigits---string)
- [`.letter`](#gradianletter---string)
- [`.letters`](#gradianletters---string)
- [`.whitespace`](#gradianwhitespace---string)
- [`.optionalWhitespace`](#gradianoptionalwhitespace---string)
- [`.anyCharacter`](#gradiananycharacter---char--string)
- [`.endOfInput`](#gradianendofinput---null)
- [`.string(String value)`](#gradianstringstring-value---string)
- [`.character(char value)`](#gradiancharacterchar-value---char--string)
- [`.anyOfString(String chars)`](#gradiananyofstringstring-chars---char)
- [`.choiceOfCharacters(char... chars)`](#gradianchoiceofcharacterschar-chars---char)
- [`.regex(String pattern, int flags = 0)`](#gradianregexstring-pattern-int-flags--0---string)
- [`.maybe(Parser value)`](#gradianmaybeparser-parser---result-type-of-parser)
- [`.ignore(Parser value)`](#gradianignoreparser-value---null)
- [`.sequence(Parser... parsers)`](#gradiansequenceparser-parsers---object--arraylistobject--string)
- [`.between(Parser left, Parser right, Parser middle)`](#gradianbetweenparser-left-parser-right-parser-middle---result-type-of-middle)
- [`.peek1`](#gradianpeek1---string)
- [`.peek(int chars)`](#gradianpeekint-chars---string)
- [`.lookAhead(Parser parser)`](#gradianlookaheadparser-parser---result-type-of-parser)
- [`.choice(Parser... parsers)`](#gradianchoiceparser-parsers---)
- [`.many(Parser parser)`](#gradianmanyparser-parser---array--arraylist-of-result-type-of-parser-or-string)
- [`.atLeast(Parser parser, int minimumCount)`](#gradianatleastparser-parser-int-minimumcount---array--arraylist-of-result-type-of-parser-or-string)
- [`.atLeastOne(Parser parser)`](#gradianatleastoneparser-parser---array--arraylist-of-result-type-of-parser-or-string)
- [`.atMost(Parser parser, int maximumCount)`](#gradianatmostparser-parser-int-maximumcount---array--arraylist-of-result-type-of-parser-or-string)
- [`.manyBetween(Parser parser, int minimumCount, int maximumCount)`](#gradianmanybetweenparser-parser-int-minimumcount-int-maximumcount---array--arraylist-of-result-type-of-parser-or-string)
- [`.exactly(Parser parser, int count)`](#gradianexactlyparser-parser-int-count---array--arraylist-of-result-type-of-parser-or-string)
- [`.separatedBy(Parser separator, Parser values)`](#gradianseparatedbyparser-separator-parser-values---array--arraylist-of-result-type-of-values-or-string)
- [`.everythingUntil(Parser parser)`](#gradianeverythinguntilparser-parser---string)
- [`.anythingExcept(Parser parser)`](#gradiananythingexceptparser-parser---char)
- [`.coroutine(CoroutineExecutor executor)`](#gradiancoroutinecoroutineexecutor-executor---)
- [`.fail(String message)`](#gradianfailstring-message---null)
- [`.succeedWith(Object result)`](#gradiansucceedwithobject-result---result)
- [`.recursive(ParserProducer producer)`](#gradianrecursiveparserproducer-producer---)

## Parser Methods
### `parser.run(String | bytes[] input)` -> `ParserState`
Runs a parser on a given string/byte array The returned value is a `ParserState` with a `.getResult()` method to get the result of parsing. If the parser fails, the value of `parserState.isException()` will be true, and the `parserState.getException()` will return the exception.
- `String | bytes[] input` -> The string to parse
```java
ParserState<String> state = Gradian.string("Hello world").run("Hello world");
System.out.println(state); 
/*
ParserState {
  isException = false
  result = Hello world
  input = Hello world
  index = 11
}
*/
```

### `parser.getResult(String | bytes[] input)` -> `???`
Runs a parser on a given string/btye array and returns the result, or throws a ParserException if the parsing fails.
- `String | bytes[] input` -> The string to parse
```java
String result = Gradian.string("Parsers").getResult("Parsers are cool!");
System.out.println(result); // "Parsers"
```

### `parser.fork(String | byte[] input, ErrorTransformer errorTransformer, SuccessTransformer successTransformer)` -> `ParserState`
Runs a parser, and transforms the output based on whether the parser succeeded or failed. If the parser succeeds, successTransformer is run, and if the parser fails, errorTransformer is run.
- `String | byte[] input` -> The input to parse
- `ErrorTransformer errorTransformer` -> The error transformer, which modifies the result if an error is encountered
- `SuccessTransformer successTransformer` -> The success transformer, which modifies the result if no error is encountered
```java
ParserState<String> state = Gradian.string("success").fork("success", (message, parserState) -> {
    System.out.println("Error: " + message);
    return parserState;
}, (result, parserState) -> {
    System.out.println("Success: " + result);
    return parserState;
}); // "Success: success"

ParserState<String> otherState = Gradian.string("success").fork("invalid string", (message, parserState) -> {
    System.out.println("Error: " + message);
    return parserState;
}, (result, parserState) -> {
    System.out.println("Success: " + result);
    return parserState;
}); // "Error: Exception in string parser (position 0): Expected string "success" but got string "invalid" instead."
```

### `parser.map(ResultMapper mapper)` -> `Parser`
Maps the result of a parser to a new value. Useful for processing the result in the parser itself, instead of externally.
- `ResultMapper mapper` -> A lambda which takes a value, the result of the parser, and returns a new value
```java
Parser<Integer> digitParser = Gradian.digits.map(result -> Integer.parseInt(result));
// digitParser now returns an int
System.out.println(digitParser.getResult("123")); // 123
```

### `parser.<NewResultType>mapType()` -> `Parser`
A utility method, used to cast the result of a parser to a different type. In many situations, this method will be called without the type generic, if the type can be inferred.
- `NewResultType` -> The type to cast the result to
```java
Parser<Long> longParser = Gradian.digits.map(result -> Integer.parseInt(result)).mapType();
// longParser gets an int and maps it to a long
System.out.println(longParser.getResult("123456789")); // 123456789
```

### `parser.mapState(StateMapper mapper)` -> `Parser`
Maps a resulting parser state to a new parser state. Can be useful for more advanced parsers, where the parser index, or the exception needs to be modified.
- `StateMapper mapper` -> A lambda which takes a parser state, the resulting state of the parser, and returns a new parser state
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `parser.asString()` -> `Parser`
Maps a parser to result in a string.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

## Parsers
### `Gradian.digit` -> `String`
A parser which matches a digit. This parser results in a string. This parser fails if the next character in the input is not a digit.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.digits` -> `String`
A parser which matches 1 or more digits. This parser results in a string. This parser fails if the next character in the input is not a digit. Otherwise, it will match digits until the next character is not a digit.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.letter` -> `String`
A parser which matches a letter. This parser results in a string. This parser fails if the next character in the input is not a letter.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.letters` -> `String`
A parser which parses 1 or more letters. This parser results in a string. This parser fails if the next character in the input is not a letter. Otherwise, it will match letters until the next character is not a letter.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.whitespace` -> `String`
A parser which matches any whitespace characters, up until the next non-whitespace character. This parser fails if the next character in the input is not a whitespace character. If you wish for whitespace to be optional, use [`Gradian.optionalWhitespace`](#gradianoptionalwhitespace---string) instead. The parser will match whitespace characters until the next character is not a whitespace character.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.optionalWhitespace` -> `String`
A parser which optionally matches any whitespace character. If there is no whitespace present, it returns an empty string. Otherwise, it will return a string with the whitespace. This parser will match all whitespace characters, up until a non-whitespace character.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.anyCharacter` -> `char | String`
A parser which matches any character. It results in a character, or a string if `.asString()` is called. This parser will only fail if the end of input has been reached.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.endOfInput` -> `null`
This parser matches the end of the input. It results in a null value. This parser will fail if the end of input has not been reached.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.string(String value)` -> `String`
Returns a parser that matches a specific string. This parser will fail if it cannot match the string.
- `String value` -> The string to match
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.character(char value)` -> `char | String`
Returns a parser that matches a single character. Use `.asString()` if you want to receive a string as the result. This parser will fail if it cannot match the specified character.
- `char value` -> The character to match
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.anyOfString(String chars)` -> `char`
Returns a choice parser that matches one character from the specified string. This parser results in a character. This parser will fail if it cannot match one of the specified characters.
- `String chars` -> The character string
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.choiceOfCharacters(char... chars)` -> `char`
Returns a choice parser that matches one of a list of characters. This parser results in a character. This parser will fail if it cannot match one of the specified characters.
- `char... chars` -> An array of characters to choose from
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.regex(String pattern, int flags = 0)` -> `String`
A parser which matches a regular expression. This parser will fail if it cannot match the pattern.
- `String pattern` -> The regex pattern to match
- `int flags = 0` -> The regex flags, defaulting to 0
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.maybe(Parser parser)` -> Result type of `parser`
Returns a parser which optionally matches another parser. If a match cannot be made, the parser has a result of null. If you wish to ignore the result if it is absent, use `.ignoreIfAbsent()`. If you wish to return a specific value if the result is absent, use `.valueIfAbsent(Object value)`. This parser cannot fail.
- `Parser value` -> The parser to possibly match
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.ignore(Parser value)` -> `null`
Returns a parser which matches another parser, but ignores the result. Useful in parsers such as sequence, in order to omit unnecessary data. This parser cannot fail.
- `Parser value` -> The parser to ignore the result of
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.sequence(Parser... parsers)` -> `Object[] | ArrayList<Object> | String`
A parser which parses a sequence of parsers. If any parser in the sequence fails, then the sequence parser fails. By default, this parser results in an array. If you would like to receive an ArrayList back, use `.asArrayList()`. If you would like to join the resulting values, use `.join(String delimiter)`.
- `Parser... parsers` -> A list of parsers, which will be used in the sequence
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.between(Parser left, Parser right, Parser middle)` -> Result type of `middle`
A parser which parses a child parser between two other parsers. This parser will fail in the same way as a sequence parser. The result of the middle parser is returned.
- `Parser left` -> The left parser, which will be parsed before the middle parser
- `Parser right` -> The right parser, which will be parsed after the middle parser
- `Parser middle` -> The middle parser, which is the parser whose result will be returned
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.choice(Parser... parsers)` -> `???`
A parser which attempts to parse each "choice" it is given. The first parser that doesn't fail is the one that is chosen, and the result from that parser is used. If every choice fails, then this parser will fail.
- `Parser... parsers` -> A list of choices, which will be attempted in that order
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.peek1` -> `String`
A parser which "peeks" ahead in the string, without consuming any input. This parser will peek at the next character. If the end of input has been reached, this parser will result in an empty string. This parser cannot fail.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.peek(int chars)` -> `String`
A parser which "peeks" ahead in the string, without consuming any input. This parser will peek at the next n chars, with n being the input to this method. If the input has less characters left than the amount of characters, the result will be truncated. This parser cannot fail.
- `int chars` -> The amount of characters to peek.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.lookAhead(Parser parser)` -> Result type of `parser`
A parser which attempts to parse its child parser, without consuming input. This parser will fail if its child parser fails.
- `Parser parser` -> The parser to look ahead with
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.many(Parser parser)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`.
- `Parser parser` -> The parser to repeat
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.atLeast(Parser parser, int minimumCount)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser cannot parse enough instances, it fails.
- `Parser parser` -> The parser to repeat
- `int minimumCount` -> The minimum amount of repetitions to allow
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.atLeastOne(Parser parser)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses at least one instance of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser cannot parse at least one instance, it fails.
- `Parser parser` -> The parser to repeat
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.atMost(Parser parser, int maximumCount)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses multiple instances of the parser passed into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser parses too many instances, it fails.
- `Parser parser` -> The parser to repeat
- `int maximumCount` -> The maximum amount of repetitions to allow
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.manyBetween(Parser parser, int minimumCount, int maximumCount)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses multiple instances of the parser passed into it, the amount of which will be in a range. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser parses too many of too few instances, it fails.
- `Parser parser` -> The parser to repeat
- `int minimumCount` -> The minimum amount of repetitions to allow
- `int maximumCount` -> The maximum amount of repetitions to allow
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.exactly(Parser parser, int count)` -> `Array | ArrayList` of result type of `parser`, or `String`
A parser which parses a certain amount of instances of the parser passes into it. This parser repeats until it is not able to parse any more instances of the child parser. Results are returned in an array, or an ArrayList if `.asArrayList()` is called. If you would like to join the resulting values, use `.join(String delimiter)`. If the parser doesn't parse the right amount of instances, it fails.
- `Parser parser` -> The parser to repeat
- `int count` -> The amount of repetitions to parse
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.separatedBy(Parser separator, Parser values)` -> `Array | ArrayList` of result type of `values`, or `String`
A parser which parses values, separated by a separator. This parser will fail if the separator is not followed by a value. Empty "lists" are allowed. An array of values, separated by the separator, is returned. If you would like to receive an ArrayList back, use `.asArrayList()`. If you would like to join the resulting values, use `.join(String delimiter)`.
- `Parser separator` -> The separator between values
- `Parser values` -> The values to parse between separators.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.everythingUntil(Parser parser)` -> `String`
A parser which matches everything up until the specified parser. If this parser reaches the end of input, it will fail. Otherwise, the result is everything matched up until the specified parser.
- `Parser parser` -> The parser to match everything up until
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.anythingExcept(Parser parser)` -> `char`
A parser which matches anything except the parser passed into it. If the child parser passes, this parser will fail. Otherwise, the result is the current character in the string.
- `Parser parser` -> The parser to not match
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.coroutine(CoroutineExecutor executor)` -> `???`
Creates a coroutine parser, allowing you to run custom logic in a parser. This is an advanced parser. It will fail if any of the parsers used inside of it fail. Otherwise, it will result in the value returned from the lambda.
- `CoroutineExecutor executor` -> A lambda taking in a context, with a `.yield()` method. When you want to parse a value, use `.yield(parser)` to parse that parser, and get its result back. The context also has a `.reject()` method, which will exit out of the coroutine and fail the parser.
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.fail(String message)` -> `null`
A parser which always fails with the specified message. Useful for failing a coroutine or other complex logic with a custom message.
- `String message` -> The message to fail with
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.succeedWith(Object result)` -> `result`
A parser which always succeeds with the specified result.
- `Object result` -> The result to succeed with
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>

### `Gradian.recursive(ParserProducer producer)` -> `???`
Used to create recursive parsers.
- `ParserProducer producer` -> A lambda which returns a parser
<details>
    <summary>Examples</summary>

    *No examples yet...*
</details>