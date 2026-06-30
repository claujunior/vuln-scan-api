package BCC.ES.CLP.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import BCC.ES.CLP.dto.ScanRawResult;
import BCC.ES.CLP.exceptions.AlvoNaoEncontradoException;
import BCC.ES.CLP.exceptions.ScanOrquestracaoException;
import BCC.ES.CLP.exceptions.TimeoutScan;
import BCC.ES.CLP.model.Alvo;
import BCC.ES.CLP.model.Select;
import BCC.ES.CLP.repository.RepositoryAlvo;

@Service
public class ServiceOrquestrador {

    private final RepositoryAlvo repositoryAlvo;
    private final Map<String, ExecutorStrategy> executores;

    public ServiceOrquestrador(RepositoryAlvo repositoryAlvo,
                               Map<String, ExecutorStrategy> executores) {
        this.repositoryAlvo = repositoryAlvo;
        this.executores = executores;
    }

    public CompletableFuture<ScanRawResult> executarScan(Long id, Select select, String executorNome) {
        return CompletableFuture.supplyAsync(() -> {
            Alvo alvo = repositoryAlvo.findById(id).orElseThrow(AlvoNaoEncontradoException::new);

            ExecutorStrategy executor = executores.get(executorNome);
            if (executor == null) {
                throw new ScanOrquestracaoException("Executor não encontrado: " + executorNome, null);
            }
            String[] command = executor.montarComando(alvo, select);

            try {
                Process process = new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();

                String output;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    output = reader.lines().collect(Collectors.joining("\n"));
                }

                System.out.println("LOG DO SCAN:\n" + output);

                boolean finished = process.waitFor(90, java.util.concurrent.TimeUnit.SECONDS);

                if (!finished) {
                    process.destroyForcibly();
                    throw new TimeoutScan("Timeout no scan");
                }

                return new ScanRawResult(alvo.getIp(), output);
            } catch (Exception e) {
                throw new ScanOrquestracaoException(
                        "Falha no scan em " + alvo.getIp(), e);
            }
        });
    }
}