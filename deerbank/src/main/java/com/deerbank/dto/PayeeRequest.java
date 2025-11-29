package com.deerbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayeeRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name should be 3 to 50")
    private String name;
    private String nickname;

//    @NotBlank(message = "Email is required")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    private String email;

//    @NotBlank(message = "Phone no is required")
    @Pattern(
            regexp = "^\\+?1?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}$",
            message = "Invalid phone number"
    )
    private String phone;

    @NotBlank(message = "Account No is required")
    @Size(max = 20, message = "Account No should be 20 digits")
    private String accountNo;

    @NotNull(message = "User ID is required")
    private Integer userId;
}
