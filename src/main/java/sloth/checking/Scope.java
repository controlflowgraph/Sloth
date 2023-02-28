package sloth.checking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public record Scope(String name, Set<String> variables)
{
    public Scope(String name)
    {
        this(name, new HashSet<>());
    }

    public boolean isDefined(String name)
    {
        return this.variables.contains(name);
    }

    public void define(String name)
    {
        if(isDefined(name))
            throw new RuntimeException("Variable " + name + " already defined in scope!");
        this.variables.add(name);
    }

    public Scope clone()
    {
        return new Scope(
                this.name,
                new HashSet<>(this.variables)
        );
    }
}
