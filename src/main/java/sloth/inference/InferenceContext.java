package sloth.inference;

import java.util.*;

public record InferenceContext(Map<Integer, Fragment> fragments, Map<Integer, Type> types, Deque<VariableScope> scopes, IdProvider provider, ChangeFlag flag, InheritanceTree tree)
{
    public InferenceContext(InheritanceTree tree)
    {
        this(new HashMap<>(), new HashMap<>(), new ArrayDeque<>(), new IdProvider(), new ChangeFlag(), tree);
        this.scopes.push(new VariableScope());
    }

    public boolean isKnown(int id)
    {
        return this.types.containsKey(id);
    }

    public boolean areKnown(List<Integer> ids)
    {
        for (Integer id : ids)
        {
            if(!isKnown(id))
                return false;
        }
        return true;
    }

    public void register(Fragment fragment)
    {
        this.fragments.put(fragment.output(), fragment);
    }

    public int getId()
    {
        return this.provider.next();
    }

    public boolean isDefinedLocally(String name)
    {
        return this.scopes.peek().isDefined(name);
    }

    public int defineLocally(String name)
    {
        int id = getId();
        this.scopes.peek().define(name, id);
        return id;
    }

    public int getId(String name)
    {
        for (VariableScope scope : this.scopes)
        {
            if(scope.isDefined(name))
                return scope.getId(name);
        }
        throw new RuntimeException("No variable '" + name + "' defined!");
    }

    public void setExpected(int id, Type type)
    {
        if(this.types.containsKey(id))
        {
            Type sub = this.types.get(id);
            if(!sub.equals(type))
            {
                this.flag.set();
                // sub must be assignable to type
            }
        }
        else
        {
            this.flag.set();
            this.types.put(id, type);
        }
    }

    public void setActual(int id, Type type)
    {
        if(this.types.containsKey(id))
        {
            Type sup = this.types.get(id);
            if(!sup.equals(type))
            {
                this.flag.set();
                // type must be assignable to sup
            }
        }
        else
        {
            this.flag.set();
            this.types.put(id, type);
        }
    }

    public List<Type> getTypes(List<Integer> ids)
    {
        return ids.stream()
                .map(this::getType)
                .toList();
    }

    public Type getType(int source)
    {
        return this.types.get(source);
    }

    public Set<Type> getSuperType(Type over, Type type)
    {
        return this.tree.getSuperType(over, type);
    }
    public Set<Type> getSuperType(Set<Type> over, Set<Type> type)
    {
        return this.tree.getSuperType(over, type);
    }

    public void push()
    {
        this.scopes.push(new VariableScope());
    }

    public void pop()
    {
        this.scopes.pop();
    }

    public Type createAlias(Set<Type> over)
    {
        if(over.size() == 1)
            return over.stream().toList().get(0);
        String name = "$" + this.tree.nodes().size();
        this.tree.add(new Descriptor(name, List.of(), over.stream().toList()));
        return new Type(false, name, List.of());
    }
}
