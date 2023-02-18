package sloth.typing;

import java.util.List;

public record Signature(String name, List<String> generics, List<Type> parameters, Type result)
{
}
