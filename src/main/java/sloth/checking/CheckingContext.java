package sloth.checking;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class CheckingContext
{
    private final Deque<Scope> scopes;
    private final PrecedenceGraph graph;
    private CheckingContext(PrecedenceGraph graph, Deque<Scope> scopes)
    {
        this.graph = graph;
        this.scopes = scopes;
    }

    public CheckingContext(PrecedenceGraph graph)
    {
        this(graph, new ArrayDeque<>());
        this.scopes.push(new Scope("global"));
    }

    public CheckingContext clone()
    {
        return new CheckingContext(
                this.graph,
                this.scopes.stream()
                        .map(Scope::clone)
                        .collect(
                                ArrayDeque::new,
                                ArrayDeque::add,
                                (a, b) -> {throw new RuntimeException();}
                        )
        );
    }

    public int getPrecedence(String name)
    {
        return this.graph.get(name);
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

    public void push(String name)
    {
        this.scopes.push(new Scope(name));
    }

    public void pop()
    {
        this.scopes.pop();
    }
}
