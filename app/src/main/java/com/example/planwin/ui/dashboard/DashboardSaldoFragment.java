package com.example.planwin.ui.dashboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.entity.Renda;
import com.example.planwin.repository.ContaRepository;
import com.example.planwin.service.ServiceRenda;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardSaldoFragment extends Fragment {

    private DashboardViewModel viewModel;
    private ServiceRenda serviceRenda;
    private ContaRepository contaRepository;

    private TextView txtMesAnoSelecionado, txtSaldoDetalhe, txtTotalEntradas, txtTotalSaidas;
    private TextView txtStatusEmoji, txtStatusTitulo, txtMensagemConselho, txtTaxaPoupancaPill;
    private PieChart pieChartSaldo;

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoMesAno = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR"));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_saldo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        serviceRenda = new ServiceRenda(requireContext());
        contaRepository = new ContaRepository(requireContext());

        txtMesAnoSelecionado = view.findViewById(R.id.txtMesAnoSelecionado);
        txtSaldoDetalhe = view.findViewById(R.id.txtSaldoDetalhe);
        txtTotalEntradas = view.findViewById(R.id.txtTotalEntradas);
        txtTotalSaidas = view.findViewById(R.id.txtTotalSaidas);

        txtStatusEmoji = view.findViewById(R.id.txtStatusEmoji);
        txtStatusTitulo = view.findViewById(R.id.txtStatusTitulo);
        txtMensagemConselho = view.findViewById(R.id.txtMensagemConselho);
        txtTaxaPoupancaPill = view.findViewById(R.id.txtTaxaPoupancaPill);
        pieChartSaldo = view.findViewById(R.id.pieChartSaldo);

        View btnAnterior = view.findViewById(R.id.btnMesAnterior);
        if (btnAnterior != null) btnAnterior.setOnClickListener(v -> alterarMes(-1));

        View btnProximo = view.findViewById(R.id.btnMesProximo);
        if (btnProximo != null) btnProximo.setOnClickListener(v -> alterarMes(1));

        configurarGrafico();
        carregarDadosSaldo();
    }

    private void configurarGrafico() {
        if (pieChartSaldo == null) return;
        pieChartSaldo.setUsePercentValues(true);
        pieChartSaldo.getDescription().setEnabled(false);
        pieChartSaldo.setDrawHoleEnabled(true);
        pieChartSaldo.setHoleRadius(52f);
        pieChartSaldo.setTransparentCircleRadius(58f);
        pieChartSaldo.setRotationEnabled(true); // Permite girar com o dedo perfeitamente
        pieChartSaldo.setHighlightPerTapEnabled(true);

        // Removemos a legenda lateral poluída para focar no design limpo e nos valores diretos
        pieChartSaldo.getLegend().setEnabled(false);

        // Animação fluida ao carregar a tela
        pieChartSaldo.animateY(1000);
    }

    private void alterarMes(int meses) {
        viewModel.setMesSelecionado(viewModel.getMesSelecionado().plusMonths(meses));
        carregarDadosSaldo();
    }

    private void carregarDadosSaldo() {
        YearMonth mesAtual = viewModel.getMesSelecionado();
        if (txtMesAnoSelecionado != null) {
            txtMesAnoSelecionado.setText(capitalizar(mesAtual.format(formatoMesAno)));
        }

        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;

        List<Renda> rendas = serviceRenda.listarRendas();
        if (rendas != null) {
            for (Renda r : rendas) {
                if (r.getValor() != null && r.getData() != null) {
                    YearMonth mesRenda = null;
                    Object dataObj = r.getData();
                    if (dataObj instanceof YearMonth) {
                        mesRenda = (YearMonth) dataObj;
                    } else if (dataObj instanceof LocalDate) {
                        mesRenda = YearMonth.from((LocalDate) dataObj);
                    } else if (dataObj instanceof String) {
                        try { mesRenda = YearMonth.parse((String) dataObj); } catch (Exception ignored) {}
                    }

                    if (mesRenda != null && mesRenda.equals(mesAtual)) {
                        totalEntradas = totalEntradas.add(r.getValor());
                    }
                }
            }
        }

        List<Conta> contas = contaRepository.listarTodas();
        if (contas != null) {
            for (Conta c : contas) {
                if (c.getValor() != null && c.getDataDeLancamento() != null) {
                    YearMonth mesConta = YearMonth.from(c.getDataDeLancamento());
                    if (mesConta.equals(mesAtual)) {
                        totalSaidas = totalSaidas.add(c.getValor());
                    }
                }
            }
        }

        BigDecimal saldoMes = totalEntradas.subtract(totalSaidas);

        if (txtSaldoDetalhe != null) txtSaldoDetalhe.setText(formatoMoeda.format(saldoMes));
        if (txtTotalEntradas != null) txtTotalEntradas.setText(formatoMoeda.format(totalEntradas));
        if (txtTotalSaidas != null) txtTotalSaidas.setText(formatoMoeda.format(totalSaidas));

        atualizarGrafico(totalEntradas, totalSaidas, saldoMes);
        atualizarDiagnosticoSaude(totalEntradas, totalSaidas, saldoMes);
    }

    private void atualizarGrafico(BigDecimal entradas, BigDecimal saidas, BigDecimal saldo) {
        if (pieChartSaldo == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> cores = new ArrayList<>();

        if (saidas.compareTo(BigDecimal.ZERO) > 0) {
            entries.add(new PieEntry(saidas.floatValue(), "Saiu (Gastos)"));
            cores.add(Color.parseColor("#F43F5E")); // Vermelho moderno
        }

        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            entries.add(new PieEntry(saldo.floatValue(), "Guardado (Saldo)"));
            cores.add(Color.parseColor("#10B981")); // Verde esmeralda
        } else if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            entries.add(new PieEntry(saldo.abs().floatValue(), "Déficit Negativo"));
            cores.add(Color.parseColor("#F59E0B")); // Laranja alerta
        }

        if (entries.isEmpty()) {
            pieChartSaldo.clear();
            pieChartSaldo.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(6f);
        dataSet.setColors(cores);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartSaldo));
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieChartSaldo.setData(data);
        pieChartSaldo.invalidate();
    }

    private void atualizarDiagnosticoSaude(BigDecimal entradas, BigDecimal saidas, BigDecimal saldo) {
        if (txtStatusTitulo == null || txtMensagemConselho == null) return;

        if (entradas.compareTo(BigDecimal.ZERO) == 0 && saidas.compareTo(BigDecimal.ZERO) == 0) {
            txtStatusEmoji.setText("💤");
            txtStatusTitulo.setText("Mês sem registros");
            txtMensagemConselho.setText("Você ainda não lançou nenhuma entrada ou saída neste mês. Cadastre suas movimentações para ver a auditoria inteligente.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("Sem dados");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#64748B"));
                }
            }
            return;
        }

        BigDecimal taxaPoupanca = BigDecimal.ZERO;
        if (entradas.compareTo(BigDecimal.ZERO) > 0) {
            taxaPoupanca = saldo.divide(entradas, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            txtStatusEmoji.setText("🚨");
            txtStatusTitulo.setText("Situação Alarmante");
            txtMensagemConselho.setText("Ei, cuidado com os gastos! O total que saiu superou o total que entrou em " + formatoMoeda.format(saidas.subtract(entradas)) + ". Você entrou no vermelho.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("Déficit");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#EF4444"));
                }
            }
        } else if (taxaPoupanca.compareTo(BigDecimal.valueOf(20)) >= 0) {
            txtStatusEmoji.setText("🏆");
            txtStatusTitulo.setText("Situação Positiva");
            txtMensagemConselho.setText("Excelente controle! Você guardou " + formatoMoeda.format(saldo) + " do total que entrou neste mês. Sua taxa de economia está em " + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "%.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("+" + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "% guardado");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#10B981"));
                }
            }
        } else {
            txtStatusEmoji.setText("⚠️");
            txtStatusTitulo.setText("Situação Preocupante");
            txtMensagemConselho.setText("Atenção! Sobrou pouco do total que entrou (" + formatoMoeda.format(saldo) + "). Cuidado para não estourar o orçamento nos próximos dias.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("+" + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "% guardado");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#F59E0B"));
                }
            }
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + texto.substring(1);
    }
}