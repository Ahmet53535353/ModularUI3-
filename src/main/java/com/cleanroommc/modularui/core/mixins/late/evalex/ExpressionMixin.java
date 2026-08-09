package com.cleanroommc.modularui.core.mixins.late.evalex;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.Token;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.math.MathContext;

@Mixin(value = Expression.class, remap = false)
public abstract class ExpressionMixin {

    @WrapOperation(method = "evaluateSubtree(Lcom/ezylang/evalex/parser/ASTNode;I)Lcom/ezylang/evalex/data/EvaluationValue;",
                    at = @At(value = "INVOKE", target = "Lcom/ezylang/evalex/data/EvaluationValue;numberOfString(Ljava/lang/String;Ljava/math/MathContext;)Lcom/ezylang/evalex/data/EvaluationValue;"))
    private static EvaluationValue wrapErr(String value, MathContext mathContext, Operation<EvaluationValue> method, @Local(name = "token") Token token) throws EvaluationException {
        try {
            return method.call(value, mathContext);
        } catch (NumberFormatException _) {
            throw new EvaluationException(token, "Failed to parse BigDecimal");
        }
    }

}
