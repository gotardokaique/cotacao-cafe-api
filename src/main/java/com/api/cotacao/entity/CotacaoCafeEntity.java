package com.api.cotacao.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cotacao_cafe")
public class CotacaoCafeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cotcaf_id")
	private Long id;

	@Column(name = "cotcaf_dataReferencia", nullable = false)
	private LocalDate dataReferencia;

	@Column(name = "cotcaf_tipo", length = 100)
	private String tipoCafe;

	@Column(name = "cotcaf_valor", nullable = false)
	private Double valor;

	@Column(name = "cotcaf_variacao")
	private Double variacao;

	@Column(name = "cotcaf_origem", length = 255)
	private String origemDado;

	@Column(name = "cotcaf_criadoEm")
	private LocalDate criadoEm = LocalDate.now();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDataReferencia() {
		return dataReferencia;
	}

	public void setDataReferencia(LocalDate dataReferencia) {
		this.dataReferencia = dataReferencia;
	}

	public String getTipoCafe() {
		return tipoCafe;
	}

	public void setTipoCafe(String tipoCafe) {
		this.tipoCafe = tipoCafe;
	}

	public BigDecimal getValor() {
		return new BigDecimal(valor);
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Double getVariacao() {
		return variacao;
	}

	public void setVariacao(Double variacao) {
		this.variacao = variacao;
	}

	public String getOrigemDado() {
		return origemDado;
	}

	public void setOrigemDado(String origemDado) {
		this.origemDado = origemDado;
	}

	public LocalDate getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDate criadoEm) {
		this.criadoEm = criadoEm;
	}

	public CotacaoCafeEntity() {
	}

	public CotacaoCafeEntity(LocalDate dataReferencia, String tipoCafe, Double valor, Double variacao) {
		this.dataReferencia = dataReferencia;
		this.tipoCafe = tipoCafe;
		this.valor = valor;
		this.variacao = variacao;
		this.origemDado = "CEPEA";
	}

	@Override
	public String toString() {
		return "CotacaoCafeEntity{" + "id=" + id + ", dataReferencia=" + dataReferencia + ", tipoCafe='" + tipoCafe
				+ '\'' + ", valor=" + valor + ", variacao=" + variacao + ", origemDado='" + origemDado + '\'' + '}';
	}

}
