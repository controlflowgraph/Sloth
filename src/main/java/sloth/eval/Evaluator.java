package sloth.eval;

import sloth.match.Match;

public interface Evaluator
{
    Object evaluate(Match match, EvaluationContext context);
}
