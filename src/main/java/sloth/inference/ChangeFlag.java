package sloth.inference;

public class ChangeFlag
{
    private boolean active;

    public void set()
    {
        this.active = true;
    }

    public void reset()
    {
        this.active = false;
    }

    public boolean active()
    {
        return this.active;
    }
}
