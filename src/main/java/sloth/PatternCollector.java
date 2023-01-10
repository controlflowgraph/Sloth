package sloth;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class PatternCollector
{
    private PatternCollector()
    {
    }

    public static List<Pattern> collect(Class<?> cls)
    {
        List<Pattern> patterns = new ArrayList<>();

        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(PatternMarker.class))
            {
                if (!Modifier.isStatic(method.getModifiers()))
                    throw new RuntimeException("Method must be static (" + method + ")!");
                if (!method.getReturnType().equals(Pattern.class))
                    throw new RuntimeException("Method must return Pattern! (" + method + ")");
                if (method.getParameterCount() != 0)
                    throw new RuntimeException("Method must have no parameters! (" + method + ")");
                try
                {
                    patterns.add((Pattern) method.invoke(null));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        return patterns;
    }
}
