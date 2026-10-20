package com.example.planwin.ui.dashboard;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.repository.ContaRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardDespesasFragment extends Fragment {

    private ContaRepository contaRepository;

    private TextView txtMesAnoSelecionadoDespesa, txtAbaDespesaTotal;
    private PieChart pieChartDespesas;
    private LinearLayout containerListaDespesasMes;

    private YearMonth mesSelecionado = YearMonth.now();

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoMesAno = DateTimeFormatter.ofPattern("MMM. yyyy", new Locale("pt", "BR"));

    // Cores modernas para categorias de despesas (Vermelhos, Laranjas, Rosas, Roxos)
    private final int[] CORES_DESPESAS = new int[]{
            Color.parseColor("#EF4444"), // Vermelho
            Color.parseColor("#F97316"), // Laranja
            Color.parseColor("#EC4899"), // Rosa
            Color.parseColor("#8B5CF6")  // Roxo
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_despesas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contaRepository = new ContaRepository(requireContext());

        txtMesAnoSelecionadoDespesa = view.findViewById(R.id.txtMesAnoSelecionadoDespesa);
        txtAbaDespesaTotal = view.findViewById(R.id.txtAbaDespesaTotal);
        pieChartDespesas = view.findViewById(R.id.pieChartDespesas);
        containerListaDespesasMes = view.findViewById(R.id.containerListaDespesasMes);

        view.findViewById(R.id.btnMesAnteriorDespesa).setOnClickListener(v -> alterarMes(-1));
        view.findViewById(R.id.btnMesProximoDespesa).setOnClickListener(v -> alterarMes(1));

        // Permite alternar de volta para o Dashboard de Receitas ao tocar na aba superior
        view.findViewById(R.id.tabIrParaReceitas).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new DashboardReceitasFragment())
                    .commit();
        });

        configurarGrafico();
        carregarDashboardDespesas();
    }

    private void configurarGrafico() {
        pieChartDespesas.setUsePercentValues(true);
        pieChartDespesas.getDescription().setEnabled(false);
        pieChartDespesas.setDrawHoleEnabled(false); // Gráfico sólido idêntico ao modelo
        pieChartDespesas.setRotationEnabled(true);
        pieChartDespesas.setHighlightPerTapEnabled(true);
        pieChartDespesas.getLegend().setEnabled(false);
    }

    private void alterarMes(int meses) {
        mesSelecionado = mesSelecionado.plusMonths(meses);
        carregarDashboardDespesas();
    }

    private void carregarDashboardDespesas() {
        if (txtMesAnoSelecionadoDespesa != null) {
            txtMesAnoSelecionadoDespesa.setText(capitalizar(mesSelecionado.format(formatoMesAno)));
        }
        if (containerListaDespesasMes != null) {
            containerListaDespesasMes.removeAllViews();
        }

        BigDecimal totalDespesasMes = BigDecimal.ZERO;
        Map<String, BigDecimal> somaPorCategoria = new HashMap<>();

        List<Conta> contas = contaRepository.listarTodas();
        if (contas != null) {
            for (Conta conta : contas) {
                if (conta.getValor() != null && conta.getDataDeLancamento() != null) {
                    YearMonth mesConta = YearMonth.from(conta.getDataDeLancamento());
                    if (mesConta.equals(mesSelecionado)) {
                        BigDecimal valor = conta.getValor();
                        totalDespesasMes = totalDespesasMes.add(valor);

                        String categoria = conta.getCategoria() != null ? conta.getCategoria() : "Despesas Gerais";
                        BigDecimal totalCat = somaPorCategoria.getOrDefault(categoria, BigDecimal.ZERO);
                        somaPorCategoria.put(categoria, totalCat.add(valor));
                    }
                }
            }
        }

        if (txtAbaDespesaTotal != null) {
            txtAbaDespesaTotal.setText("Despesa: " + formatoMoeda.format(totalDespesasMes));
        }

        atualizarGraficoEListaDespesas(somaPorCategoria, totalDespesasMes);
    }

    private void atualizarGraficoEListaDespesas(Map<String, BigDecimal> somaPorCategoria, BigDecimal totalGeral) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        List<CategoriaResumoDespesa> listaResumo = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, BigDecimal> entry : somaPorCategoria.entrySet()) {
            String categoria = entry.getKey();
            BigDecimal valor = entry.getValue();
            float valorFloat = valor.floatValue();

            if (valorFloat > 0) {
                entries.add(new PieEntry(valorFloat, categoria));

                float porcentagem = 0f;
                if (totalGeral.compareTo(BigDecimal.ZERO) > 0) {
                    porcentagem = valor.divide(totalGeral, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).floatValue();
                }

                int cor = CORES_DESPESAS[index % CORES_DESPESAS.length];
                listaResumo.add(new CategoriaResumoDespesa(categoria, valor, porcentagem, cor));
                index++;
            }
        }

        if (entries.isEmpty()) {
            pieChartDespesas.clear();
            pieChartDespesas.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(4f);
        dataSet.setColors(CORES_DESPESAS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartDespesas));
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieChartDespesas.setData(data);
        pieChartDespesas.invalidate();

        for (CategoriaResumoDespesa item : listaResumo) {
            containerListaDespesasMes.addView(criarItemListaDespesa(item));
        }
    }

    private View criarItemListaDespesa(CategoriaResumoDespesa item) {
        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dashboard_categoria, containerListaDespesasMes, false);

        TextView txtPorcentagemPill = itemView.findViewById(R.id.txtPorcentagemPill);
        TextView txtNomeCategoria = itemView.findViewById(R.id.txtNomeCategoria);
        TextView txtValorCategoria = itemView.findViewById(R.id.txtValorCategoria);

        txtPorcentagemPill.setText(Math.round(item.porcentagem) + "%");
        if (txtPorcentagemPill.getBackground() != null) {
            txtPorcentagemPill.getBackground().setTint(item.cor);
        }

        txtNomeCategoria.setText(getEmojiDespesa(item.nome) + "  " + item.nome);
        txtValorCategoria.setText(formatoMoeda.format(item.valor));

        return itemView;
    }

    private String getEmojiDespesa(String categoria) {
        if (categoria == null) return "💸";
        switch (categoria) {
            case "Alimentação": return "🍔";
            case "Moradia": return "🏠";
            case "Transporte": return "🚗";
            case "Lazer": return "🎉";
            case "Saúde": return "💊";
            case "Educação": return "📚";
            default: return "💸";
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + texto.substring(1);
    }

    private static class CategoriaResumoDespesa {
        String nome;
        BigDecimal valor;
        float porcentagem;
        int cor;

        public CategoriaResumoDespesa(String nome, BigDecimal valor, float porcentagem, int cor) {
            this.nome = nome;
            this.valor = valor;
            this.porcentagem = porcentagem;
            this.cor = cor;
        }
    }
}