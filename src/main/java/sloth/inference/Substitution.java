package sloth.inference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Substitution(Map<String, Type> subs)
{
    public static Substitution of(List<String> names, List<Type> types)
    {
        Map<String, Type> subs = new HashMap<>();
        for (int i = 0; i < names.size(); i++)
        {
            String name = names.get(i);
            if(subs.containsKey(name))
                throw new RuntimeException("Duplicate names of generic parameters!");
            subs.put(name, types.get(i));
        }
        return new Substitution(subs);
    }
}
