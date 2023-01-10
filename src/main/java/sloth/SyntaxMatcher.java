package sloth;

import sloth.match.Match;
import sloth.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SyntaxMatcher
{
    private SyntaxMatcher() { }
    public static List<List<List<Match>>> parse(List<Pattern> patterns, Provider<String> provider)
    {
        List<List<String>> splits = split(provider);
        List<List<List<Match>>> matches = new ArrayList<>();
        for (List<String> split : splits)
        {
            matches.add(matches(patterns, new Provider<>(split)));
        }
        return matches;
    }

    private static List<List<String>> split(Provider<String> provider)
    {
        List<List<String>> lists = new ArrayList<>();
        List<String> current = new ArrayList<>();
        lists.add(current);
        while (provider.has())
        {
            String next = provider.next();
            if(next.equals("."))
            {
                current = new ArrayList<>();
                lists.add(current);
            }
            else
            {
                current.add(next);
            }
        }
        return lists;
    }

    private static List<List<Match>> matches(List<Pattern> patterns, Provider<String> provider)
    {
        List<List<Match>> iterations = List.of(
                List.of(new Match(0, 0, null, Map.of()))
        );
        boolean changed;
        do
        {
            changed = false;
            List<List<Match>> iterated = new ArrayList<>();
            for (List<Match> iteration : iterations)
            {
                Match last = iteration.get(iteration.size() - 1);
                provider.index(last.end());
                if(last.end() == provider.size())
                {
                    iterated.add(iteration);
                }
                else
                {
                    for (Pattern pattern : patterns)
                    {
                        Match start = new Match(last.end(), last.end(), null, Map.of());
                        List<Match> matches = pattern.tryMatch(patterns, provider, start);
                        for (Match match : matches)
                        {
                            List<Match> set = new ArrayList<>(iteration);
                            set.add(match);
                            iterated.add(set);
                            changed = true;
                        }
                    }
                }
            }
            iterations = iterated;
        }
        while (changed);
        return iterations;
    }
}
