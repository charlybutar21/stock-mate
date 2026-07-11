package com.stockmate.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculatorForm {

    private String stockCode;

    @Builder.Default
    @NotNull(message = "Jumlah lot saat ini tidak boleh kosong")
    @Min(value = 0, message = "Jumlah lot saat ini minimal 0")
    private Integer currentLots = 0;

    @Builder.Default
    @NotNull(message = "Harga rata-rata saat ini tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Harga rata-rata saat ini tidak boleh negatif")
    private BigDecimal currentAvgPrice = BigDecimal.ZERO;

    @Builder.Default
    @NotNull(message = "Mode kalkulasi tidak boleh kosong")
    private String calculationMode = "LOT"; // "LOT" or "BUDGET"

    @Builder.Default
    @Valid
    private List<TrancheForm> tranches = new ArrayList<>(List.of(new TrancheForm()));

    @Builder.Default
    @NotNull(message = "Fee beli tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee beli tidak boleh negatif")
    private BigDecimal buyFeePercent = BigDecimal.valueOf(0.15); // Pre-fill 0.15%

    @Builder.Default
    @NotNull(message = "Fee jual tidak boleh kosong")
    @DecimalMin(value = "0.0", inclusive = true, message = "Fee jual tidak boleh negatif")
    private BigDecimal sellFeePercent = BigDecimal.valueOf(0.25); // Pre-fill 0.25%

    @DecimalMin(value = "0.0", inclusive = true, message = "Harga jual target tidak boleh negatif")
    private BigDecimal targetSellPrice;
}
