package sloth.checking;

import java.util.List;

public record Type(String name, List<Type> generics)
{
    public Type(String name)
    {
        this(name, List.of());
    }
}
