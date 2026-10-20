package com.example.planwin.ui.rendas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwin.R;
import com.example.planwin.entity.Renda;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RendaAdapter extends RecyclerView.Adapter<RendaAdapter.RendaViewHolder> {

    public interface OnRendaClickListener {
        void onEditar(Renda renda);
        void onExcluir(Renda renda);
    }

    private final List<Renda> rendas = new ArrayList<>();
    private final OnRendaClickListener listener;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("MM/yyyy");

    public RendaAdapter(OnRendaClickListener listener) {
        this.listener = listener;
    }

    public void atualizarLista(List<Renda> novaLista) {
        rendas.clear();
        rendas.addAll(novaLista);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RendaViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_renda, parent, false);

        return new RendaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RendaViewHolder holder,
            int position
    ) {
        Renda renda = rendas.get(position);

        String emoji = getEmojiCategoria(renda.getCategoria());
        holder.txtNome.setText(emoji + "  " + renda.getNome());
        holder.txtValor.setText(formatoMoeda.format(renda.getValor()));

        String categoriaNome = renda.getCategoria() != null ? renda.getCategoria() : "Outro";
        if ("Fixa".equalsIgnoreCase(renda.getTipo()) && renda.getDiaRecebimentoFixo() != null) {
            holder.txtDetalhes.setText(categoriaNome + " • Fixa (Recebimento dia " + renda.getDiaRecebimentoFixo() + ")");
        } else if (renda.getData() != null) {
            holder.txtDetalhes.setText(categoriaNome + " • Mês " + renda.getData().format(formatoData));
        } else {
            holder.txtDetalhes.setText(categoriaNome + " • " + renda.getTipo());
        }

        if (renda.getDescricao() != null
                && !renda.getDescricao().trim().isEmpty()) {
            holder.txtDescricao.setText(renda.getDescricao());
            holder.txtDescricao.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescricao.setVisibility(View.GONE);
        }

        holder.btnEditar.setOnClickListener(
                v -> listener.onEditar(renda)
        );

        holder.btnExcluir.setOnClickListener(
                v -> listener.onExcluir(renda)
        );
    }

    private String getEmojiCategoria(String categoria) {
        if (categoria == null) return "💵";
        switch (categoria) {
            case "Salário": return "💼";
            case "Extra": return "💰";
            case "Investimento": return "📈";
            default: return "💵";
        }
    }

    @Override
    public int getItemCount() {
        return rendas.size();
    }

    static class RendaViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome;
        TextView txtValor;
        TextView txtDetalhes;
        TextView txtDescricao;
        MaterialButton btnEditar;
        MaterialButton btnExcluir;

        RendaViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.txtNomeRenda);
            txtValor = itemView.findViewById(R.id.txtValorRenda);
            txtDetalhes = itemView.findViewById(R.id.txtDetalhesRenda);
            txtDescricao = itemView.findViewById(R.id.txtDescricaoRenda);
            btnEditar = itemView.findViewById(R.id.btnEditarRenda);
            btnExcluir = itemView.findViewById(R.id.btnExcluirRenda);
        }
    }
}