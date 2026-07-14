package br.com.leitovivo.service;

import br.com.leitovivo.domain.MatrizCompatibilidadeLeito;
import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.exception.PayloadInvalidoException;
import br.com.leitovivo.persistence.Leito;
import br.com.leitovivo.persistence.LeitoBuscaIndicadorRepository;
import br.com.leitovivo.web.dto.LeitoCompativelResponse;
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
                .map(this::toResponse)
                .toList();
    }

    private LeitoCompativelResponse toResponse(Leito leito) {
        return new LeitoCompativelResponse(
                leito.getId(),
                leito.getUnidade().getId(),
                leito.getUnidade().getNome(),
                leito.getUnidade().getRegiao(),
                leito.getCodigo(),
                leito.getTipo());
    }
}
