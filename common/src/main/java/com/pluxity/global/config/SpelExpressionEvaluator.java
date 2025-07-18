package com.pluxity.global.config;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class SpelExpressionEvaluator {
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public <T> T evaluate(
            String expression, String[] parameterNames, Object[] args, Class<T> desiredResultType) {
        String parsedExpression = expression.startsWith("#") ? expression : "#" + expression;

        EvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return expressionParser.parseExpression(parsedExpression).getValue(context, desiredResultType);
    }
}
