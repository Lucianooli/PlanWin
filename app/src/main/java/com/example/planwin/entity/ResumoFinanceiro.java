
package com.example.planwin.entity;

import java.math.BigDecimal;
import java.time.YearMonth;

public class ResumoFinanceiro {

    private int id;
    private YearMonth mesAno;
    private BigDecimal totalLucro;
    private BigDecimal totalDespesa;
    private BigDecimal totalFinal;

    public ResumoFinanceiro(
            int id,
            YearMonth mesAno,
            BigDecimal totalLucro,
            BigDecimal totalDespesa,
            BigDecimal totalFinal
    ) {
        this.id = id;
        this.mesAno = mesAno;
        this.totalLucro = totalLucro;
        this.totalDespesa = totalDespesa;
        this.totalFinal = totalFinal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public YearMonth getMesAno() {
        return mesAno;
    }

    public void setMesAno(YearMonth mesAno) {
        this.mesAno = mesAno;
    }

    public BigDecimal getTotalLucro() {
        return totalLucro;
    }

    public void setTotalLucro(BigDecimal totalLucro) {
        this.totalLucro = totalLucro;
    }

    public BigDecimal getTotalDespesa() {
        return totalDespesa;
    }

    public void setTotalDespesa(BigDecimal totalDespesa) {
        this.totalDespesa = totalDespesa;
    }

    public BigDecimal getTotalFinal() {
        return totalFinal;
    }

    public void setTotalFinal(BigDecimal totalFinal) {
        this.totalFinal = totalFinal;
    }
}
