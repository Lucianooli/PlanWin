package com.example.planwin.service;

import android.content.Context;

import com.example.planwin.entity.Renda;
import com.example.planwin.repository.RendaRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ServiceRenda {

    private final RendaRepository rendaRepository;

    public ServiceRenda(Context context) {
        rendaRepository = new RendaRepository(context);
    }

    // Cadastra uma nova renda.
    public boolean cadastrarRenda(Renda renda) {
        if (!dadosValidos(renda)) {
            return false;
        }

        if (existeRendaComMesmoNomeEValor(renda)) {
            return false;
        }

        long id = rendaRepository.inserir(renda);

        if (id == -1) {
            return false;
        }

        renda.setId((int) id);

        return true;
    }

    // Lista todas as rendas salvas no banco.
    public List<Renda> listarRendas() {
        return rendaRepository.listarTodas();
    }

    // Busca uma renda pelo ID.
    public Renda buscarRendaPorId(int id) {
        if (id <= 0) {
            return null;
        }

        return rendaRepository.buscarPorId(id);
    }

    // Atualiza uma renda existente.
    public boolean atualizarRenda(Renda renda) {
        if (!dadosValidos(renda) || renda.getId() <= 0) {
            return false;
        }

        Renda existente = rendaRepository.buscarPorId(renda.getId());

        if (existente == null) {
            return false;
        }

        if (existeRendaDuplicadaNaAtualizacao(renda)) {
            return false;
        }

        return rendaRepository.atualizar(renda) > 0;
    }

    // Exclui uma renda.
    public boolean excluirRenda(int id) {
        if (id <= 0) {
            return false;
        }

        return rendaRepository.excluir(id) > 0;
    }

    // Lista somente as rendas fixas.
    public List<Renda> listarRendasFixas() {
        List<Renda> resultado = new ArrayList<>();

        for (Renda renda : rendaRepository.listarTodas()) {
            if ("Fixa".equalsIgnoreCase(renda.getTipo())) {
                resultado.add(renda);
            }
        }

        return resultado;
    }

    // Lista as rendas classificadas como mensais/variáveis.
    public List<Renda> listarRendasDoMes() {
        List<Renda> resultado = new ArrayList<>();

        for (Renda renda : rendaRepository.listarTodas()) {
            if ("Mes".equalsIgnoreCase(renda.getTipo())) {
                resultado.add(renda);
            }
        }

        return resultado;
    }

    // Soma as rendas de um determinado mês.
    public BigDecimal calcularTotalRendas(YearMonth mes) {
        BigDecimal total = BigDecimal.ZERO;

        if (mes == null) {
            return total;
        }

        for (Renda renda : rendaRepository.listarTodas()) {
            if (mes.equals(renda.getData())) {
                total = total.add(renda.getValor());
            }
        }

        return total;
    }

    // Valida os dados antes de acessar o banco.
    private boolean dadosValidos(Renda renda) {
        if (renda == null
                || renda.getNome() == null
                || renda.getNome().trim().isEmpty()
                || renda.getValor() == null
                || renda.getValor().compareTo(BigDecimal.ZERO) <= 0
                || renda.getData() == null
                || renda.getTipo() == null) {
            return false;
        }

        String tipo = renda.getTipo().trim();

        return tipo.equalsIgnoreCase("Fixa")
                || tipo.equalsIgnoreCase("Mes");
    }

    // Evita cadastros com mesmo nome, valor, mês e tipo.
    private boolean existeRendaComMesmoNomeEValor(Renda renda) {
        for (Renda existente : rendaRepository.listarTodas()) {
            if (mesmaRenda(renda, existente)) {
                return true;
            }
        }

        return false;
    }

    // Na edição, ignora o próprio registro.
    private boolean existeRendaDuplicadaNaAtualizacao(Renda renda) {
        for (Renda existente : rendaRepository.listarTodas()) {
            if (existente.getId() != renda.getId()
                    && mesmaRenda(renda, existente)) {
                return true;
            }
        }

        return false;
    }

    private boolean mesmaRenda(Renda a, Renda b) {
        return a.getNome().trim().equalsIgnoreCase(b.getNome().trim())
                && a.getValor().compareTo(b.getValor()) == 0
                && a.getData().equals(b.getData())
                && a.getTipo().equalsIgnoreCase(b.getTipo());
    }

    public void fechar() {
        rendaRepository.fechar();
    }
}