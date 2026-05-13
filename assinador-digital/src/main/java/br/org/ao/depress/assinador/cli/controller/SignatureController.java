package br.org.ao.depress.assinador.cli.controller;

import br.org.ao.depress.assinador.core.model.dto.OperationOutcomeDTO;
import br.org.ao.depress.assinador.core.model.dto.SignRequestDTO;
import br.org.ao.depress.assinador.core.model.dto.SignatureDTO;
import br.org.ao.depress.assinador.core.model.dto.ValidateRequestDTO;
import br.org.ao.depress.assinador.core.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class SignatureController {

    private final SignatureService signatureService;

    @PostMapping("/sign")
    public ResponseEntity<SignatureDTO> sign(@RequestBody SignRequestDTO request) throws IOException, NoSuchAlgorithmException {
        return ResponseEntity.ok(signatureService.executarAssinatura(
                new File(request.bundlePath()),
                new File(request.provenancePath()),
                request.pin()
        ));
    }

    @PostMapping("/validate")
    public ResponseEntity<OperationOutcomeDTO> validate(@RequestBody ValidateRequestDTO request) {
        return ResponseEntity.ok(signatureService.validarAssinatura(
                new File(request.signaturePath()),
                new File(request.bundlePath())
        ));
    }
}