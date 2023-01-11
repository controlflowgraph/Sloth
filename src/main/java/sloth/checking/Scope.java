package sloth.checking;

import java.util.HashMap;
import java.util.Map;

public record Scope(String name, Map<String, Type> variables)
{
    public Scope(String name)
    {
        this(name, new HashMap<>());
    }

    public boolean isDefined(String name)
    {
        return this.variables.containsKey(name);
    }

    public void define(String name, Type type)
    {
        if(isDefined(name))
            throw new RuntimeException("Variable " + name + " already defined in scope!");
        this.variables.put(name, type);
    }

    public Type get(String name)
    {
        if(!isDefined(name))
            throw new RuntimeException("Variable " + name + " not defined in scope!");
        return this.variables.get(name);
    }
}
