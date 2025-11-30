package com.deerbank.controller;

import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;
import com.deerbank.service.PayeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
                    .ok(resultMap(true, "Sucessfully payees list",allPayees)
                    );
        }catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(resultMap(false, e.getMessage(),"")
                    );
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayee(@Valid @RequestBody PayeeRequest payeeRequest) {

        PayeeResponse createdPayee = payeeService.createPayee(payeeRequest);
        return ResponseEntity
                .status((HttpStatus.OK))
                .body(resultMap(true,"Created Payee Successfully", createdPayee)
        );
    }

    @DeleteMapping()
    public ResponseEntity<?> deletePayee(@RequestParam Integer id) {

        payeeService.deletePayee(id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resultMap(true,"Payee deleted successfully", ""));
    }

    @PutMapping()
    public ResponseEntity<?> updatePayee(@RequestBody PayeeRequest payeeRequest,@RequestParam Integer id) {
        PayeeResponse updatedPayee=payeeService.updatePayee(payeeRequest, id);

        return ResponseEntity
                .status((HttpStatus.CREATED))
                .body(resultMap(true,"Updated data Successfully", updatedPayee)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayee(@PathVariable  Integer id) {
        PayeeResponse payeeResponse=payeeService.getPayee(id);

        return ResponseEntity
                .status((HttpStatus.OK))
                .body(resultMap(true,"Get Payee "+id+" Successfully", payeeResponse)
                );
    }

    public <T> Map<String, Object> resultMap(boolean status, String message,T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", status);
        result.put("message", message);
        result.put("data", data);

        return result;
    }

}
