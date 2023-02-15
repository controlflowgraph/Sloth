package sloth.typing;

import java.util.List;

public record Descriptor(String name, List<String> generics, List<Type> traits)
{
}
