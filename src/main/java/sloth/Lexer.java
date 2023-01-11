package sloth;

import java.util.Arrays;
import java.util.List;

import static java.util.function.Predicate.not;

public class Lexer
{
    public static List<String> lex(String text)
    {
        return Arrays.stream(text
                        .replaceAll("(\\W)", " $1 ")
                        .split("[ \t\n]+")
                )
                .filter(not(String::isBlank))
                .toList()
                ;
    }
}
