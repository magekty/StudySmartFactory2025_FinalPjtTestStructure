package com.globalmed.mes.mes_api.code.Definition.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalmed.mes.mes_api.code.Definition.domain.DefinitionEntity;
import com.globalmed.mes.mes_api.code.Definition.repository.DefinitionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DefinitionService {

    private final DefinitionRepo definitionRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 정의된 수식 계산 (BigDecimal 정밀도 유지)
     */
    public BigDecimal calculate(String definitionName, Map<String, Object> params) {
        DefinitionEntity def = definitionRepo.findByDefinitionName(definitionName)
                .orElseThrow(() -> new IllegalArgumentException("DEFINITION_NOT_FOUND: " + definitionName));

        List<String> requiredParams;
        try {
            requiredParams = objectMapper.readValue(def.getParameters(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("PARAMS_PARSE_ERROR: " + def.getParameters(), e);
        }

        // 필수 파라미터 체크
        for (String p : requiredParams) {
            if (!params.containsKey(p)) {
                throw new IllegalArgumentException("MISSING_PARAM: " + p);
            }
        }

        // 0으로 나누기 방지
        if (params.containsKey("total_qty") && new BigDecimal(params.get("total_qty").toString()).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (params.containsKey("planned_time") && new BigDecimal(params.get("planned_time").toString()).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // Expression 캐싱
        Expression expression = expressionCache.computeIfAbsent(def.getFormula(), parser::parseExpression);

        // 평가 컨텍스트 생성
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(params.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            Object val = e.getValue();
                            if (val instanceof Number) return new BigDecimal(val.toString());
                            else throw new IllegalArgumentException("PARAM_TYPE_INVALID: " + e.getKey());
                        }
                )));

        try {
            // BigDecimal 타입으로 결과 받기
            BigDecimal result = expression.getValue(context, BigDecimal.class);
            if (result == null) result = BigDecimal.ZERO;
            BigDecimal res = result.setScale(4, RoundingMode.HALF_UP);
            return res;

        } catch (ArithmeticException ae) {
            throw new IllegalStateException(
                    "FORMULA_EVAL_ERROR (Arithmetic): definition=" + definitionName
                            + ", formula=" + def.getFormula()
                            + ", params=" + params
                            + ", cause=" + ae.getMessage(), ae);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "FORMULA_EVAL_ERROR: definition=" + definitionName
                            + ", formula=" + def.getFormula()
                            + ", params=" + params
                            + ", cause=" + e.getMessage(), e);
        }
    }
}
