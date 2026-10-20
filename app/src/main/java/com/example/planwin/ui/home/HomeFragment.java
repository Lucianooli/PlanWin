package com.example.planwin.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.entity.Renda;
import com.example.planwin.enums.StatusPagamento;
import com.example.planwin.repository.ContaRepository;
import com.example.planwin.repository.RendaRepository;
import com.example.planwin.ui.dashboard.DashboardReceitasFragment;
import com.example.planwin.ui.dashboard.DashboardSaldoFragment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView textMesAtual;
    private TextView textSaldoMes;
    private TextView textTotalRendas;
    private TextView textTotalDespesas;

    private TextView textTotalPendentes;
    private TextView textQuantidadePendentes;

    private TextView textTotalAtrasadas;
    private TextView textQuantidadeAtrasadas;

    private TextView textTotalPagas;
    private TextView textQuantidadePagas;

    private ContaRepository contaRepository;
    private RendaRepository rendaRepository;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final DateTimeFormatter formatoMes =
            DateTimeFormatter.ofPattern(
                    "MMMM 'de' yyyy",
                    new Locale("pt", "BR")
            );

    public HomeFragment() {
        // Construtor vazio necessário para o FragmentManager.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        // Componentes gerais.
        textMesAtual = view.findViewById(R.id.textMesAtual);
        textSaldoMes = view.findViewById(R.id.textSaldoMes);
        textTotalRendas = view.findViewById(R.id.textTotalRendas);
        textTotalDespesas = view.findViewById(R.id.textTotalDespesas);

        // Cartão de contas pendentes.
        textTotalPendentes = view.findViewById(R.id.textTotalPendentes);
        textQuantidadePendentes = view.findViewById(R.id.textQuantidadePendentes);

        // Cartão de contas atrasadas.
        textTotalAtrasadas = view.findViewById(R.id.textTotalAtrasadas);
        textQuantidadeAtrasadas = view.findViewById(R.id.textQuantidadeAtrasadas);

        // Cartão de contas pagas.
        textTotalPagas = view.findViewById(R.id.textTotalPagas);
        textQuantidadePagas = view.findViewById(R.id.textQuantidadePagas);

        contaRepository = new ContaRepository(requireContext());
        rendaRepository = new RendaRepository(requireContext());

        // Evento de clique no Cartão de Saldo do Mês para abrir o relatório detalhado de saldo
        View cardSaldo = view.findViewById(R.id.cardSaldoDoMes);
        if (cardSaldo != null) {
            cardSaldo.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new DashboardSaldoFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Evento de clique no cartão de Receitas para abrir o Dashboard na aba de Receitas
        view.findViewById(R.id.cardReceitas).setOnClickListener(v -> {
            DashboardReceitasFragment fragment = new DashboardReceitasFragment();
            Bundle args = new Bundle();
            args.putBoolean("abrir_em_despesas", false);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Evento de clique no cartão de Despesas para abrir o Dashboard direto na aba de Despesas
        view.findViewById(R.id.cardDespesas).setOnClickListener(v -> {
            DashboardReceitasFragment fragment = new DashboardReceitasFragment();
            Bundle args = new Bundle();
            args.putBoolean("abrir_em_despesas", true);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Eventos de clique nos demais cartões
        view.findViewById(R.id.cardPendentes).setOnClickListener(v ->
                abrirDetalhesContas(StatusPagamento.PENDENTE, "Contas Pendentes")
        );

        view.findViewById(R.id.cardAtrasadas).setOnClickListener(v ->
                abrirDetalhesContas(StatusPagamento.ATRASADA, "Contas Atrasadas")
        );

        view.findViewById(R.id.cardPagas).setOnClickListener(v ->
                abrirDetalhesContas(StatusPagamento.PAGA, "Contas Pagas no Mês")
        );

        atualizarDashboard();
    }

    private void abrirDetalhesContas(StatusPagamento status, String titulo) {
        ContasFiltradasBottomSheet.novaInstancia(status, titulo)
                .show(getParentFragmentManager(), "ContasFiltradasModal");
    }

    @Override
    public void onResume() {
        super.onResume();

        if (contaRepository == null) {
            contaRepository = new ContaRepository(requireContext());
        }
        if (rendaRepository == null) {
            rendaRepository = new RendaRepository(requireContext());
        }

        atualizarDashboard();
    }

    private void atualizarDashboard() {
        if (contaRepository == null || rendaRepository == null || textMesAtual == null) {
            return;
        }

        YearMonth mesAtual = YearMonth.now();

        textMesAtual.setText(
                capitalizar(mesAtual.format(formatoMes))
        );

        BigDecimal totalRendas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        BigDecimal totalPendentes = BigDecimal.ZERO;
        BigDecimal totalAtrasadas = BigDecimal.ZERO;
        BigDecimal totalPagas = BigDecimal.ZERO;

        int quantidadePendentes = 0;
        int quantidadeAtrasadas = 0;
        int quantidadePagas = 0;

        List<Renda> rendas = rendaRepository.listarTodas();

        if (rendas != null) {
            for (Renda renda : rendas) {
                if (renda.getValor() != null && renda.getData() != null) {
                    YearMonth mesRenda = null;
                    Object dataObj = renda.getData();

                    if (dataObj instanceof YearMonth) {
                        mesRenda = (YearMonth) dataObj;
                    } else if (dataObj instanceof LocalDate) {
                        mesRenda = YearMonth.from((LocalDate) dataObj);
                    }

                    if (mesRenda != null && mesRenda.equals(mesAtual)) {
                        totalRendas = totalRendas.add(renda.getValor());
                    }
                }
            }
        }

        List<Conta> contas = contaRepository.listarTodas();

        if (contas != null) {
            for (Conta conta : contas) {
                if (conta.getValor() == null) {
                    continue;
                }

                LocalDate dataLancamento = conta.getDataDeLancamento();

                boolean lancadaNesteMes =
                        dataLancamento != null
                                && YearMonth.from(dataLancamento).equals(mesAtual);

                if (lancadaNesteMes) {
                    totalDespesas = totalDespesas.add(conta.getValor());
                }

                StatusPagamento status = conta.getStatusPagamentoEfetivo();

                if (status == StatusPagamento.PAGA) {
                    if (lancadaNesteMes) {
                        totalPagas = totalPagas.add(conta.getValor());
                        quantidadePagas++;
                    }
                } else if (status == StatusPagamento.ATRASADA) {
                    totalAtrasadas = totalAtrasadas.add(conta.getValor());
                    quantidadeAtrasadas++;
                } else {
                    totalPendentes = totalPendentes.add(conta.getValor());
                    quantidadePendentes++;
                }
            }
        }

        BigDecimal saldo = totalRendas.subtract(totalDespesas);

        textTotalRendas.setText(formatoMoeda.format(totalRendas));
        textTotalDespesas.setText(formatoMoeda.format(totalDespesas));
        textSaldoMes.setText(formatoMoeda.format(saldo));

        textTotalPendentes.setText(formatoMoeda.format(totalPendentes));
        textQuantidadePendentes.setText(
                quantidadePendentes == 1
                        ? "1 conta pendente"
                        : quantidadePendentes + " contas pendentes"
        );

        textTotalAtrasadas.setText(formatoMoeda.format(totalAtrasadas));
        textQuantidadeAtrasadas.setText(
                quantidadeAtrasadas == 1
                        ? "1 conta atrasada"
                        : quantidadeAtrasadas + " contas atrasadas"
        );

        textTotalPagas.setText(formatoMoeda.format(totalPagas));
        textQuantidadePagas.setText(
                quantidadePagas == 1
                        ? "1 conta paga neste mês"
                        : quantidadePagas + " contas pagas neste mês"
        );
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        return texto.substring(0, 1)
                .toUpperCase(new Locale("pt", "BR"))
                + texto.substring(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (contaRepository != null) {
            contaRepository.fechar();
            contaRepository = null;
        }

        if (rendaRepository != null) {
            rendaRepository.fechar();
            rendaRepository = null;
        }
    }
}