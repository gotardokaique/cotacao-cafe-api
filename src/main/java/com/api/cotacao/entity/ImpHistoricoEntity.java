package com.api.cotacao.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "imp_historico")
public class ImpHistoricoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "imp_id")
    private Long id;

    @Column(name = "imp_nomeArquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "imp_origem", nullable = false, length = 50)
    private String origem;

    @Column(name = "imp_data", nullable = false)
    private LocalDateTime data;

    @Column(name = "imp_totalRegistro", nullable = false)
    private Integer totalRegistro;

    @Column(name = "imp_tempoExecucao", nullable = false)
    private Long tempoExecucaoMs;

    public ImpHistoricoEntity() {
    }

    public ImpHistoricoEntity(
            String nomeArquivo,
            String origem,
            LocalDateTime data,
            Integer totalRegistro,
            Long tempoExecucaoMs
    ) {
        this.nomeArquivo = nomeArquivo;
        this.origem = origem;
        this.data = data;
        this.totalRegistro = totalRegistro;
        this.tempoExecucaoMs = tempoExecucaoMs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public Integer getTotalRegistro() {
        return totalRegistro;
    }

    public void setTotalRegistro(Integer totalRegistro) {
        this.totalRegistro = totalRegistro;
    }

    public Long getTempoExecucaoMs() {
        return tempoExecucaoMs;
    }

    public void setTempoExecucaoMs(Long tempoExecucaoMs) {
        this.tempoExecucaoMs = tempoExecucaoMs;
    }

    @Override
    public String toString() {
        return "ImpHistoricoEntity{" +
                "id=" + id +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", origem='" + origem + '\'' +
                ", data=" + data +
                ", totalRegistro=" + totalRegistro +
                ", tempoExecucaoMs=" + tempoExecucaoMs +
                '}';
    }
}
