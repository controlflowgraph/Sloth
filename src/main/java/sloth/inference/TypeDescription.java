package sloth.inference;

import java.util.List;
import java.util.Map;

public record TypeDescription(String name, List<String> generics, List<Type> implementations)
{
}
