package com.example.planwin.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public class Renda {

    private int id;
    private String nome;
    private BigDecimal valor;
    private YearMonth data; // Usada para renda pontual do mês
    private Integer diaRecebimentoFixo; // Usado para rendas fixas (ex: dia 5 de todo mês)
    private String tipo; // "Fixa" or "Mes"
    private String categoria; // "Salário", "Extra", "Investimento", "Outro"
    private String descricao;

    // Construtor original mantido por compatibilidade
    public Renda(
            int id,
            String nome,
            BigDecimal valor,
            YearMonth data,
            String tipo,
            String descricao
    ) {
        this(id, nome, valor, data, null, tipo, "Outro", descricao);
    }

    // Construtor completo com Categoria e Dia Fixo
    public Renda(
            int id,
            String nome,
            BigDecimal valor,
            YearMonth data,
            Integer diaRecebimentoFixo,
            String tipo,
            String categoria,
            String descricao
    ) {
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.data = data;
        this.diaRecebimentoFixo = diaRecebimentoFixo;
        this.tipo = tipo;
        this.categoria = categoria != null && !categoria.trim().isEmpty() ? categoria : "Outro";
        this.descricao = descricao;
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

    public YearMonth getData() {
        // Se for renda fixa e tiver dia fixo, calcula o YearMonth para o mês atual dinamicamente
        if ("Fixa".equalsIgnoreCase(tipo) && diaRecebimentoFixo != null) {
            LocalDate hoje = LocalDate.now();
            return YearMonth.of(hoje.getYear(), hoje.getMonth());
        }
        return data;
    }

    public void setData(YearMonth data) {
        this.data = data;
    }

    public Integer getDiaRecebimentoFixo() {
        return diaRecebimentoFixo;
    }

    public void setDiaRecebimentoFixo(Integer diaRecebimentoFixo) {
        this.diaRecebimentoFixo = diaRecebimentoFixo;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}