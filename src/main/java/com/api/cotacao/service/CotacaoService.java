package com.api.cotacao.service;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.cotacao.DTO.CotacaoCafeDTO;
import com.api.cotacao.DTO.CotacaoCafePeriodoDTO;
import com.api.cotacao.db.DAOController;
import com.api.cotacao.entity.CotacaoCafeEntity;
import com.api.cotacao.entity.ImpHistoricoEntity;
import com.api.cotacao.select.CotacaoSelect;
import com.api.cotacao.utils.Utils;

@Service
public class CotacaoService {

	private static final String ORIGEM_CEPEA = "CEPEA";
	private static final String TIPO_ROBUSTA = "Café Robusta";
	private static final String TIPO_ARABICA = "Café Arabica";



    @Autowired
    private DAOController dao;

    public void importarDeJson(String pathJson) throws Exception {
        long start = System.currentTimeMillis();

        Path path = Path.of(pathJson);
        String nomeArquivo = path.getFileName().toString();

        List<CotacaoCafeDTO> dados = Utils.readJsonList(path, CotacaoCafeDTO.class);

        int totalRegistros = 0;

        for (CotacaoCafeDTO cotacaoDTO : dados) {
            if (cotacaoDTO.mesAno() == null || cotacaoDTO.valor() == null) {
                continue; 
            }

            LocalDate dataReferencia = Utils.parseMesAno(cotacaoDTO.mesAno());

            CotacaoCafeEntity cotacao = CotacaoSelect.buscarPorDataEOrigem(dao, dataReferencia, ORIGEM_CEPEA);

            if (cotacao.getId() == null) {
            	cotacao.setDataReferencia(dataReferencia);
            	cotacao.setTipoCafe(TIPO_ROBUSTA); //Ajustar os arquivo dps apra pegar o tipo
            	cotacao.setValor(cotacaoDTO.valor());
            	cotacao.setVariacao(null);
            	cotacao.setOrigemDado(ORIGEM_CEPEA);
            	cotacao.setCriadoEm(LocalDate.now());

            } else {
            	cotacao.setValor(cotacaoDTO.valor());
            	cotacao.setCriadoEm(LocalDate.now());
            }
            
            salvar(cotacao);

            totalRegistros++;
        }

        long end = System.currentTimeMillis();
        long tempoExecucao = end - start;

        //histórico 
        ImpHistoricoEntity historico = new ImpHistoricoEntity();
        historico.setNomeArquivo(nomeArquivo);
        historico.setOrigem(ORIGEM_CEPEA);
        historico.setData(LocalDateTime.now());
        historico.setTotalRegistro(totalRegistros);
        historico.setTempoExecucaoMs(tempoExecucao);
        
        historicoSave(historico);

       
    }
    
    public List<CotacaoCafePeriodoDTO> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<CotacaoCafeEntity> cotacaoList = CotacaoSelect.buscarPorPeriodo(dao, dataInicio, dataFim);

        List<CotacaoCafePeriodoDTO> cotacaDTO = new ArrayList<>();

        for (CotacaoCafeEntity cotacaoEntity : cotacaoList) {
            
        	cotacaDTO.add(new CotacaoCafePeriodoDTO(
        			cotacaoEntity.getDataReferencia(),
        			Utils.round(cotacaoEntity.getValor(), 2)
        			
            ));
        }

        return cotacaDTO;
    }
    
    private void historicoSave(ImpHistoricoEntity historico) {
    	
    	dao.insert(historico);
    	
    }
    
    private void salvar(CotacaoCafeEntity cotacaoEntity) {
    	if (cotacaoEntity.getId() == null) {
    		dao.insert(cotacaoEntity);
    	} else {
    		dao.update(cotacaoEntity);
    	}
    	
    }
}
