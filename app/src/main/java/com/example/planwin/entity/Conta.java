package com.example.planwin.entity;

import com.example.planwin.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Conta {

    private int id;
    private String nome;
    private BigDecimal valor;
    private LocalDate dataDeVencimento;
    private Integer diaVencimentoFixo; // Novo: Para contas fixas (ex: dia 10 de todo mês)
    private LocalDate dataDeLancamento;
    private String tipo;
    private String categoria; // Novo: "Casa", "Alimento", "Saúde", "Carro", "Viagem", "Roupa", "Cartão", "Outro"
    private StatusPagamento statusPagamento;
    private LocalDate dataPagamento;

    // Construtor original mantido por compatibilidade
    public Conta(
            int id,
            String nome,
            BigDecimal valor,
            LocalDate dataDeVencimento,
            LocalDate dataDeLancamento,
            String tipo,
            StatusPagamento statusPagamento
    ) {
        this(
                id,
                nome,
                valor,
                dataDeVencimento,
                null,
                dataDeLancamento,
                tipo,
                "Outro",
                statusPagamento,
                null
        );
    }

    // Construtor completo atualizado
    public Conta(
            int id,
            String nome,
            BigDecimal valor,
            LocalDate dataDeVencimento,
            Integer diaVencimentoFixo,
            LocalDate dataDeLancamento,
            String tipo,
            String categoria,
            StatusPagamento statusPagamento,
            LocalDate dataPagamento
    ) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.dataDeVencimento = dataDeVencimento;
        this.diaVencimentoFixo = diaVencimentoFixo;
        this.dataDeLancamento = dataDeLancamento;
        this.tipo = tipo;
        this.categoria = categoria != null && !categoria.trim().isEmpty() ? categoria : "Outro";
        this.statusPagamento = statusPagamento != null
                ? statusPagamento
                : StatusPagamento.PENDENTE;
        this.dataPagamento = dataPagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getDataDeVencimento() {
        // Se for conta fixa e tiver dia fixo informado, calcula o vencimento para o mês atual dinamicamente
        if ("Fixa".equalsIgnoreCase(tipo) && diaVencimentoFixo != null) {
            LocalDate hoje = LocalDate.now();
            try {
                int ultimoDiaMes = hoje.getMonth().length(hoje.isLeapYear());
                int diaUtil = Math.min(diaVencimentoFixo, ultimoDiaMes);
                return LocalDate.of(hoje.getYear(), hoje.getMonth(), diaUtil);
            } catch (Exception e) {
                return dataDeVencimento;
            }
        }
        return dataDeVencimento;
    }

    public void setDataDeVencimento(LocalDate dataDeVencimento) {
        this.dataDeVencimento = dataDeVencimento;
    }

    public Integer getDiaVencimentoFixo() {
        return diaVencimentoFixo;
    }

    public void setDiaVencimentoFixo(Integer diaVencimentoFixo) {
        this.diaVencimentoFixo = diaVencimentoFixo;
    }

    public LocalDate getDataDeLancamento() {
        return dataDeLancamento;
    }

    public void setDataDeLancamento(LocalDate dataDeLancamento) {
        this.dataDeLancamento = dataDeLancamento;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria != null ? categoria : "Outro";
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(StatusPagamento statusPagamento) {
        this.statusPagamento = statusPagamento != null
                ? statusPagamento
                : StatusPagamento.PENDENTE;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public StatusPagamento getStatusPagamentoEfetivo() {
        if (statusPagamento == StatusPagamento.PAGA) {
            return StatusPagamento.PAGA;
        }

        LocalDate vencimentoEfetivo = getDataDeVencimento();
        if (vencimentoEfetivo != null
                && vencimentoEfetivo.isBefore(LocalDate.now())) {
            return StatusPagamento.ATRASADA;
        }

        return StatusPagamento.PENDENTE;
    }

    public boolean isPaga() {
        return statusPagamento == StatusPagamento.PAGA;
    }

    public void setPaga(boolean paga) {
        if (paga) {
            this.statusPagamento = StatusPagamento.PAGA;

            if (this.dataPagamento == null) {
                this.dataPagamento = LocalDate.now();
            }
        } else {
            this.statusPagamento = StatusPagamento.PENDENTE;
            this.dataPagamento = null;
        }
    }
}