package sloth.inference;

import java.util.List;

public record Type(boolean generic, String name, List<Type> generics)
{
    public Type(String name)
    {
        this(false, name, List.of());
    }

    public Type substitute(Substitution substitution)
    {
        if (this.generic) return substitution.subs().get(this.name);
        return new Type(false, this.name, this.generics.stream()
                .map(t -> t.substitute(substitution))
                .toList());
    }

    @Override
    public String toString()
    {
        if(this.generic) return "'" + this.name;
        if(this.generics.isEmpty())
            return this.name;
        return this.name + "<" + String.join(", ", this.generics.stream().map(Type::toString).toList()) + ">";
    }
}
