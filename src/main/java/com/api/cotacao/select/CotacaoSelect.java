package com.api.cotacao.select;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.api.cotacao.db.Condicao;
import com.api.cotacao.db.DAOController;
import com.api.cotacao.entity.CotacaoCafeEntity;

import jakarta.persistence.NoResultException;

public class CotacaoSelect {
	
	public static CotacaoCafeEntity buscarPorDataEOrigem(DAOController dao,LocalDate dataReferencia, String origem) {
		CotacaoCafeEntity cotacaoBean ;
		
		try {
			cotacaoBean= dao.select()
					.from("CotacaoCafeEntity")
					.where("dataReferencia", Condicao.EQUAL, dataReferencia)
					.where("origemDado", Condicao.EQUAL, origem)
					.one();			
			
		} catch (NoResultException not) {
			cotacaoBean = new CotacaoCafeEntity();
			
		}
        
        return cotacaoBean;
		
	}
	
	public static List<CotacaoCafeEntity> buscarPorPeriodo(DAOController dao, LocalDate dataInicio, LocalDate dataFim) {
		 List<CotacaoCafeEntity> cotacaoList;
		 try {
			 cotacaoList = dao.select()
					 .select("dataReferencia", "valor")
					 .from(CotacaoCafeEntity.class)
					 .where("dataReferencia", Condicao.BETWEEN, dataInicio, dataFim)
					 .orderBy("dataReferencia", true)
					 .list();
			 
			 
		 } catch (NoResultException not) {
			 cotacaoList = new ArrayList<CotacaoCafeEntity>();
			 
		 }
		 
		 return cotacaoList;

	    }


}
