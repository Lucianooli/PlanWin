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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardReceitasFragment extends Fragment {

    private DashboardViewModel viewModel;
    private ServiceRenda serviceRenda;
    private ContaRepository contaRepository;

    private TextView txtMesAnoSelecionado, txtAbaReceitaValor, txtAbaDespesaValor;
    private TextView txtStatusEmoji, txtStatusTitulo, txtMensagemConselho, txtTaxaPoupancaPill;
    private View cardSaudeFinanceira, tabAbaReceita, tabAbaDespesa;
    private LinearLayout containerListaItensMes;
    private PieChart pieChartCategorias;

    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoMesAno = DateTimeFormatter.ofPattern("MMM. yyyy", new Locale("pt", "BR"));

    private final int[] CORES_RECEITAS = new int[]{
            Color.parseColor("#EF4444"), Color.parseColor("#F97316"),
            Color.parseColor("#FACC15"), Color.parseColor("#3B82F6")
    };

    private final int[] CORES_DESPESAS = new int[]{
            Color.parseColor("#8B5CF6"), Color.parseColor("#EC4899"),
            Color.parseColor("#3B82F6"), Color.parseColor("#10B981")
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_receitas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        if (getArguments() != null && getArguments().containsKey("abrir_em_despesas")) {
            boolean abrirEmDespesas = getArguments().getBoolean("abrir_em_despesas", false);
            viewModel.setVisualizandoReceitas(!abrirEmDespesas);
        }

        serviceRenda = new ServiceRenda(requireContext());
        contaRepository = new ContaRepository(requireContext());

        txtMesAnoSelecionado = view.findViewById(R.id.txtMesAnoSelecionado);
        txtAbaReceitaValor = view.findViewById(R.id.txtAbaReceitaValor);
        txtAbaDespesaValor = view.findViewById(R.id.txtAbaDespesaValor);

        // Componentes do Card de Saúde Financeira
        txtStatusEmoji = view.findViewById(R.id.txtStatusEmoji);
        txtStatusTitulo = view.findViewById(R.id.txtStatusTitulo);
        txtMensagemConselho = view.findViewById(R.id.txtMensagemConselho);
        txtTaxaPoupancaPill = view.findViewById(R.id.txtTaxaPoupancaPill);
        cardSaudeFinanceira = view.findViewById(R.id.cardSaudeFinanceira);

        containerListaItensMes = view.findViewById(R.id.containerListaItensMes);
        pieChartCategorias = view.findViewById(R.id.pieChartCategorias);
        tabAbaReceita = view.findViewById(R.id.tabAbaReceita);
        tabAbaDespesa = view.findViewById(R.id.tabAbaDespesa);

        View btnAnterior = view.findViewById(R.id.btnMesAnterior);
        if (btnAnterior != null) btnAnterior.setOnClickListener(v -> alterarMes(-1));

        View btnProximo = view.findViewById(R.id.btnMesProximo);
        if (btnProximo != null) btnProximo.setOnClickListener(v -> alterarMes(1));

        if (tabAbaReceita != null) {
            tabAbaReceita.setOnClickListener(v -> {
                viewModel.setVisualizandoReceitas(true);
                atualizarAbasUI();
                carregarDados();
            });
        }

        if (tabAbaDespesa != null) {
            tabAbaDespesa.setOnClickListener(v -> {
                viewModel.setVisualizandoReceitas(false);
                atualizarAbasUI();
                carregarDados();
            });
        }

        configurarGrafico();
        atualizarAbasUI();
        carregarDados();
    }

    private void configurarGrafico() {
        if (pieChartCategorias == null) return;
        pieChartCategorias.setUsePercentValues(true);
        pieChartCategorias.getDescription().setEnabled(false);
        pieChartCategorias.setDrawHoleEnabled(false);
        pieChartCategorias.setRotationEnabled(true);
        pieChartCategorias.setHighlightPerTapEnabled(true);
        pieChartCategorias.getLegend().setEnabled(false);
    }

    private void alterarMes(int meses) {
        viewModel.setMesSelecionado(viewModel.getMesSelecionado().plusMonths(meses));
        carregarDados();
    }

    private void atualizarAbasUI() {
        if (txtAbaReceitaValor != null && txtAbaDespesaValor != null) {
            if (viewModel.isVisualizandoReceitas()) {
                txtAbaReceitaValor.setTypeface(null, Typeface.BOLD);
                txtAbaReceitaValor.setTextColor(Color.parseColor("#2E9D65")); // Verde
                txtAbaDespesaValor.setTypeface(null, Typeface.NORMAL);
                txtAbaDespesaValor.setTextColor(Color.parseColor("#64748B"));
            } else {
                txtAbaDespesaValor.setTypeface(null, Typeface.BOLD);
                txtAbaDespesaValor.setTextColor(Color.parseColor("#EF4444")); // Vermelho
                txtAbaReceitaValor.setTypeface(null, Typeface.NORMAL);
                txtAbaReceitaValor.setTextColor(Color.parseColor("#64748B"));
            }
        }
    }

    private void carregarDados() {
        YearMonth mesAtual = viewModel.getMesSelecionado();
        if (txtMesAnoSelecionado != null) {
            txtMesAnoSelecionado.setText(capitalizar(mesAtual.format(formatoMesAno)));
        }
        if (containerListaItensMes != null) {
            containerListaItensMes.removeAllViews();
        }

        BigDecimal totalReceitasMes = BigDecimal.ZERO;
        BigDecimal totalDespesasMes = BigDecimal.ZERO;
        Map<String, BigDecimal> somaPorCategoria = new HashMap<>();

        // 1. Calcular Rendas
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
                        BigDecimal val = r.getValor();
                        totalReceitasMes = totalReceitasMes.add(val);
                        if (viewModel.isVisualizandoReceitas()) {
                            String cat = r.getCategoria() != null ? r.getCategoria() : "Outro";
                            somaPorCategoria.put(cat, somaPorCategoria.getOrDefault(cat, BigDecimal.ZERO).add(val));
                        }
                    }
                }
            }
        }

        // 2. Calcular Despesas
        List<Conta> contas = contaRepository.listarTodas();
        if (contas != null) {
            for (Conta c : contas) {
                if (c.getValor() != null && c.getDataDeLancamento() != null) {
                    YearMonth mesConta = YearMonth.from(c.getDataDeLancamento());
                    if (mesConta.equals(mesAtual)) {
                        BigDecimal val = c.getValor();
                        totalDespesasMes = totalDespesasMes.add(val);
                        if (!viewModel.isVisualizandoReceitas()) {
                            String cat = c.getCategoria() != null ? c.getCategoria() : "Despesas Gerais";
                            somaPorCategoria.put(cat, somaPorCategoria.getOrDefault(cat, BigDecimal.ZERO).add(val));
                        }
                    }
                }
            }
        }

        if (txtAbaReceitaValor != null) {
            txtAbaReceitaValor.setText("Receita: " + formatoMoeda.format(totalReceitasMes));
        }
        if (txtAbaDespesaValor != null) {
            txtAbaDespesaValor.setText("Despesa: " + formatoMoeda.format(totalDespesasMes));
        }

        // --- APLICANDO A INTELIGÊNCIA DE SAÚDE FINANCEIRA ---
        atualizarSaudeFinanceira(totalReceitasMes, totalDespesasMes);

        BigDecimal totalGeralAtivo = viewModel.isVisualizandoReceitas() ? totalReceitasMes : totalDespesasMes;
        atualizarGraficoELista(somaPorCategoria, totalGeralAtivo);
    }

    private void atualizarSaudeFinanceira(BigDecimal receitas, BigDecimal despesas) {
        if (txtStatusTitulo == null || txtMensagemConselho == null) return;

        BigDecimal saldo = receitas.subtract(despesas);

        if (receitas.compareTo(BigDecimal.ZERO) == 0 && despesas.compareTo(BigDecimal.ZERO) == 0) {
            txtStatusEmoji.setText("💤");
            txtStatusTitulo.setText("Mês sem movimentações");
            txtMensagemConselho.setText("Nenhuma receita ou despesa registrada ainda. Lance seus ganhos e gastos para ver a análise completa.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("Sem dados");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#64748B"));
                }
            }
            return;
        }

        BigDecimal taxaPoupanca = BigDecimal.ZERO;
        if (receitas.compareTo(BigDecimal.ZERO) > 0) {
            taxaPoupanca = saldo.divide(receitas, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            txtStatusEmoji.setText("🚨");
            txtStatusTitulo.setText("Situação Alarmante");
            txtMensagemConselho.setText("Ei, cuidado com os gastos! Você gastou " + formatoMoeda.format(despesas.subtract(receitas)) + " a mais do que entrou. Hora de frear o cartão urgente!");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("Déficit");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#EF4444"));
                }
            }
        } else if (taxaPoupanca.compareTo(BigDecimal.valueOf(20)) >= 0) {
            txtStatusEmoji.setText("🏆");
            txtStatusTitulo.setText("Situação Positiva");
            txtMensagemConselho.setText("Excelente controle! Sobraram " + formatoMoeda.format(saldo) + " neste mês. Sua taxa de economia está em " + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "%. Continue assim!");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("+" + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "% guardado");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#2E9D65"));
                }
            }
        } else if (taxaPoupanca.compareTo(BigDecimal.valueOf(5)) >= 0) {
            txtStatusEmoji.setText("⚠️");
            txtStatusTitulo.setText("Situação Preocupante");
            txtMensagemConselho.setText("Cuidado! Sobraram apenas " + formatoMoeda.format(saldo) + ". Margem muito apertada para imprevistos. Evite compras supérfluas.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("+" + taxaPoupanca.setScale(0, RoundingMode.HALF_UP) + "% guardado");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#D99522"));
                }
            }
        } else {
            txtStatusEmoji.setText("👀");
            txtStatusTitulo.setText("Atenção aos Gastos");
            txtMensagemConselho.setText("Você está no limite do orçamento! Entrou " + formatoMoeda.format(receitas) + " e quase tudo foi consumido. Fique de olho.");
            if (txtTaxaPoupancaPill != null) {
                txtTaxaPoupancaPill.setText("Margem baixa");
                if (txtTaxaPoupancaPill.getBackground() != null) {
                    txtTaxaPoupancaPill.getBackground().setTint(Color.parseColor("#F97316"));
                }
            }
        }
    }

    private void atualizarGraficoELista(Map<String, BigDecimal> somaPorCategoria, BigDecimal totalGeral) {
        if (pieChartCategorias == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        List<ResumoItem> listaResumo = new ArrayList<>();
        int[] coresAtuais = viewModel.isVisualizandoReceitas() ? CORES_RECEITAS : CORES_DESPESAS;

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

                int cor = coresAtuais[index % coresAtuais.length];
                listaResumo.add(new ResumoItem(categoria, valor, porcentagem, cor));
                index++;
            }
        }

        if (entries.isEmpty()) {
            pieChartCategorias.clear();
            pieChartCategorias.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(4f);
        dataSet.setColors(coresAtuais);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChartCategorias));
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieChartCategorias.setData(data);
        pieChartCategorias.invalidate();

        if (containerListaItensMes != null) {
            for (ResumoItem item : listaResumo) {
                containerListaItensMes.addView(criarItemLista(item));
            }
        }
    }

    private View criarItemLista(ResumoItem item) {
        View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dashboard_categoria, containerListaItensMes, false);

        TextView txtPorcentagemPill = itemView.findViewById(R.id.txtPorcentagemPill);
        TextView txtNomeCategoria = itemView.findViewById(R.id.txtNomeCategoria);
        TextView txtValorCategoria = itemView.findViewById(R.id.txtValorCategoria);

        txtPorcentagemPill.setText(Math.round(item.porcentagem) + "%");
        if (txtPorcentagemPill.getBackground() != null) {
            txtPorcentagemPill.getBackground().setTint(item.cor);
        }

        txtNomeCategoria.setText(getEmoji(item.nome) + "  " + item.nome);
        txtValorCategoria.setText(formatoMoeda.format(item.valor));

        return itemView;
    }

    private String getEmoji(String cat) {
        if (cat == null) return "📌";
        switch (cat) {
            case "Salário": return "💼";
            case "Extra": return "💰";
            case "Bônus": return "🎁";
            case "Investimento": return "📈";
            case "Alimentação": return "🍔";
            case "Moradia": return "🏠";
            case "Transporte": return "🚗";
            case "Lazer": return "🎉";
            default: return "📌";
        }
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + texto.substring(1);
    }

    private static class ResumoItem {
        String nome;
        BigDecimal valor;
        float porcentagem;
        int cor;

        public ResumoItem(String nome, BigDecimal valor, float porcentagem, int cor) {
            this.nome = nome;
            this.valor = valor;
            this.porcentagem = porcentagem;
            this.cor = cor;
        }
    }
}