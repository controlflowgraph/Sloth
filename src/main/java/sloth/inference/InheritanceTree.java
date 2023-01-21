package sloth.inference;

import java.util.*;

public record InheritanceTree(Map<String, TypeDescription> description, Map<String, List<Type>> implementations)
{
    public InheritanceTree()
    {
        this(new HashMap<>(), new HashMap<>());
    }

    public static void mustBeType(Type expected, Type actual)
    {
        if (!expected.equals(actual))
            throw new RuntimeException("Mismatching type!");
    }

    public List<Type> getDifferentiated(Type t)
    {
        List<Type> diff = new ArrayList<>();
        diff.add(t);
        Substitution substitution = Substitution.of(
                this.description.get(t.name()).generics(),
                t.generics()
        );

        List<Type> types = this.implementations.get(t.name());
        for (Type type : types)
        {
            diff.add(type.substitute(substitution));
        }
        return diff;
    }

    public boolean isAssignable(Type form, Type value)
    {
        List<Type> differentiated = getDifferentiated(value);
        for (Type type : differentiated)
        {
            if (type.name().equals(form.name()) && type.generics().size() == form.generics().size())
            {
                List<Type> generics = type.generics();
                boolean assignable = true;
                for (int i = 0; i < generics.size(); i++)
                {
                    Type actParam = generics.get(i);
                    Type expParam = form.generics().get(i);
                    assignable &= isAssignable(expParam, actParam);
                }
                if (assignable)
                    return true;
            }
        }
        return false;
    }

    public void add(TypeDescription description)
    {
        if (this.description.containsKey(description.name()))
            throw new RuntimeException("Type '" + description.name() + "' already added!");
        this.description.put(description.name(), description);
        Set<Type> implemented = new HashSet<>();
        for (Type implementation : description.implementations())
        {
            implemented.addAll(getDifferentiated(implementation));
        }
        this.implementations.put(description.name(), new ArrayList<>(implemented));
    }

    public Type getSuperType(Type over, Type type)
    {
        if(over.equals(type))
            return over;
        if(isAssignable(over, type))
            return over;
        if(isAssignable(type, over))
            return type;

        // check if over is contained in type diff
        // check if type is contained in over diff
        // iteratively check the super types
        throw new RuntimeException("ERROR NOT IMPLEMENTED!");
    }
}
