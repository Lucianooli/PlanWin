package com.example.planwin.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ContasFiltradasAdapter extends RecyclerView.Adapter<ContasFiltradasAdapter.ViewHolder> {

    private final List<Conta> contas;
    private final NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ContasFiltradasAdapter(List<Conta> contas) {
        this.contas = contas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conta_filtrada, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conta conta = contas.get(position);

        // CORRIGIDO AQUI: Usando getNome() em vez de getDescricao()
        holder.textNome.setText(conta.getNome() != null ? conta.getNome() : "Conta sem nome");

        if (conta.getValor() != null) {
            holder.textValor.setText(formatoMoeda.format(conta.getValor()));
        } else {
            holder.textValor.setText("R$ 0,00");
        }

        if (conta.getDataDeVencimento() != null) {
            holder.textData.setText("Vencimento: " + conta.getDataDeVencimento().format(formatoData));
        } else {
            holder.textData.setText("Sem data definida");
        }
    }

    @Override
    public int getItemCount() {
        return contas != null ? contas.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNome, textData, textValor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNome = itemView.findViewById(R.id.textNomeConta);
            textData = itemView.findViewById(R.id.textDataConta);
            textValor = itemView.findViewById(R.id.textValorConta);
        }
    }
}