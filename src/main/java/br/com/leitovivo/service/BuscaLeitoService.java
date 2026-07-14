package br.com.leitovivo.service;

import br.com.leitovivo.domain.compatibilidade.MatrizCompatibilidadeLeito;
import br.com.leitovivo.domain.leito.enums.StatusLeito;
import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.persistence.entity.Leito;
import br.com.leitovivo.persistence.repository.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.web.dto.response.LeitoCompativelResponse;
import br.com.leitovivo.web.mapper.BuscaLeitoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class BuscaLeitoService {

    private final LeitoBuscaIndicadorRepository leitoBuscaIndicadorRepository;

    public BuscaLeitoService(LeitoBuscaIndicadorRepository leitoBuscaIndicadorRepository) {
        this.leitoBuscaIndicadorRepository = leitoBuscaIndicadorRepository;
    }

    @Transactional(readOnly = true)
    public List<LeitoCompativelResponse> buscarCompativeis(TipoLeito necessidade, String regiao) {
        if (necessidade == null) {
            throw new PayloadInvalidoException("necessidade é obrigatória");
        }
        if (regiao == null || regiao.isBlank()) {
            throw new PayloadInvalidoException("regiao é obrigatória");
        }

        Set<TipoLeito> tipos = MatrizCompatibilidadeLeito.tiposCompativeis(necessidade);
        return leitoBuscaIndicadorRepository
                .findByRegiaoAndStatusAndTipoIn(regiao.trim(), StatusLeito.LIVRE, tipos)
                .stream()
                .map(BuscaLeitoMapper::toResponse)
                .toList();
    }

}
