package com.stockmate.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrancheForm {

    @NotNull(message = "Harga beli tranche tidak boleh kosong")
    @DecimalMin(value = "0.01", inclusive = true, message = "Harga beli harus lebih besar dari 0")
    private BigDecimal buyPrice;

    @Min(value = 1, message = "Jumlah lot minimal 1")
    private Integer targetLots;

    @DecimalMin(value = "0.01", inclusive = true, message = "Target budget harus lebih besar dari 0")
    private BigDecimal targetBudget;
}
