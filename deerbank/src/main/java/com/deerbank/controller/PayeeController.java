package com.deerbank.controller;

import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;
import com.deerbank.service.PayeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payees")
@CrossOrigin(origins = "*")
public class PayeeController {

    @Autowired
    private PayeeService payeeService;

    @GetMapping
    public ResponseEntity<?> getAllPayees() {

        try{
            List<PayeeResponse> allPayees=payeeService.getAllPayees();
            return ResponseEntity
                    .ok(createSuccessResponse(true, "Sucessfully payees list",allPayees)
                    );
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createSuccessResponse(false, e.getMessage(),"")
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayee(@Valid @RequestBody PayeeRequest payeeRequest) {

        try {
            PayeeResponse payeeResponse = payeeService.createPayee(payeeRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse(true, "Created Payee Successfully", payeeResponse));

        }catch (Exception e) {
            return  ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping()
    public ResponseEntity<?> deletePayee(@RequestParam Integer id) {

        payeeService.deletePayee(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(createSuccessResponse(true,"Payee deleted successfully", ""));
    }

    @PutMapping()
    public ResponseEntity<?> updatePayee(@RequestBody PayeeRequest payeeRequest,@RequestParam Integer id) {
        PayeeResponse updatedPayee=payeeService.updatePayee(payeeRequest, id);

        return ResponseEntity
                .status((HttpStatus.CREATED))
                .body(createSuccessResponse(true,"Updated data Successfully", updatedPayee)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayee(@PathVariable  Integer id) {
        PayeeResponse payeeResponse=payeeService.getPayee(id);

        return ResponseEntity
                .status((HttpStatus.OK))
                .body(createSuccessResponse(true,"Get Payee "+id+" Successfully", payeeResponse)
                );
    }

    public <T> Map<String, Object> createSuccessResponse(boolean status, String message, T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", status);
        result.put("message", message);
        result.put("data", data);

        return result;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "error");
        return response;
    }

}
