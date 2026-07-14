package br.com.leitovivo.service;

import br.com.leitovivo.domain.AutorAcao;
import br.com.leitovivo.domain.EventoLeito;
import br.com.leitovivo.domain.StatusLeito;
import br.com.leitovivo.domain.TipoLeito;
import br.com.leitovivo.exception.TransicaoInvalidaException;
import br.com.leitovivo.web.dto.CriarLeitoRequest;
import br.com.leitovivo.web.dto.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.LeitoResponse;
import br.com.leitovivo.web.dto.UnidadeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ReservaConcorrenciaIT {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("leitovivo")
            .withUsername("leitovivo")
            .withPassword("leitovivo");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UnidadeService unidadeService;
    @Autowired
    private LeitoService leitoService;
    @Autowired
    private BuscaLeitoService buscaLeitoService;

    @Test
    @Timeout(60)
    void duasReservasSimultaneasNoMesmoLeito() throws Exception {
        UnidadeResponse unidade = unidadeService.criar(
                new CriarUnidadeRequest("Hospital Reserva Race", "SP", "Sudeste", "Geral"));
        LeitoResponse leito = leitoService.criar(
                new CriarLeitoRequest(unidade.id(), "RES-RACE-01", TipoLeito.UTI));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger falhas = new AtomicInteger();
        List<Throwable> erros = new ArrayList<>();

        try {
            Future<?> f1 = pool.submit(() -> tentarReservar(leito.id(), ready, start, sucessos, falhas, erros));
            Future<?> f2 = pool.submit(() -> tentarReservar(leito.id(), ready, start, sucessos, falhas, erros));

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            f1.get(30, TimeUnit.SECONDS);
            f2.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, sucessos.get(), "exatamente uma reserva deve ter sucesso: " + erros);
        assertEquals(1, falhas.get(), "exatamente uma deve falhar com conflito: " + erros);

        assertTrue(buscaLeitoService.buscarCompativeis(TipoLeito.UTI, "Sudeste").stream()
                .noneMatch(l -> l.id().equals(leito.id())),
                "leito RESERVADO não aparece na busca");
    }

    private void tentarReservar(
            java.util.UUID leitoId,
            CountDownLatch ready,
            CountDownLatch start,
            AtomicInteger sucessos,
            AtomicInteger falhas,
            List<Throwable> erros) {
        try {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            LeitoResponse r = leitoService.transicionar(
                    leitoId, EventoLeito.RESERVAR_LEITO, AutorAcao.USUARIO, "concorrencia");
            assertEquals(StatusLeito.RESERVADO, r.status());
            sucessos.incrementAndGet();
        } catch (OptimisticLockingFailureException
                 | org.springframework.orm.ObjectOptimisticLockingFailureException
                 | TransicaoInvalidaException e) {
            falhas.incrementAndGet();
        } catch (Exception e) {
            erros.add(e);
            falhas.incrementAndGet();
        }
    }
}
