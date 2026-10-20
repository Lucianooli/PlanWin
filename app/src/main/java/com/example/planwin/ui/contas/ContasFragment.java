package com.example.planwin.ui.contas;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.planwin.R;
import com.example.planwin.entity.Conta;
import com.example.planwin.entity.Usuario;
import com.example.planwin.enums.StatusPagamento;
import com.example.planwin.service.ServiceConta;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ContasFragment extends Fragment {

    private ServiceConta serviceConta;
    private LinearLayout listaContasContainer;
    private TextView textQuantidadeContas;
    private TextView textEstadoContas;

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ContasFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_contas,
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

        listaContasContainer =
                view.findViewById(R.id.listaContasContainer);

        textQuantidadeContas =
                view.findViewById(R.id.textQuantidadeContas);

        textEstadoContas =
                view.findViewById(R.id.textEstadoContas);

        Usuario usuario = new Usuario(
                1,
                "Usuário",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        serviceConta = new ServiceConta(
                requireContext(),
                usuario
        );

        Button btnNovaConta = view.findViewById(R.id.btnNovaConta);

        btnNovaConta.setOnClickListener(
                v -> mostrarDialogNovaConta()
        );

        carregarContas();
    }

    private void mostrarDialogNovaConta() {
        View formulario = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_nova_conta, null);

        TextInputEditText edtNome =
                formulario.findViewById(R.id.edtNomeConta);

        TextInputEditText edtValor =
                formulario.findViewById(R.id.edtValorConta);

        AutoCompleteTextView spinnerCategoria =
                formulario.findViewById(R.id.spinnerCategoria);

        String[] categorias = new String[]{"Casa", "Alimento", "Saúde", "Carro", "Viagem", "Roupa", "Cartão", "Outro"};
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        spinnerCategoria.setAdapter(adapterCategorias);
        spinnerCategoria.setText("Outro", false);

        LinearLayout containerDiaFixo = formulario.findViewById(R.id.containerDiaFixo);
        LinearLayout containerDataCompleta = formulario.findViewById(R.id.containerDataCompleta);
        TextInputEditText edtDiaFixo = formulario.findViewById(R.id.edtDiaFixo);

        Button btnData =
                formulario.findViewById(R.id.btnSelecionarVencimento);

        TextView textData =
                formulario.findViewById(R.id.textDataVencimento);

        RadioGroup grupoTipo =
                formulario.findViewById(R.id.grupoTipoConta);

        final LocalDate[] vencimento = {null};

        grupoTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioFixa) {
                containerDiaFixo.setVisibility(View.VISIBLE);
                containerDataCompleta.setVisibility(View.GONE);
            } else {
                containerDiaFixo.setVisibility(View.GONE);
                containerDataCompleta.setVisibility(View.VISIBLE);
            }
        });

        btnData.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();

            DatePickerDialog seletor = new DatePickerDialog(
                    requireContext(),
                    (picker, ano, mes, dia) -> {
                        vencimento[0] = LocalDate.of(
                                ano,
                                mes + 1,
                                dia
                        );

                        textData.setText(
                                vencimento[0].format(formatoData)
                        );
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
            );

            seletor.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Nova conta")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(ignorado ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String nome = edtNome.getText() == null
                                    ? ""
                                    : edtNome.getText().toString().trim();

                            String valorTexto = edtValor.getText() == null
                                    ? ""
                                    : edtValor.getText().toString().trim();

                            String categoriaSelecionada = spinnerCategoria.getText() == null
                                    ? "Outro"
                                    : spinnerCategoria.getText().toString().trim();

                            if (nome.isEmpty()) {
                                edtNome.setError("Informe o nome da conta.");
                                edtNome.requestFocus();
                                return;
                            }

                            if (valorTexto.isEmpty()) {
                                edtValor.setError("Informe o valor.");
                                edtValor.requestFocus();
                                return;
                            }

                            BigDecimal valor;

                            try {
                                valor = new BigDecimal(
                                        valorTexto.replace(",", ".")
                                );
                            } catch (NumberFormatException erro) {
                                edtValor.setError("Informe um valor válido.");
                                edtValor.requestFocus();
                                return;
                            }

                            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                                edtValor.setError("O valor deve ser maior que zero.");
                                edtValor.requestFocus();
                                return;
                            }

                            int opcao = grupoTipo.getCheckedRadioButtonId();

                            if (opcao != R.id.radioFixa
                                    && opcao != R.id.radioVariavel) {
                                Toast.makeText(
                                        requireContext(),
                                        "Selecione o tipo de conta.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            String tipo = opcao == R.id.radioFixa
                                    ? "Fixa"
                                    : "Mes";

                            Conta conta;

                            if ("Fixa".equalsIgnoreCase(tipo)) {
                                String diaFixoTexto = edtDiaFixo.getText() == null ? "" : edtDiaFixo.getText().toString().trim();
                                if (diaFixoTexto.isEmpty()) {
                                    edtDiaFixo.setError("Informe o dia de vencimento (1 a 31).");
                                    edtDiaFixo.requestFocus();
                                    return;
                                }

                                int diaFixo = Integer.parseInt(diaFixoTexto);
                                if (diaFixo < 1 || diaFixo > 31) {
                                    edtDiaFixo.setError("Dia inválido.");
                                    edtDiaFixo.requestFocus();
                                    return;
                                }

                                conta = new Conta(
                                        0,
                                        nome,
                                        valor,
                                        null,
                                        diaFixo,
                                        LocalDate.now(),
                                        tipo,
                                        categoriaSelecionada,
                                        StatusPagamento.PENDENTE,
                                        null
                                );
                            } else {
                                if (vencimento[0] == null) {
                                    Toast.makeText(
                                            requireContext(),
                                            "Selecione o vencimento.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    return;
                                }

                                conta = new Conta(
                                        0,
                                        nome,
                                        valor,
                                        vencimento[0],
                                        null,
                                        LocalDate.now(),
                                        tipo,
                                        categoriaSelecionada,
                                        StatusPagamento.PENDENTE,
                                        null
                                );
                            }

                            boolean sucesso =
                                    serviceConta.cadastrarConta(conta);

                            if (sucesso) {
                                carregarContas();

                                Toast.makeText(
                                        requireContext(),
                                        "Conta salva com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "Não foi possível salvar. Confira os dados ou verifique se a conta já está cadastrada.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        })
        );

        dialog.show();
    }

    private void carregarContas() {
        listaContasContainer.removeAllViews();

        List<Conta> contas = serviceConta.listarContas();

        textQuantidadeContas.setText(
                contas.size()
                        + (contas.size() == 1
                        ? " conta cadastrada"
                        : " contas cadastradas")
        );

        if (contas.isEmpty()) {
            textEstadoContas.setVisibility(View.VISIBLE);
            textEstadoContas.setText(
                    "Você ainda não cadastrou contas.\n"
                            + "Toque em Nova conta para começar."
            );
            return;
        }

        textEstadoContas.setVisibility(View.GONE);

        for (Conta conta : contas) {
            listaContasContainer.addView(criarCartaoConta(conta));
        }
    }

    private View criarCartaoConta(Conta conta) {
        Context context = requireContext();

        // Obtém as cores do tema ativo (Suporta Light e Dark Theme dinamicamente)
        int colorSurfaceContainer = getCorDoTema(context, com.google.android.material.R.attr.colorSurfaceContainerLow);
        int colorOnSurface = getCorDoTema(context, com.google.android.material.R.attr.colorOnSurface);
        int colorOnSurfaceVariant = getCorDoTema(context, com.google.android.material.R.attr.colorOnSurfaceVariant);
        int colorOutlineVariant = getCorDoTema(context, com.google.android.material.R.attr.colorOutlineVariant);
        int colorPrimary = getCorDoTema(context, com.google.android.material.R.attr.colorPrimary);
        int colorError = getCorDoTema(context, com.google.android.material.R.attr.colorError);

        // Cartão moderno com cores dinâmicas do tema
        MaterialCardView cartao = new MaterialCardView(context);
        cartao.setRadius(32f);
        cartao.setCardElevation(0f);
        cartao.setStrokeWidth(2);
        cartao.setStrokeColor(colorOutlineVariant);
        cartao.setCardBackgroundColor(colorSurfaceContainer);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, 16);
        cartao.setLayoutParams(parametros);

        // Container interno
        LinearLayout containerInterno = new LinearLayout(context);
        containerInterno.setOrientation(LinearLayout.VERTICAL);
        containerInterno.setPadding(24, 20, 24, 20);

        // Nome da Conta com Emoji de Categoria
        TextView nome = new TextView(context);
        String emoji = getEmojiCategoria(conta.getCategoria());
        nome.setText(emoji + "  " + conta.getNome());
        nome.setTextSize(18);
        nome.setTypeface(null, Typeface.BOLD);
        nome.setTextColor(colorOnSurface);

        // Subtítulo (Categoria e Tipo)
        TextView subInfo = new TextView(context);
        String categoriaNome = conta.getCategoria() != null ? conta.getCategoria() : "Outro";
        subInfo.setText(categoriaNome + " • " + (conta.getTipo().equalsIgnoreCase("Fixa") ? "Fixa Mensal" : "Conta do Mês"));
        subInfo.setTextSize(13);
        subInfo.setTextColor(colorOnSurfaceVariant);
        subInfo.setPadding(0, 2, 0, 12);

        // Valor Destacado
        TextView valor = new TextView(context);
        valor.setText(String.format(
                new Locale("pt", "BR"),
                "R$ %,.2f",
                conta.getValor()
        ));
        valor.setTextSize(24);
        valor.setTypeface(null, Typeface.BOLD);
        valor.setTextColor(colorPrimary);
        valor.setPadding(0, 0, 0, 8);

        // Vencimento
        TextView vencimento = new TextView(context);
        if ("Fixa".equalsIgnoreCase(conta.getTipo()) && conta.getDiaVencimentoFixo() != null) {
            vencimento.setText("📅 Vencimento: Todo dia " + conta.getDiaVencimentoFixo());
        } else {
            vencimento.setText(
                    "📅 Vencimento: "
                            + (conta.getDataDeVencimento() == null
                            ? "Não informado"
                            : conta.getDataDeVencimento().format(formatoData))
            );
        }
        vencimento.setTextSize(14);
        vencimento.setTextColor(colorOnSurfaceVariant);

        // Data de Pagamento (se houver)
        TextView dataPagamento = new TextView(context);
        if (conta.getDataPagamento() != null) {
            dataPagamento.setText(
                    "✅ Pago em: "
                            + conta.getDataPagamento().format(formatoData)
            );
            dataPagamento.setVisibility(View.VISIBLE);
            dataPagamento.setTextSize(13);
            dataPagamento.setTextColor(getCorDoTema(context, androidx.appcompat.R.attr.colorPrimary));
            dataPagamento.setPadding(0, 4, 0, 0);
        } else {
            dataPagamento.setVisibility(View.GONE);
        }

        // Status com cor dinâmica
        StatusPagamento status = conta.getStatusPagamentoEfetivo();
        TextView textoStatus = new TextView(context);
        textoStatus.setText("● Status: " + traduzirStatus(status));
        textoStatus.setTextSize(14);
        textoStatus.setTypeface(null, Typeface.BOLD);
        textoStatus.setTextColor(corStatus(status));
        textoStatus.setPadding(0, 8, 0, 16);

        containerInterno.addView(nome);
        containerInterno.addView(subInfo);
        containerInterno.addView(valor);
        containerInterno.addView(vencimento);
        if (conta.getDataPagamento() != null) {
            containerInterno.addView(dataPagamento);
        }
        containerInterno.addView(textoStatus);

        // Linha de ações
        LinearLayout linhaBotoes = new LinearLayout(context);
        linhaBotoes.setOrientation(LinearLayout.HORIZONTAL);

        MaterialButton btnPagamento = new MaterialButton(context);
        btnPagamento.setText(
                status == StatusPagamento.PAGA
                        ? "Desfazer"
                        : "Pagar"
        );
        btnPagamento.setTextSize(13);
        btnPagamento.setCornerRadius(16);

        MaterialButton btnEditar = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnEditar.setText("Editar");
        btnEditar.setTextSize(13);
        btnEditar.setCornerRadius(16);

        MaterialButton btnExcluir = new MaterialButton(context, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnExcluir.setText("Excluir");
        btnExcluir.setTextSize(13);
        btnExcluir.setCornerRadius(16);
        btnExcluir.setTextColor(colorError);

        LinearLayout.LayoutParams parametrosBotao =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1.2f
                );
        parametrosBotao.setMargins(0, 0, 6, 0);

        LinearLayout.LayoutParams parametrosEditar =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );
        parametrosEditar.setMargins(3, 0, 3, 0);

        LinearLayout.LayoutParams parametrosExcluir =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );
        parametrosExcluir.setMargins(6, 0, 0, 0);

        linhaBotoes.addView(btnPagamento, parametrosBotao);
        linhaBotoes.addView(btnEditar, parametrosEditar);
        linhaBotoes.addView(btnExcluir, parametrosExcluir);

        btnPagamento.setOnClickListener(v ->
                alternarStatusPagamento(conta)
        );

        btnEditar.setOnClickListener(v ->
                mostrarDialogEditarConta(conta)
        );

        btnExcluir.setOnClickListener(v ->
                confirmarExclusao(conta)
        );

        containerInterno.addView(linhaBotoes);
        cartao.addView(containerInterno);

        return cartao;
    }

    private int getCorDoTema(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr, value, true);
        return value.data;
    }

    private String getEmojiCategoria(String categoria) {
        if (categoria == null) return "📦";
        switch (categoria) {
            case "Casa": return "🏠";
            case "Alimento": return "🛒";
            case "Saúde": return "💊";
            case "Carro": return "🚗";
            case "Viagem": return "✈️";
            case "Roupa": return "👕";
            case "Cartão": return "💳";
            default: return "📦";
        }
    }

    private String traduzirStatus(StatusPagamento status) {
        switch (status) {
            case PAGA:
                return "Paga";

            case ATRASADA:
                return "Atrasada";

            case PENDENTE:
            default:
                return "Pendente";
        }
    }

    private int corStatus(StatusPagamento status) {
        switch (status) {
            case PAGA:
                return android.graphics.Color.parseColor("#22C55E");

            case ATRASADA:
                return android.graphics.Color.parseColor("#EF4444");

            case PENDENTE:
            default:
                return android.graphics.Color.parseColor("#F59E0B");
        }
    }

    private void alternarStatusPagamento(Conta conta) {
        if (conta.isPaga()) {
            conta.setPaga(false);
        } else {
            conta.setPaga(true);
        }

        if (serviceConta.atualizarConta(conta)) {
            carregarContas();

            String mensagem = conta.isPaga()
                    ? "Conta marcada como paga em "
                    + conta.getDataPagamento().format(formatoData)
                    : "Conta marcada como pendente.";

            Toast.makeText(
                    requireContext(),
                    mensagem,
                    Toast.LENGTH_SHORT
            ).show();

        } else {
            Toast.makeText(
                    requireContext(),
                    "Não foi possível atualizar o pagamento.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void mostrarDialogEditarConta(Conta conta) {
        View formulario = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_nova_conta, null);

        TextInputEditText edtNome =
                formulario.findViewById(R.id.edtNomeConta);

        TextInputEditText edtValor =
                formulario.findViewById(R.id.edtValorConta);

        AutoCompleteTextView spinnerCategoria =
                formulario.findViewById(R.id.spinnerCategoria);

        String[] categorias = new String[]{"Casa", "Alimento", "Saúde", "Carro", "Viagem", "Roupa", "Cartão", "Outro"};
        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        spinnerCategoria.setAdapter(adapterCategorias);
        if (conta.getCategoria() != null) {
            spinnerCategoria.setText(conta.getCategoria(), false);
        }

        LinearLayout containerDiaFixo = formulario.findViewById(R.id.containerDiaFixo);
        LinearLayout containerDataCompleta = formulario.findViewById(R.id.containerDataCompleta);
        TextInputEditText edtDiaFixo = formulario.findViewById(R.id.edtDiaFixo);

        Button btnData =
                formulario.findViewById(R.id.btnSelecionarVencimento);

        TextView textData =
                formulario.findViewById(R.id.textDataVencimento);

        RadioGroup grupoTipo =
                formulario.findViewById(R.id.grupoTipoConta);

        final LocalDate[] vencimento = {
                conta.getDataDeVencimento()
        };

        edtNome.setText(conta.getNome());
        edtValor.setText(conta.getValor().toPlainString());

        if (conta.getDiaVencimentoFixo() != null) {
            edtDiaFixo.setText(String.valueOf(conta.getDiaVencimentoFixo()));
        }

        if (vencimento[0] != null) {
            textData.setText(vencimento[0].format(formatoData));
        } else {
            textData.setText("Nenhuma data selecionada");
        }

        if ("Fixa".equalsIgnoreCase(conta.getTipo())) {
            grupoTipo.check(R.id.radioFixa);
            containerDiaFixo.setVisibility(View.VISIBLE);
            containerDataCompleta.setVisibility(View.GONE);
        } else {
            grupoTipo.check(R.id.radioVariavel);
            containerDiaFixo.setVisibility(View.GONE);
            containerDataCompleta.setVisibility(View.VISIBLE);
        }

        grupoTipo.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioFixa) {
                containerDiaFixo.setVisibility(View.VISIBLE);
                containerDataCompleta.setVisibility(View.GONE);
            } else {
                containerDiaFixo.setVisibility(View.GONE);
                containerDataCompleta.setVisibility(View.VISIBLE);
            }
        });

        btnData.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();

            if (vencimento[0] != null) {
                calendario.set(
                        vencimento[0].getYear(),
                        vencimento[0].getMonthValue() - 1,
                        vencimento[0].getDayOfMonth()
                );
            }

            new DatePickerDialog(
                    requireContext(),
                    (picker, ano, mes, dia) -> {
                        vencimento[0] = LocalDate.of(
                                ano,
                                mes + 1,
                                dia
                        );

                        textData.setText(
                                vencimento[0].format(formatoData)
                        );
                    },
                    calendario.get(Calendar.YEAR),
                    calendario.get(Calendar.MONTH),
                    calendario.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Editar conta")
                .setView(formulario)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar alterações", null)
                .create();

        dialog.setOnShowListener(ignorado ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String nome = edtNome.getText() == null
                                    ? ""
                                    : edtNome.getText().toString().trim();

                            String valorTexto = edtValor.getText() == null
                                    ? ""
                                    : edtValor.getText().toString().trim();

                            String categoriaSelecionada = spinnerCategoria.getText() == null
                                    ? "Outro"
                                    : spinnerCategoria.getText().toString().trim();

                            if (nome.isEmpty()) {
                                edtNome.setError("Informe o nome da conta.");
                                edtNome.requestFocus();
                                return;
                            }

                            BigDecimal valor;

                            try {
                                valor = new BigDecimal(
                                        valorTexto.replace(",", ".")
                                );
                            } catch (NumberFormatException erro) {
                                edtValor.setError("Informe um valor válido.");
                                edtValor.requestFocus();
                                return;
                            }

                            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                                edtValor.setError("O valor deve ser maior que zero.");
                                edtValor.requestFocus();
                                return;
                            }

                            int opcao = grupoTipo.getCheckedRadioButtonId();

                            if (opcao != R.id.radioFixa
                                    && opcao != R.id.radioVariavel) {
                                Toast.makeText(
                                        requireContext(),
                                        "Selecione o tipo de conta.",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }

                            String tipo = opcao == R.id.radioFixa
                                    ? "Fixa"
                                    : "Mes";

                            Conta atualizada;

                            if ("Fixa".equalsIgnoreCase(tipo)) {
                                String diaFixoTexto = edtDiaFixo.getText() == null ? "" : edtDiaFixo.getText().toString().trim();
                                int diaFixo = diaFixoTexto.isEmpty() ? 1 : Integer.parseInt(diaFixoTexto);

                                atualizada = new Conta(
                                        conta.getId(),
                                        nome,
                                        valor,
                                        null,
                                        diaFixo,
                                        conta.getDataDeLancamento(),
                                        tipo,
                                        categoriaSelecionada,
                                        conta.getStatusPagamento(),
                                        conta.getDataPagamento()
                                );
                            } else {
                                atualizada = new Conta(
                                        conta.getId(),
                                        nome,
                                        valor,
                                        vencimento[0],
                                        null,
                                        conta.getDataDeLancamento(),
                                        tipo,
                                        categoriaSelecionada,
                                        conta.getStatusPagamento(),
                                        conta.getDataPagamento()
                                );
                            }

                            if (serviceConta.atualizarConta(atualizada)) {
                                carregarContas();

                                Toast.makeText(
                                        requireContext(),
                                        "Conta atualizada com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            } else {
                                Toast.makeText(
                                        requireContext(),
                                        "Não foi possível atualizar a conta.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        })
        );

        dialog.show();
    }

    private void confirmarExclusao(Conta conta) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir conta")
                .setMessage(
                        "Deseja realmente excluir a conta \""
                                + conta.getNome()
                                + "\"? Essa ação não pode ser desfeita."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    if (serviceConta.excluirConta(conta.getId())) {
                        carregarContas();

                        Toast.makeText(
                                requireContext(),
                                "Conta excluída com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                requireContext(),
                                "Não foi possível excluir a conta.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }
}