package sloth.inference;

public class IdProvider
{
    private int id;

    public int next()
    {
        return this.id++;
    }
}
