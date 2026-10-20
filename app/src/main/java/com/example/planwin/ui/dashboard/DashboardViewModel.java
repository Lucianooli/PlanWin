package com.example.planwin.ui.dashboard;

import androidx.lifecycle.ViewModel;
import java.time.YearMonth;

public class DashboardViewModel extends ViewModel {
    private YearMonth mesSelecionado = YearMonth.now();
    private boolean visualizandoReceitas = true;

    public YearMonth getMesSelecionado() {
        return mesSelecionado;
    }

    public void setMesSelecionado(YearMonth mesSelecionado) {
        this.mesSelecionado = mesSelecionado;
    }

    public boolean isVisualizandoReceitas() {
        return visualizandoReceitas;
    }

    public void setVisualizandoReceitas(boolean visualizandoReceitas) {
        this.visualizandoReceitas = visualizandoReceitas;
    }
}