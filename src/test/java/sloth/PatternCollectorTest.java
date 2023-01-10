package sloth;

import org.junit.jupiter.api.Test;
import sloth.examples.CorrectPattern;
import sloth.examples.NonStaticPattern;
import sloth.examples.WrongArgumentsPattern;
import sloth.examples.WrongReturnPattern;
import sloth.pattern.Pattern;
import sloth.pattern.PatternCollector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatternCollectorTest
{
    @Test
    void shouldCollectPatterns()
    {
        List<Pattern> expected = List.of(
                new Pattern("something")
        );
        List<Pattern> actual = PatternCollector.collect(CorrectPattern.class);
        assertEquals(expected, actual);
    }

    @Test
    void shouldFailOnFaultyPattern()
    {
        assertThrows(RuntimeException.class, () -> PatternCollector.collect(NonStaticPattern.class));
        assertThrows(RuntimeException.class, () -> PatternCollector.collect(WrongArgumentsPattern.class));
        assertThrows(RuntimeException.class, () -> PatternCollector.collect(WrongReturnPattern.class));
    }
}