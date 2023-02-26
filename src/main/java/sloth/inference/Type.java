package sloth.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Type(boolean generic, String name, List<Type> generics)
{
    public Type substitute(Substitution sub)
    {
        if (this.generic)
        {
            return sub.getSub(this.name);
        }
        return new Type(
                false,
                this.name,
                this.generics.stream()
                        .map(t -> t.substitute(sub))
                        .toList());
    }

    public void collect(Map<String, List<Type>> map, Type other)
    {
        if(this.generic)
        {
            map.computeIfAbsent(this.name, k -> new ArrayList<>()).add(other);
        }
        else
        {
            for (int i = 0; i < this.generics.size(); i++)
            {
                this.generics.get(i).collect(map, other.generics().get(i));
            }
        }
    }

    public boolean matches(Type other)
    {
        if(this.generic)
            return true;
        if(!this.name.equals(other.name()))
            return false;
        for (int i = 0; i < this.generics.size(); i++)
        {
            if(!this.generics.get(i).matches(other.generics().get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return (this.generic ? "'" : "") + this.name + "<" + String.join(", ", this.generics.stream()
                .map(Type::toString)
                .toList())+ ">";
    }
}
