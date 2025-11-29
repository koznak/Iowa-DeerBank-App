package com.deerbank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayeeResponse {

    private Integer payeeId;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String accountNo;
    private String status;
    private Integer userId;
}
