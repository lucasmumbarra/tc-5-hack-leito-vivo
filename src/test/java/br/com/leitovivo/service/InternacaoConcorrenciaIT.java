package br.com.leitovivo.service;

import br.com.leitovivo.domain.leito.enums.TipoLeito;
import br.com.leitovivo.persistence.repository.InternacaoRepository;
import br.com.leitovivo.persistence.enums.StatusInternacao;
import br.com.leitovivo.web.dto.request.CriarInternacaoRequest;
import br.com.leitovivo.web.dto.request.CriarLeitoRequest;
import br.com.leitovivo.web.dto.request.CriarPacienteRequest;
import br.com.leitovivo.web.dto.request.CriarUnidadeRequest;
import br.com.leitovivo.web.dto.response.InternacaoResponse;
import br.com.leitovivo.web.dto.response.LeitoResponse;
import br.com.leitovivo.web.dto.response.PacienteResponse;
import br.com.leitovivo.web.dto.response.UnidadeResponse;
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

import java.time.LocalDate;
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
class InternacaoConcorrenciaIT {

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
    private PacienteService pacienteService;
    @Autowired
    private InternacaoService internacaoService;
    @Autowired
    private InternacaoRepository internacaoRepository;

    @Test
    @Timeout(60)
    void duasInternacoesSimultaneasNoMesmoLeito() throws Exception {
        UnidadeResponse unidade = unidadeService.criar(
                new CriarUnidadeRequest("Hospital Race", "SP", "Sudeste", "Geral"));
        LeitoResponse leito = leitoService.criar(
                new CriarLeitoRequest(unidade.id(), "RACE-01", TipoLeito.UTI));
        PacienteResponse p1 = pacienteService.criar(
                new CriarPacienteRequest("Paciente A", LocalDate.of(1990, 1, 1), "111"));
        PacienteResponse p2 = pacienteService.criar(
                new CriarPacienteRequest("Paciente B", LocalDate.of(1991, 2, 2), "222"));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger falhas = new AtomicInteger();
        List<Throwable> erros = new ArrayList<>();

        try {
            Future<?> f1 = pool.submit(() -> tentarInternar(leito.id(), p1.id(), ready, start, sucessos, falhas, erros));
            Future<?> f2 = pool.submit(() -> tentarInternar(leito.id(), p2.id(), ready, start, sucessos, falhas, erros));

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            f1.get(30, TimeUnit.SECONDS);
            f2.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, sucessos.get(), "exatamente uma internação deve ter sucesso: " + erros);
        assertEquals(1, falhas.get(), "exatamente uma deve falhar com conflito: " + erros);
        assertEquals(1, internacaoRepository.countByLeitoIdAndStatus(leito.id(), StatusInternacao.ATIVA));
    }

    private void tentarInternar(
            java.util.UUID leitoId,
            java.util.UUID pacienteId,
            CountDownLatch ready,
            CountDownLatch start,
            AtomicInteger sucessos,
            AtomicInteger falhas,
            List<Throwable> erros) {
        try {
            ready.countDown();
            start.await(10, TimeUnit.SECONDS);
            InternacaoResponse response = internacaoService.internar(
                    new CriarInternacaoRequest(leitoId, pacienteId, "race"));
            if (response != null && response.status() == StatusInternacao.ATIVA) {
                sucessos.incrementAndGet();
            }
        } catch (OptimisticLockingFailureException | br.com.leitovivo.exception.ConflitoNegocioException
                 | br.com.leitovivo.exception.TransicaoInvalidaException ex) {
            falhas.incrementAndGet();
            synchronized (erros) {
                erros.add(ex);
            }
        } catch (Exception ex) {
            synchronized (erros) {
                erros.add(ex);
            }
            throw new RuntimeException(ex);
        }
    }
}
