package com.example.planwin.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.planwin.database.PlanWinDatabase;
import com.example.planwin.entity.Conta;
import com.example.planwin.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ContaRepository {

    private final PlanWinDatabase helper;

    public ContaRepository(Context context) {
        helper = new PlanWinDatabase(context);
    }

    private ContentValues converterParaContentValues(Conta conta) {
        ContentValues values = new ContentValues();

        values.put("nome", conta.getNome());
        values.put("valor", conta.getValor().toPlainString());

        if (conta.getDataDeVencimento() != null) {
            values.put(
                    "data_vencimento",
                    conta.getDataDeVencimento().toString()
            );
        } else {
            values.putNull("data_vencimento");
        }

        // Salva o dia de vencimento fixo, se houver
        if (conta.getDiaVencimentoFixo() != null) {
            values.put("dia_vencimento_fixo", conta.getDiaVencimentoFixo());
        } else {
            values.putNull("dia_vencimento_fixo");
        }

        values.put(
                "data_lancamento",
                conta.getDataDeLancamento().toString()
        );

        values.put("tipo", conta.getTipo());

        // Salva a categoria
        values.put("categoria", conta.getCategoria() != null ? conta.getCategoria() : "Outro");

        // Salva o status do pagamento.
        StatusPagamento status = conta.getStatusPagamento();

        values.put(
                "status_pagamento",
                status == StatusPagamento.PAGA
                        ? StatusPagamento.PAGA.name()
                        : StatusPagamento.PENDENTE.name()
        );

        // Salva a data do pagamento, se existir.
        if (conta.getDataPagamento() != null) {
            values.put(
                    "data_pagamento",
                    conta.getDataPagamento().toString()
            );
        } else {
            values.putNull("data_pagamento");
        }

        return values;
    }

    public long inserir(Conta conta) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = converterParaContentValues(conta);

        if (conta.getId() > 0) {
            values.put("id", conta.getId());
        }

        long id = db.insert("contas", null, values);

        if (id != -1) {
            conta.setId((int) id);
        }

        return id;
    }

    public List<Conta> listarTodas() {
        List<Conta> contas = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "contas",
                null,
                null,
                null,
                null,
                null,
                "id ASC"
        )) {
            while (cursor.moveToNext()) {
                contas.add(converterParaConta(cursor));
            }
        }

        return contas;
    }

    public Conta buscarPorId(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "contas",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return converterParaConta(cursor);
            }
        }

        return null;
    }

    public int atualizar(Conta conta) {
        if (conta.getId() <= 0) {
            return 0;
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = converterParaContentValues(conta);

        return db.update(
                "contas",
                values,
                "id = ?",
                new String[]{String.valueOf(conta.getId())}
        );
    }

    public int excluir(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();

        return db.delete(
                "contas",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private Conta converterParaConta(Cursor cursor) {
        int id = cursor.getInt(
                cursor.getColumnIndexOrThrow("id")
        );

        String nome = cursor.getString(
                cursor.getColumnIndexOrThrow("nome")
        );

        BigDecimal valor = new BigDecimal(
                cursor.getString(
                        cursor.getColumnIndexOrThrow("valor")
                )
        );

        String vencimento = cursor.getString(
                cursor.getColumnIndexOrThrow("data_vencimento")
        );

        LocalDate dataDeVencimento = vencimento == null
                ? null
                : LocalDate.parse(vencimento);

        // Recupera o dia de vencimento fixo
        int columnIndexDiaFixo = cursor.getColumnIndex("dia_vencimento_fixo");
        Integer diaVencimentoFixo = (columnIndexDiaFixo != -1 && !cursor.isNull(columnIndexDiaFixo))
                ? cursor.getInt(columnIndexDiaFixo)
                : null;

        LocalDate dataDeLancamento = LocalDate.parse(
                cursor.getString(
                        cursor.getColumnIndexOrThrow("data_lancamento")
                )
        );

        String tipo = cursor.getString(
                cursor.getColumnIndexOrThrow("tipo")
        );

        // Recupera a categoria
        int columnIndexCategoria = cursor.getColumnIndex("categoria");
        String categoria = (columnIndexCategoria != -1 && !cursor.isNull(columnIndexCategoria))
                ? cursor.getString(columnIndexCategoria)
                : "Outro";

        String statusSalvo = cursor.getString(
                cursor.getColumnIndexOrThrow("status_pagamento")
        );

        StatusPagamento statusPagamento =
                StatusPagamento.PENDENTE;

        if (StatusPagamento.PAGA.name().equals(statusSalvo)) {
            statusPagamento = StatusPagamento.PAGA;
        }

        // Recupera a data de pagamento do SQLite.
        String pagamento = cursor.getString(
                cursor.getColumnIndexOrThrow("data_pagamento")
        );

        LocalDate dataPagamento = pagamento == null
                ? null
                : LocalDate.parse(pagamento);

        return new Conta(
                id,
                nome,
                valor,
                dataDeVencimento,
                diaVencimentoFixo,
                dataDeLancamento,
                tipo,
                categoria,
                statusPagamento,
                dataPagamento
        );
    }

    public void fechar() {
        helper.close();
    }
}