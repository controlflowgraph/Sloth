package sloth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest
{

    @Test
    void shouldLexSimpleText()
    {
        List<String> expected = List.of(
                "this",
                "(",
                "is",
                "a",
                ")",
                "test",
                "."
        );
        List<String> actual = Lexer.lex("this(is a)      test.");
        assertEquals(expected, actual);
    }
}