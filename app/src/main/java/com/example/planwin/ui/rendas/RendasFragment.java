package com.example.planwin.ui.rendas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.planwin.R;
import com.example.planwin.entity.Renda;
import com.example.planwin.service.ServiceRenda;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RendasFragment extends Fragment {

    private ServiceRenda serviceRenda;
    private RendaAdapter rendaAdapter;

    private TextView txtTotalRendas;
    private TextView txtRendasVazio;
    private RecyclerView recyclerRendas;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public RendasFragment() {
        super(R.layout.fragment_rendas);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        serviceRenda = new ServiceRenda(requireContext());

        txtTotalRendas = view.findViewById(R.id.txtTotalRendas);
        txtRendasVazio = view.findViewById(R.id.txtRendasVazio);
        recyclerRendas = view.findViewById(R.id.recyclerRendas);

        rendaAdapter = new RendaAdapter(
                new RendaAdapter.OnRendaClickListener() {
                    @Override
                    public void onEditar(Renda renda) {
                        mostrarFormulario(renda);
                    }

                    @Override
                    public void onExcluir(Renda renda) {
                        confirmarExclusao(renda);
                    }
                }
        );

        recyclerRendas.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        recyclerRendas.setAdapter(rendaAdapter);

        view.findViewById(R.id.btnNovaRenda)
                .setOnClickListener(v -> mostrarFormulario(null));

        carregarRendas();
    }

    private void carregarRendas() {
        List<Renda> rendas = serviceRenda.listarRendas();
        rendaAdapter.atualizarLista(rendas);

        BigDecimal total = BigDecimal.ZERO;
        for (Renda renda : rendas) {
            total = total.add(renda.getValor());
        }

        txtTotalRendas.setText(formatoMoeda.format(total));

        boolean listaVazia = rendas.isEmpty();
        txtRendasVazio.setVisibility(listaVazia ? View.VISIBLE : View.GONE);
        recyclerRendas.setVisibility(listaVazia ? View.GONE : View.VISIBLE);
    }

    private void mostrarFormulario(@Nullable Renda rendaExistente) {
        boolean editando = rendaExistente != null;
        Context context = requireContext();

        LinearLayout formulario = new LinearLayout(context);
        formulario.setOrientation(LinearLayout.VERTICAL);
        int margem = dp(20);
        formulario.setPadding(margem, dp(8), margem, 0);

        EditText edtNome = criarCampo("Nome da renda (ex.: Salário)", InputType.TYPE_CLASS_TEXT);
        EditText edtValor = criarCampo("Valor (ex.: 2500,00)", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AutoCompleteTextView spinnerCategoria = new AutoCompleteTextView(context);
        spinnerCategoria.setHint("Categoria");
        spinnerCategoria.setInputType(InputType.TYPE_NULL);
        spinnerCategoria.setFocusable(false);
        spinnerCategoria.setClickable(true);

        String[] categorias = new String[]{"Salário", "Extra", "Investimento", "Outro"};
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        spinnerCategoria.setAdapter(adapterCategorias);
        spinnerCategoria.setText("Salário", false);
        spinnerCategoria.setOnClickListener(v -> spinnerCategoria.showDropDown());

        RadioGroup grupoTipo = new RadioGroup(context);
        grupoTipo.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton radioFixa = new RadioButton(context);
        radioFixa.setId(View.generateViewId());
        radioFixa.setText("Renda Fixa");

        RadioButton radioMes = new RadioButton(context);
        radioMes.setId(View.generateViewId());
        radioMes.setText("Do Mês");

        grupoTipo.addView(radioFixa);
        grupoTipo.addView(radioMes);
        grupoTipo.check(radioFixa.getId());

        EditText edtDiaFixo = criarCampo("Dia do recebimento (ex.: 5)", InputType.TYPE_CLASS_NUMBER);
        edtDiaFixo.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(2)});

        EditText edtData = criarCampo("Selecione o mês de referência", InputType.TYPE_NULL);
        edtData.setFocusable(false);
        edtData.setClickable(true);

        final YearMonth[] mesSelecionado = {YearMonth.now()};
        edtData.setText(mesSelecionado[0].toString());

        edtData.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(
                    context,
                    (picker, ano, mes, dia) -> {
                        mesSelecionado[0] = YearMonth.of(ano, mes + 1);
                        edtData.setText(mesSelecionado[0].toString());
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        EditText edtDescricao = criarCampo("Descrição (opcional)", InputType.TYPE_CLASS_TEXT);

        adicionarCampo(formulario, edtNome);
        adicionarCampo(formulario, edtValor);
        adicionarCampo(formulario, spinnerCategoria);
        adicionarCampo(formulario, grupoTipo);
        adicionarCampo(formulario, edtDiaFixo);
        adicionarCampo(formulario, edtData);
        adicionarCampo(formulario, edtDescricao);

        edtData.setVisibility(View.GONE);

        grupoTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == radioFixa.getId()) {
                edtDiaFixo.setVisibility(View.VISIBLE);
                edtData.setVisibility(View.GONE);
            } else {
                edtDiaFixo.setVisibility(View.GONE);
                edtData.setVisibility(View.VISIBLE);
            }
        });

        if (editando) {
            edtNome.setText(rendaExistente.getNome());
            edtValor.setText(rendaExistente.getValor().toPlainString());
            spinnerCategoria.setText(rendaExistente.getCategoria() != null ? rendaExistente.getCategoria() : "Salário", false);
            edtDescricao.setText(rendaExistente.getDescricao() == null ? "" : rendaExistente.getDescricao());

            if ("Fixa".equalsIgnoreCase(rendaExistente.getTipo())) {
                grupoTipo.check(radioFixa.getId());
                if (rendaExistente.getDiaRecebimentoFixo() != null) {
                    edtDiaFixo.setText(String.valueOf(rendaExistente.getDiaRecebimentoFixo()));
                }
                edtDiaFixo.setVisibility(View.VISIBLE);
                edtData.setVisibility(View.GONE);
            } else {
                grupoTipo.check(radioMes.getId());
                if (rendaExistente.getData() != null) {
                    mesSelecionado[0] = rendaExistente.getData();
                    edtData.setText(mesSelecionado[0].toString());
                }
                edtDiaFixo.setVisibility(View.GONE);
                edtData.setVisibility(View.VISIBLE);
            }
        } else {
            edtDiaFixo.setText("5");
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(editando ? "Editar renda" : "Nova renda")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(editando ? "Salvar alterações" : "Cadastrar", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        try {
                            String nome = edtNome.getText().toString().trim();
                            String valorTexto = edtValor.getText().toString().trim().replace(" ", "");

                            if (valorTexto.contains(",")) {
                                valorTexto = valorTexto.replace(".", "").replace(",", ".");
                            }

                            String categoria = spinnerCategoria.getText().toString().trim();
                            String descricao = edtDescricao.getText().toString().trim();

                            boolean isFixa = (grupoTipo.getCheckedRadioButtonId() == radioFixa.getId());
                            String tipo = isFixa ? "Fixa" : "Mes";

                            if (nome.isEmpty() || valorTexto.isEmpty()) {
                                mostrarMensagem("Preencha o nome e o valor.");
                                return;
                            }

                            BigDecimal valor = new BigDecimal(valorTexto);
                            Integer diaFixo = null;
                            YearMonth data;

                            if (isFixa) {
                                String diaStr = edtDiaFixo.getText().toString().trim();
                                if (diaStr.isEmpty()) {
                                    mostrarMensagem("Informe o dia de recebimento.");
                                    return;
                                }
                                diaFixo = Integer.parseInt(diaStr);
                                data = YearMonth.now();
                            } else {
                                data = mesSelecionado[0];
                            }

                            Renda renda = new Renda(
                                    editando ? rendaExistente.getId() : 0,
                                    nome,
                                    valor,
                                    data,
                                    diaFixo,
                                    tipo,
                                    categoria,
                                    descricao.isEmpty() ? null : descricao
                            );

                            boolean sucesso = editando
                                    ? serviceRenda.atualizarRenda(renda)
                                    : serviceRenda.cadastrarRenda(renda);

                            if (sucesso) {
                                dialog.dismiss();
                                carregarRendas();
                                mostrarMensagem(editando ? "Renda atualizada!" : "Renda cadastrada!");
                            } else {
                                mostrarMensagem("Não foi possível salvar. Confira os dados.");
                            }

                        } catch (NumberFormatException e) {
                            mostrarMensagem("Informe um valor monetário válido.");
                        } catch (Exception e) {
                            mostrarMensagem("Confira os dados informados.");
                        }
                    });
        });

        dialog.show();
    }

    private EditText criarCampo(String dica, int tipoEntrada) {
        EditText campo = new EditText(requireContext());
        campo.setHint(dica);
        campo.setInputType(tipoEntrada);
        campo.setSingleLine(true);
        return campo;
    }

    private void adicionarCampo(LinearLayout formulario, View campo) {
        LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        parametros.bottomMargin = dp(12);
        formulario.addView(campo, parametros);
    }

    private void confirmarExclusao(Renda renda) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir renda")
                .setMessage("Deseja realmente excluir \"" + renda.getNome() + "\"?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (serviceRenda.excluirRenda(renda.getId())) {
                        carregarRendas();
                        mostrarMensagem("Renda excluída.");
                    } else {
                        mostrarMensagem("Não foi possível excluir a renda.");
                    }
                })
                .show();
    }

    private void mostrarMensagem(String mensagem) {
        Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show();
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }
}