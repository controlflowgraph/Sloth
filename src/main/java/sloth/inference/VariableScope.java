package sloth.inference;

import java.util.HashMap;
import java.util.Map;

public record VariableScope(Map<String, Integer> mapping)
{
    public VariableScope()
    {
        this(new HashMap<>());
    }
    public boolean isDefined(String name)
    {
        return this.mapping.containsKey(name);
    }

    public void define(String name, int id)
    {
        this.mapping.put(name, id);
    }

    public int getId(String name)
    {
        return this.mapping.get(name);
    }
}
