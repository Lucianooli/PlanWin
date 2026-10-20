
package com.example.planwin.service;

import com.example.planwin.entity.Conta;
import com.example.planwin.entity.Renda;
import com.example.planwin.entity.ResumoFinanceiro;
import com.example.planwin.entity.Usuario;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.example.planwin.repository.ContaRepository;

public class ServiceConta {

    private final Usuario usuario;
    private final ContaRepository contaRepository;

    public ServiceConta(Context context, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "O usuário não pode ser nulo."
            );
        }

        this.usuario = usuario;
        this.contaRepository = new ContaRepository(context);

        // Recupera as contas salvas no SQLite.
        usuario.getContasFixas().clear();
        usuario.getContaDoMes().clear();

        for (Conta conta : contaRepository.listarTodas()) {
            if ("Fixa".equalsIgnoreCase(conta.getTipo())) {
                usuario.getContasFixas().add(conta);
            } else {
                usuario.getContaDoMes().add(conta);
            }
        }
    }

    // =========================
    // CADASTRO DE CONTAS
    // =========================

    public boolean cadastrarConta(Conta conta) {
        if (conta == null
                || conta.getNome() == null
                || conta.getNome().trim().isEmpty()
                || conta.getValor() == null
                || conta.getValor().compareTo(BigDecimal.ZERO) < 0
                || conta.getTipo() == null
                || conta.getDataDeLancamento() == null) {
            return false;
        }

        String tipo = conta.getTipo().trim();

        if (!tipo.equalsIgnoreCase("Fixa")
                && !tipo.equalsIgnoreCase("Mes")) {
            return false;
        }

        if (conta.getId() < 0) {
            return false;
        }

        // Verifica duplicidade usando os dados do banco.
        for (Conta existente : contaRepository.listarTodas()) {
            boolean mesmoId = conta.getId() > 0
                    && existente.getId() == conta.getId();

            boolean mesmoNome = existente.getNome()
                    .equalsIgnoreCase(conta.getNome().trim());

            boolean mesmoValor = existente.getValor()
                    .compareTo(conta.getValor()) == 0;

            if (mesmoId || (mesmoNome && mesmoValor)) {
                return false;
            }
        }

        // Persiste a conta no SQLite.
        long idGerado = contaRepository.inserir(conta);

        if (idGerado == -1) {
            return false;
        }

        // Atualiza as listas usadas pelos cálculos financeiros.
        if (tipo.equalsIgnoreCase("Fixa")) {
            usuario.getContasFixas().add(conta);
        } else {
            usuario.getContaDoMes().add(conta);
        }

        return true;
    }

    private boolean existeContaComId(int id) {
        for (Conta conta : usuario.getContasFixas()) {
            if (conta.getId() == id) {
                return true;
            }
        }

        for (Conta conta : usuario.getContaDoMes()) {
            if (conta.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private boolean existeContaComMesmoNomeEValor(Conta novaConta) {
        return existeContaDuplicada(
                usuario.getContasFixas(), novaConta
        ) || existeContaDuplicada(
                usuario.getContaDoMes(), novaConta
        );
    }

    private boolean existeContaDuplicada(
            List<Conta> contas, Conta novaConta) {

        for (Conta conta : contas) {
            boolean mesmoNome = conta.getNome() != null
                    && conta.getNome().trim().equalsIgnoreCase(
                    novaConta.getNome().trim()
            );

            boolean mesmoValor = conta.getValor() != null
                    && conta.getValor().compareTo(
                    novaConta.getValor()
            ) == 0;

            if (mesmoNome && mesmoValor) {
                return true;
            }
        }

        return false;
    }

    // =========================
    // CADASTRO DE RENDAS
    // =========================

    public boolean cadastrarRenda(Renda renda) {
        if (renda == null
                || renda.getNome() == null
                || renda.getNome().trim().isEmpty()
                || renda.getValor() == null
                || renda.getValor().compareTo(BigDecimal.ZERO) < 0
                || renda.getTipo() == null
                || renda.getData() == null
                || renda.getId() <= 0) {
            return false;
        }

        String tipo = renda.getTipo().trim();

        if (!tipo.equalsIgnoreCase("Fixa")
                && !tipo.equalsIgnoreCase("Mes")) {
            return false;
        }

        if (existeRendaComId(renda.getId())) {
            return false;
        }

        if (existeRendaComMesmoNomeEValor(renda)) {
            return false;
        }

        if (tipo.equalsIgnoreCase("Fixa")) {
            usuario.getRendafixa().add(renda);
        } else {
            usuario.getRendaMes().add(renda);
        }

        return true;
    }

    private boolean existeRendaComId(int id) {
        for (Renda renda : usuario.getRendafixa()) {
            if (renda.getId() == id) {
                return true;
            }
        }

        for (Renda renda : usuario.getRendaMes()) {
            if (renda.getId() == id) {
                return true;
            }
        }

        return false;
    }

    private boolean existeRendaComMesmoNomeEValor(Renda novaRenda) {
        return existeRendaDuplicada(
                usuario.getRendafixa(), novaRenda
        ) || existeRendaDuplicada(
                usuario.getRendaMes(), novaRenda
        );
    }

    private boolean existeRendaDuplicada(
            List<Renda> rendas, Renda novaRenda) {

        for (Renda renda : rendas) {
            boolean mesmoNome = renda.getNome() != null
                    && renda.getNome().trim().equalsIgnoreCase(
                    novaRenda.getNome().trim()
            );

            boolean mesmoValor = renda.getValor() != null
                    && renda.getValor().compareTo(
                    novaRenda.getValor()
            ) == 0;

            if (mesmoNome && mesmoValor) {
                return true;
            }
        }

        return false;
    }

    // =========================
    // CONSULTA DE CONTAS E RENDAS
    // =========================

    public List<Conta> listarContasFixas() {
        return usuario.getContasFixas();
    }

    public List<Conta> listarContas() {
        return contaRepository.listarTodas();
    }

    public List<Conta> listarContasDoMes() {
        return usuario.getContaDoMes();
    }

    public List<Renda> listarRendasFixas() {
        return usuario.getRendafixa();
    }

    public List<Renda> listarRendasDoMes() {
        return usuario.getRendaMes();
    }

    // =========================
    // CÁLCULOS FINANCEIROS
    // =========================

    public BigDecimal totalDeGastosMes(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        // Contas fixas: contam a partir do mês de lançamento.
        for (Conta conta : usuario.getContasFixas()) {
            if (conta.getDataDeLancamento() != null
                    && !YearMonth.from(
                    conta.getDataDeLancamento()
            ).isAfter(mes)) {
                total = total.add(conta.getValor());
            }
        }

        // Contas do mês: contam somente no mês de lançamento.
        for (Conta conta : usuario.getContaDoMes()) {
            if (conta.getDataDeLancamento() != null
                    && YearMonth.from(
                    conta.getDataDeLancamento()
            ).equals(mes)) {
                total = total.add(conta.getValor());
            }
        }

        return total;
    }

    public BigDecimal totalDeLucroMes(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        BigDecimal total = BigDecimal.ZERO;

        // Rendas fixas: contam a partir do mês cadastrado.
        for (Renda renda : usuario.getRendafixa()) {
            if (renda.getData() != null
                    && !renda.getData().isAfter(mes)) {
                total = total.add(renda.getValor());
            }
        }

        // Rendas do mês: contam somente no mês cadastrado.
        for (Renda renda : usuario.getRendaMes()) {
            if (renda.getData() != null
                    && renda.getData().equals(mes)) {
                total = total.add(renda.getValor());
            }
        }

        return total;
    }

    public BigDecimal resumoFinanceiroMes(YearMonth mes) {
        BigDecimal rendas = totalDeLucroMes(mes);
        BigDecimal gastos = totalDeGastosMes(mes);

        return rendas.subtract(gastos);
    }

    // =========================
    // RESUMO FINANCEIRO
    // =========================

    public ResumoFinanceiro cadastrarResumoFinanceiro(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        BigDecimal rendas = totalDeLucroMes(mes);
        BigDecimal gastos = totalDeGastosMes(mes);
        BigDecimal saldo = rendas.subtract(gastos);

        List<ResumoFinanceiro> resumos =
                usuario.getResumoFinanceiros();

        // Atualiza o resumo caso já exista um para esse mês.
        for (ResumoFinanceiro resumo : resumos) {
            if (resumo.getMesAno().equals(mes)) {
                resumo.setTotalLucro(rendas);
                resumo.setTotalDespesa(gastos);
                resumo.setTotalFinal(saldo);

                return resumo;
            }
        }

        int novoId = 1;

        for (ResumoFinanceiro resumo : resumos) {
            if (resumo.getId() >= novoId) {
                novoId = resumo.getId() + 1;
            }
        }

        ResumoFinanceiro resumo = new ResumoFinanceiro(
                novoId,
                mes,
                rendas,
                gastos,
                saldo
        );

        resumos.add(resumo);

        return resumo;
    }


    public boolean atualizarConta(Conta contaAtualizada) {
        if (contaAtualizada == null
                || contaAtualizada.getId() <= 0
                || contaAtualizada.getNome() == null
                || contaAtualizada.getNome().trim().isEmpty()
                || contaAtualizada.getValor() == null
                || contaAtualizada.getValor().compareTo(BigDecimal.ZERO) <= 0
                || contaAtualizada.getTipo() == null
                || contaAtualizada.getDataDeLancamento() == null) {
            return false;
        }

        String tipo = contaAtualizada.getTipo().trim();

        if (!tipo.equalsIgnoreCase("Fixa")
                && !tipo.equalsIgnoreCase("Mes")) {
            return false;
        }

        // Confirma que a conta existe no banco.
        Conta existente = contaRepository.buscarPorId(
                contaAtualizada.getId()
        );

        if (existente == null) {
            return false;
        }

        // Atualiza o registro no SQLite.
        int linhasAtualizadas =
                contaRepository.atualizar(contaAtualizada);

        if (linhasAtualizadas <= 0) {
            return false;
        }

        // Remove a versão antiga das listas.
        usuario.getContasFixas().removeIf(
                c -> c.getId() == contaAtualizada.getId()
        );

        usuario.getContaDoMes().removeIf(
                c -> c.getId() == contaAtualizada.getId()
        );

        // Insere a versão atualizada na lista correta.
        if (tipo.equalsIgnoreCase("Fixa")) {
            usuario.getContasFixas().add(contaAtualizada);
        } else {
            usuario.getContaDoMes().add(contaAtualizada);
        }

        return true;
    }

    public boolean excluirConta(int id) {
        if (id <= 0) {
            return false;
        }

        // Exclui primeiro do banco.
        int linhasExcluidas = contaRepository.excluir(id);

        if (linhasExcluidas <= 0) {
            return false;
        }

        // Remove das listas usadas pelos cálculos.
        usuario.getContasFixas().removeIf(
                conta -> conta.getId() == id
        );

        usuario.getContaDoMes().removeIf(
                conta -> conta.getId() == id
        );

        return true;
    }

}
