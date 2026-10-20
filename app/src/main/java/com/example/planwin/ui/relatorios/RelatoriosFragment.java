package com.example.planwin.ui.relatorios;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.entity.Renda;
import com.example.planwin.enums.StatusPagamento;
import com.example.planwin.repository.ContaRepository;
import com.example.planwin.repository.RendaRepository;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class RelatoriosFragment extends Fragment {

    private TextView textPeriodoRelatorio;
    private TextView textRelatorioReceitas;
    private TextView textRelatorioDespesas;
    private TextView textRelatorioSaldo;
    private TextView textRelatorioPagas;
    private TextView textRelatorioPendentes;
    private TextView textRelatorioAtrasadas;
    private TextView textHistoricoVazio;

    private LinearLayout containerHistorico;

    private TextView btnMesAnterior;
    private TextView btnMesSeguinte;

    private YearMonth mesSelecionado;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(
                    new Locale("pt", "BR")
            );

    private final DateTimeFormatter formatoMes =
            DateTimeFormatter.ofPattern(
                    "MMMM 'de' yyyy",
                    new Locale("pt", "BR")
            );

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RelatoriosFragment() {
        super(R.layout.fragment_relatorios);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        vincularComponentes(view);

        if (savedInstanceState != null
                && savedInstanceState.containsKey("mesSelecionado")) {

            mesSelecionado = YearMonth.parse(
                    savedInstanceState.getString("mesSelecionado")
            );

        } else {
            mesSelecionado = YearMonth.now();
        }

        btnMesAnterior.setOnClickListener(v -> {
            mesSelecionado = mesSelecionado.minusMonths(1);
            carregarRelatorio();
        });

        btnMesSeguinte.setOnClickListener(v -> {
            if (mesSelecionado.isBefore(YearMonth.now())) {
                mesSelecionado = mesSelecionado.plusMonths(1);
                carregarRelatorio();
            }
        });

        carregarRelatorio();
    }

    @Override
    public void onSaveInstanceState(
            @NonNull Bundle outState
    ) {
        super.onSaveInstanceState(outState);

        if (mesSelecionado != null) {
            outState.putString(
                    "mesSelecionado",
                    mesSelecionado.toString()
            );
        }
    }

    private void vincularComponentes(View view) {
        textPeriodoRelatorio =
                view.findViewById(R.id.textPeriodoRelatorio);

        textRelatorioReceitas =
                view.findViewById(R.id.textRelatorioReceitas);

        textRelatorioDespesas =
                view.findViewById(R.id.textRelatorioDespesas);

        textRelatorioSaldo =
                view.findViewById(R.id.textRelatorioSaldo);

        textRelatorioPagas =
                view.findViewById(R.id.textRelatorioPagas);

        textRelatorioPendentes =
                view.findViewById(R.id.textRelatorioPendentes);

        textRelatorioAtrasadas =
                view.findViewById(R.id.textRelatorioAtrasadas);

        textHistoricoVazio =
                view.findViewById(R.id.textHistoricoVazio);

        containerHistorico =
                view.findViewById(R.id.containerHistorico);

        btnMesAnterior =
                view.findViewById(R.id.btnMesAnterior);

        btnMesSeguinte =
                view.findViewById(R.id.btnMesSeguinte);
    }

    private void carregarRelatorio() {
        if (!isAdded() || getView() == null) {
            return;
        }

        textPeriodoRelatorio.setText(
                primeiraLetraMaiuscula(
                        mesSelecionado.format(formatoMes)
                )
        );

        btnMesSeguinte.setEnabled(
                mesSelecionado.isBefore(YearMonth.now())
        );

        ContaRepository contaRepository =
                new ContaRepository(requireContext());

        RendaRepository rendaRepository =
                new RendaRepository(requireContext());

        try {
            List<Conta> contas = contaRepository.listarTodas();
            List<Renda> rendas = rendaRepository.listarTodas();

            BigDecimal totalReceitas = BigDecimal.ZERO;
            BigDecimal totalDespesas = BigDecimal.ZERO;

            BigDecimal totalPagas = BigDecimal.ZERO;
            BigDecimal totalPendentes = BigDecimal.ZERO;
            BigDecimal totalAtrasadas = BigDecimal.ZERO;

            containerHistorico.removeAllViews();

            boolean possuiMovimentacoes = false;

            // Calcula as receitas aplicáveis ao mês selecionado.
            for (Renda renda : rendas) {
                if (renda.getData() == null
                        || renda.getValor() == null) {
                    continue;
                }

                boolean rendaFixa =
                        "Fixa".equalsIgnoreCase(renda.getTipo());

                boolean rendaDoMes =
                        "Mes".equalsIgnoreCase(renda.getTipo());

                boolean pertenceAoMes =
                        (rendaFixa
                                && !renda.getData().isAfter(mesSelecionado))
                                || (rendaDoMes
                                && renda.getData().equals(mesSelecionado));

                if (!pertenceAoMes) {
                    continue;
                }

                totalReceitas =
                        totalReceitas.add(renda.getValor());

                adicionarMovimentacao(
                        "Receita: " + renda.getNome(),
                        renda.getValor(),
                        "Receita"
                );

                possuiMovimentacoes = true;
            }

            // Calcula as despesas aplicáveis ao mês selecionado.
            for (Conta conta : contas) {
                if (conta.getValor() == null
                        || conta.getDataDeLancamento() == null) {
                    continue;
                }

                YearMonth mesLancamento =
                        YearMonth.from(conta.getDataDeLancamento());

                boolean contaFixa =
                        "Fixa".equalsIgnoreCase(conta.getTipo());

                boolean contaDoMes =
                        "Mes".equalsIgnoreCase(conta.getTipo());

                boolean pertenceAoMes =
                        (contaFixa
                                && !mesLancamento.isAfter(mesSelecionado))
                                || (contaDoMes
                                && mesLancamento.equals(mesSelecionado));

                if (!pertenceAoMes) {
                    continue;
                }

                totalDespesas =
                        totalDespesas.add(conta.getValor());

                StatusPagamento status =
                        conta.getStatusPagamentoEfetivo();

                if (status == StatusPagamento.PAGA) {
                    totalPagas = totalPagas.add(conta.getValor());
                } else if (status == StatusPagamento.ATRASADA) {
                    totalAtrasadas = totalAtrasadas.add(conta.getValor());
                } else {
                    totalPendentes = totalPendentes.add(conta.getValor());
                }

                LocalDate dataReferencia =
                        conta.getDataDeVencimento();

                String dataTexto;

                if (dataReferencia != null) {
                    dataTexto = dataReferencia.format(formatoData);
                } else {
                    dataTexto =
                            conta.getDataDeLancamento()
                                    .format(formatoData);
                }

                adicionarMovimentacao(
                        "Despesa: " + conta.getNome()
                                + " (" + dataTexto + ")",
                        conta.getValor(),
                        "Despesa"
                );

                possuiMovimentacoes = true;
            }

            BigDecimal saldo =
                    totalReceitas.subtract(totalDespesas);

            textRelatorioReceitas.setText(
                    formatarMoeda(totalReceitas)
            );

            textRelatorioDespesas.setText(
                    formatarMoeda(totalDespesas)
            );

            textRelatorioSaldo.setText(
                    formatarMoeda(saldo)
            );

            textRelatorioPagas.setText(
                    formatarMoeda(totalPagas)
            );

            textRelatorioPendentes.setText(
                    formatarMoeda(totalPendentes)
            );

            textRelatorioAtrasadas.setText(
                    formatarMoeda(totalAtrasadas)
            );

            textHistoricoVazio.setVisibility(
                    possuiMovimentacoes
                            ? View.GONE
                            : View.VISIBLE
            );

        } finally {
            contaRepository.fechar();
            rendaRepository.fechar();
        }
    }

    private void adicionarMovimentacao(
            String titulo,
            BigDecimal valor,
            String tipo
    ) {
        // Cria um CardView moderno para cada item do histórico, dando aspecto elegante
        CardView cardItem = new CardView(requireContext());
        cardItem.setRadius(14f);
        cardItem.setCardElevation(0f);
        cardItem.setCardBackgroundColor(Color.parseColor("#121212")); // Fundo adaptável ou neutro

        LinearLayout layoutItem = new LinearLayout(requireContext());
        layoutItem.setOrientation(LinearLayout.HORIZONTAL);
        layoutItem.setPadding(16, 16, 16, 16);
        layoutItem.setGravity(Gravity.CENTER_VERTICAL);

        TextView txtTitulo = new TextView(requireContext());
        txtTitulo.setText(titulo);
        txtTitulo.setTextSize(14);
        txtTitulo.setTypeface(null, Typeface.BOLD);
        txtTitulo.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams paramsTitulo = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
        );
        txtTitulo.setLayoutParams(paramsTitulo);

        TextView txtValor = new TextView(requireContext());
        boolean isReceita = tipo.equals("Receita");
        String sinal = isReceita ? "+ " : "- ";
        txtValor.setText(sinal + formatarMoeda(valor));
        txtValor.setTextSize(14);
        txtValor.setTypeface(null, Typeface.BOLD);
        txtValor.setTextColor(isReceita ? Color.parseColor("#10B981") : Color.parseColor("#F43F5E"));

        layoutItem.addView(txtTitulo);
        layoutItem.addView(txtValor);
        cardItem.addView(layoutItem);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        parametros.bottomMargin = 8;
        containerHistorico.addView(cardItem, parametros);
    }

    private String formatarMoeda(BigDecimal valor) {
        return formatoMoeda.format(valor);
    }

    private String primeiraLetraMaiuscula(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        return texto.substring(0, 1)
                .toUpperCase(new Locale("pt", "BR"))
                + texto.substring(1);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (textPeriodoRelatorio != null) {
            carregarRelatorio();
        }
    }
}