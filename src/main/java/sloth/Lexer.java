package sloth;

import java.util.Arrays;
import java.util.List;

public class Lexer
{
    public static List<String> lex(String text)
    {
        return Arrays.asList(text
                .replaceAll("([(.)])", " $1 ")
                .split(" +")
        );
    }
}
