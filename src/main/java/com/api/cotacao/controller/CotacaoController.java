package com.api.cotacao.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.cotacao.DTO.CotacaoCafePeriodoDTO;
import com.api.cotacao.service.CotacaoService;
import com.api.cotacao.utils.Utils;

@RestController
@RequestMapping("/api/cotacao/")
public class CotacaoController {

    private static final Logger log = LoggerFactory.getLogger(CotacaoController.class);

    private final CotacaoService cotacaoService;

    public CotacaoController(CotacaoService cotacaoService) {
        this.cotacaoService = cotacaoService;
    }

    @PostMapping("/importar/json")
    public ResponseEntity<String> importarDeJson(@RequestParam("path") String pathJson) {
        try {
        	cotacaoService.importarDeJson(pathJson);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Importação concluída com sucesso para o arquivo: " + pathJson);
        } catch (Exception e) {
            log.error("Erro ao importar arquivo JSON: {}", pathJson, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao importar arquivo JSON.");
        }
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Importação de café API OK");
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<CotacaoCafePeriodoDTO>> listarPorPeriodo(
            @RequestParam("inicio") String inicio,
            @RequestParam("fim") String fim
    ) {
        try {
            LocalDate dataInicio = Utils.parseAnoMes(inicio);   // 2024-01-01
            LocalDate dataFimBase = Utils.parseAnoMes(fim);     // 2024-12-01

            if (dataInicio == null || dataFimBase == null) {
                return ResponseEntity.badRequest().build();
            }

            LocalDate dataFim = dataFimBase.withDayOfMonth(dataFimBase.lengthOfMonth()); // 2024-12-31

            log.info("Buscando cotações de {} até {}", dataInicio, dataFim);

            List<CotacaoCafePeriodoDTO> lista = cotacaoService.buscarPorPeriodo(dataInicio, dataFim);
            return ResponseEntity.ok(lista);

        } catch (Exception e) {
            log.error("Erro ao buscar cotações por período: inicio={}, fim={}", inicio, fim);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
