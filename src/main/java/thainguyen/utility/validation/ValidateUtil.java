package thainguyen.utility.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import thainguyen.utility.mapping.ObjectMapperUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class ValidateUtil {

    private final ObjectMapperUtil objectMapperUtil;

    public ValidateUtil(ObjectMapperUtil objectMapperUtil) {
        this.objectMapperUtil = objectMapperUtil;
    }

    public <V, T> void validate (V object , Class<T> validatorClazz) throws MethodArgumentNotValidException {
        Map<String, Object> targetObj = objectMapperUtil.converObjectToMap(object);
        BindingResult bindingResult = new BeanPropertyBindingResult(targetObj, validatorClazz.getName());

        Set<Set<ConstraintViolation<T>>> constraintViolationsSet = new HashSet<>();
        targetObj.forEach((k, v) ->
                constraintViolationsSet.add(Validation.buildDefaultValidatorFactory()
                        .getValidator()
                        .validateValue(validatorClazz, k, v)));
        if (! constraintViolationsSet.isEmpty()) {
            constraintViolationsSet.forEach(constraintViolations -> {
                constraintViolations.forEach(constraintViolation -> {
                    bindingResult.addError(new FieldError(
                            "Map<String, Object>",
                            constraintViolation.getPropertyPath().toString(),
                            constraintViolation.getMessage()));
                });
            });
            if (bindingResult.hasErrors()) {
                throw new MethodArgumentNotValidException(null, bindingResult);
            }
        }
    }

}
