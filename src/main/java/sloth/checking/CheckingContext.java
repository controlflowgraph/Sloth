package sloth.checking;

import java.util.ArrayDeque;
import java.util.Deque;

public class CheckingContext
{
    private final Deque<Scope> scopes = new ArrayDeque<>();

    public CheckingContext()
    {
        this.scopes.add(new Scope("global"));
    }

    public boolean isVariableDefined(String name)
    {
        for (Scope scope : this.scopes)
        {
            if(scope.isDefined(name))
                return true;
        }
        return false;
    }

    public Type getVariableType(String name)
    {
        for (Scope scope : this.scopes)
        {
            if(scope.isDefined(name))
                return scope.get(name);
        }
        throw new RuntimeException("Variable " + name + " is not defined!");
    }

    public void definedVariable(String name, Type v)
    {
        if(isVariableDefined(name))
            throw new RuntimeException("Variable '" + name + "' is already defined!");
        this.scopes.peek().define(name, v);
    }
}