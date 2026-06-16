package br.org.ao.depress.assinador.core.exception;

import br.org.ao.depress.assinador.core.model.dto.OperationOutcomeDTO;
import br.org.ao.depress.assinador.core.model.factory.OperationOutcomeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AssinadorException.class)
    public ResponseEntity<OperationOutcomeDTO> handleAssinadorException(AssinadorException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(OperationOutcomeFactory.fromException(ex));
    }

    @ExceptionHandler({IOException.class, NoSuchAlgorithmException.class})
    public ResponseEntity<OperationOutcomeDTO> handleSystemException(Exception ex) {
        log.error("Erro interno ao processar requisição", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OperationOutcomeFactory.systemError(ex.getMessage()));
    }
}