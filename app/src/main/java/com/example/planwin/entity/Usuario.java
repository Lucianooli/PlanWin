
package com.example.planwin.entity;

import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int id;
    private String nome;
    private ArrayList<Conta> contasFixas;
    private ArrayList<Conta> contaDoMes;
    private ArrayList<Renda> rendafixa;
    private ArrayList<Renda> rendaMes;
    private List<ResumoFinanceiro> resumoFinanceiros;

    public Usuario(
            int id,
            String nome,
            ArrayList<Conta> contasFixas,
            ArrayList<Conta> contaDoMes,
            ArrayList<Renda> rendafixa,
            ArrayList<Renda> rendaMes,
            List<ResumoFinanceiro> resumoFinanceiros
    ) {
        this.id = id;
        this.nome = nome;
        this.contasFixas = contasFixas;
        this.contaDoMes = contaDoMes;
        this.rendafixa = rendafixa;
        this.rendaMes = rendaMes;
        this.resumoFinanceiros = resumoFinanceiros;
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

    public ArrayList<Conta> getContasFixas() {
        return contasFixas;
    }

    public void setContasFixas(ArrayList<Conta> contasFixas) {
        this.contasFixas = contasFixas;
    }

    public ArrayList<Conta> getContaDoMes() {
        return contaDoMes;
    }

    public void setContaDoMes(ArrayList<Conta> contaDoMes) {
        this.contaDoMes = contaDoMes;
    }

    public ArrayList<Renda> getRendafixa() {
        return rendafixa;
    }

    public void setRendafixa(ArrayList<Renda> rendafixa) {
        this.rendafixa = rendafixa;
    }

    public ArrayList<Renda> getRendaMes() {
        return rendaMes;
    }

    public void setRendaMes(ArrayList<Renda> rendaMes) {
        this.rendaMes = rendaMes;
    }

    public List<ResumoFinanceiro> getResumoFinanceiros() {
        return resumoFinanceiros;
    }

    public void setResumoFinanceiros(
            List<ResumoFinanceiro> resumoFinanceiros
    ) {
        this.resumoFinanceiros = resumoFinanceiros;
    }
}
