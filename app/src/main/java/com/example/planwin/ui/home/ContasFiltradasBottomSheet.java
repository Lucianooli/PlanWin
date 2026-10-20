package com.example.planwin.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.enums.StatusPagamento;
import com.example.planwin.repository.ContaRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ContasFiltradasBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STATUS = "arg_status";
    private static final String ARG_TITULO = "arg_titulo";

    private StatusPagamento statusFiltro;
    private String tituloFiltro;

    private ContaRepository contaRepository;

    public static ContasFiltradasBottomSheet novaInstancia(StatusPagamento status, String titulo) {
        ContasFiltradasBottomSheet fragment = new ContasFiltradasBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATUS, status);
        args.putString(ARG_TITULO, titulo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusFiltro = (StatusPagamento) getArguments().getSerializable(ARG_STATUS);
            tituloFiltro = getArguments().getString(ARG_TITULO);
        }
        contaRepository = new ContaRepository(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_lista_contas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textTituloFiltro = view.findViewById(R.id.textTituloFiltro);
        TextView textSubtituloMes = view.findViewById(R.id.textSubtituloMes);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerContasFiltradas);

        textTituloFiltro.setText(tituloFiltro);
        textSubtituloMes.setText("Mês atual: " + YearMonth.now());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Conta> todasContas = contaRepository.listarTodas();
        List<Conta> contasFiltradas = new ArrayList<>();
        YearMonth mesAtual = YearMonth.now();

        if (todasContas != null) {
            for (Conta conta : todasContas) {
                if (conta.getValor() == null) {
                    continue;
                }

                // Avalia o status efetivo considerando vencimentos e pagamentos reais
                StatusPagamento statusEfetivo = conta.getStatusPagamentoEfetivo();

                boolean lancadaNesteMes = conta.getDataDeLancamento() != null &&
                        YearMonth.from(conta.getDataDeLancamento()).equals(mesAtual);

                // Aplica a mesma regra de filtro que você usa no dashboard
                if (statusEfetivo == statusFiltro) {
                    if (statusFiltro == StatusPagamento.ATRASADA) {
                        contasFiltradas.add(conta);
                    } else if (lancadaNesteMes) {
                        contasFiltradas.add(conta);
                    }
                }
            }
        }

        // Popula o RecyclerView com o Adapter
        ContasFiltradasAdapter adapter = new ContasFiltradasAdapter(contasFiltradas);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (contaRepository != null) {
            contaRepository.fechar();
        }
    }
}