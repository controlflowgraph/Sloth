package sloth.inference;

public interface Fragment
{
    int output();

    default void forward(InferenceContext context)
    {
        throw new RuntimeException("This does not exist! (" + getClass().getName() + ".java:1)");
    }

    default void backward(InferenceContext context)
    {
        throw new RuntimeException("This does not exist! (" + getClass().getName() + ".java:1)");
    }
}
