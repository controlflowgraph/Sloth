package sloth.eval;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public record EvaluationContext(Deque<Map<String, Object>> scopes)
{
    public EvaluationContext()
    {
        this(new ArrayDeque<>());
        push();
    }

    public void push()
    {
        push(new HashMap<>());
    }

    public void push(Map<String, Object> scope)
    {
        this.scopes.push(scope);
    }

    public void pop()
    {
        this.scopes.pop();
    }

    public <T> T get(String name, Class<T> cls)
    {
        return cls.cast(get(name));
    }

    public Object get(String name)
    {
        for (Map<String, Object> scope : this.scopes)
        {
            if(scope.containsKey(name))
            {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Variable " + name + " is not defined!");
    }

    public void set(String name, Object value)
    {
        current().put(name, value);
    }

    public Map<String, Object> current()
    {
        return this.scopes.peek();
    }

    public Map<String, Object> flatten()
    {
        Map<String, Object> flattened = new HashMap<>();
        for (Map<String, Object> scope : this.scopes)
        {

            for (Map.Entry<String, Object> entry : scope.entrySet())
            {
                if(!flattened.containsKey(entry.getKey()))
                {
                    flattened.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return flattened;
    }
}
