package com.api.cotacao.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotacaoCafePeriodoDTO(
        LocalDate data,
        BigDecimal valorCafe
) {}
