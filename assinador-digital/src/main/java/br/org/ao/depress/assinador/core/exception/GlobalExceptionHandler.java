package br.org.ao.depress.assinador.core.exception;

import br.org.ao.depress.assinador.core.model.dto.OperationOutcomeDTO;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AssinadorException.class)
    public ResponseEntity<OperationOutcomeDTO> handleAssinadorException(AssinadorException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(OperationOutcomeFactory.fromException(ex));
    }
}