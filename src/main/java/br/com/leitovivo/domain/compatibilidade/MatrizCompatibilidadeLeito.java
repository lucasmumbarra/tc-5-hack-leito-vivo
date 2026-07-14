package br.com.leitovivo.domain.compatibilidade;

import br.com.leitovivo.domain.leito.enums.TipoLeito;

import java.util.Set;

public final class MatrizCompatibilidadeLeito {

    private MatrizCompatibilidadeLeito() {
    }

    public static Set<TipoLeito> tiposCompativeis(TipoLeito necessidade) {
        return switch (necessidade) {
            case ENFERMARIA -> Set.of(TipoLeito.ENFERMARIA, TipoLeito.CLINICO, TipoLeito.UTI);
            case CLINICO -> Set.of(TipoLeito.CLINICO, TipoLeito.UTI);
            case UTI -> Set.of(TipoLeito.UTI);
            case UTI_NEONATAL -> Set.of(TipoLeito.UTI_NEONATAL);
            case PEDIATRICO -> Set.of(TipoLeito.PEDIATRICO);
            case OBSTETRICO -> Set.of(TipoLeito.OBSTETRICO);
            case ISOLAMENTO -> Set.of(TipoLeito.ISOLAMENTO);
        };
    }

    public static boolean atende(TipoLeito necessidade, TipoLeito tipoDisponivel) {
        return tiposCompativeis(necessidade).contains(tipoDisponivel);
    }
}
