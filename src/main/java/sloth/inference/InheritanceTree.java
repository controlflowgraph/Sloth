package sloth.inference;

import java.util.*;
import java.util.stream.Collectors;

import static sloth.inference.SetUtils.*;

public record InheritanceTree(List<Node> nodes)
{
    public InheritanceTree()
    {
        this(new ArrayList<>());
    }

    public Set<Type> getSuperType(Type a, Type b)
    {
        Set<Type> intersection = intersection(
                differentiate(a),
                differentiate(b)
        );

        return eliminateRedundant(intersection);
    }

    public Set<Type> getSuperType(Set<Type> a, Set<Type> b)
    {
        Set<Type> intersection = intersection(
                a.stream()
                        .map(this::differentiate)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet()),
                b.stream()
                        .map(this::differentiate)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        );

        return eliminateRedundant(intersection);
    }

    public Set<Type> eliminateRedundant(Set<Type> elements)
    {
        Set<Type> eliminated = new HashSet<>(elements);
        for (Type type : elements)
        {
            if (elements.contains(type))
            {
                Set<Type> differentiate = differentiate(type);
                eliminated.removeIf(differentiate::contains);
                eliminated.add(type);
            }
        }
        return eliminated;
    }

    public boolean isAssignable(Type to, Type from)
    {
        return isAssignable(Set.of(to), differentiate(from));
    }

    public boolean isAssignable(Set<Type> to, Set<Type> from)
    {
        return isSubset(to, from);
    }

    public Set<Type> differentiate(Type type)
    {
        Node node = getNode(type.name());
        Substitution sub = Substitution.of(node.params(), type.generics());
        return union(
                Set.of(type),
                node.impls()
                        .stream()
                        .map(t -> t.substitute(sub))
                        .collect(Collectors.toSet()));
    }

    public void add(Descriptor descriptor)
    {
        this.nodes.add(new Node(
                descriptor.name(),
                descriptor.generics(),
                descriptor.traits()
                        .stream()
                        .map(this::differentiate)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())));
    }

    public boolean hasNode(String name)
    {
        return tryGetNode(name).isPresent();
    }

    public Node getNode(String name)
    {
        return tryGetNode(name).orElseThrow();
    }

    public Optional<Node> tryGetNode(String name)
    {
        return this.nodes.stream()
                .filter(n -> n.name().equals(name))
                .findFirst();
    }

    public List<Type> getDifferentiated(Type t)
    {
        List<Type> diff = new ArrayList<>();
        diff.add(t);
        Substitution substitution = Substitution.of(
                getNode(t.name()).params(),
                t.generics()
        );

        Set<Type> types = getNode(t.name()).impls();
        for (Type type : types)
        {
            diff.add(type.substitute(substitution));
        }
        return diff;
    }

    public static void mustBeType(Type expected, Type actual)
    {
        if (!expected.equals(actual))
            throw new RuntimeException("Mismatching type!");
    }
}
