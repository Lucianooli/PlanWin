package com.example.planwin.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.planwin.database.PlanWinDatabase;
import com.example.planwin.entity.Renda;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class RendaRepository {

    private final PlanWinDatabase databaseHelper;

    public RendaRepository(Context context) {
        databaseHelper = new PlanWinDatabase(context);
    }

    public long inserir(Renda renda) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues valores = converterParaContentValues(renda);

        if (renda.getId() > 0) {
            valores.put("id", renda.getId());
        }

        long id = db.insert("rendas", null, valores);
        if (id != -1) {
            renda.setId((int) id);
        }
        return id;
    }

    public List<Renda> listarTodas() {
        List<Renda> rendas = new ArrayList<>();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "rendas",
                null,
                null,
                null,
                null,
                null,
                "id DESC"
        )) {
            while (cursor.moveToNext()) {
                rendas.add(converterParaRenda(cursor));
            }
        }

        return rendas;
    }

    public Renda buscarPorId(int id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "rendas",
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return converterParaRenda(cursor);
            }
        }

        return null;
    }

    public int atualizar(Renda renda) {
        if (renda.getId() <= 0) return 0;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues valores = converterParaContentValues(renda);

        return db.update(
                "rendas",
                valores,
                "id = ?",
                new String[]{String.valueOf(renda.getId())}
        );
    }

    public int excluir(int id) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        return db.delete("rendas", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues converterParaContentValues(Renda renda) {
        ContentValues valores = new ContentValues();

        valores.put("nome", renda.getNome());
        valores.put("valor", renda.getValor().toPlainString());

        if (renda.getData() != null) {
            valores.put("data", renda.getData().toString());
        } else {
            valores.putNull("data");
        }

        if (renda.getDiaRecebimentoFixo() != null) {
            valores.put("dia_recebimento_fixo", renda.getDiaRecebimentoFixo());
        } else {
            valores.putNull("dia_recebimento_fixo");
        }

        valores.put("tipo", renda.getTipo());
        valores.put("categoria", renda.getCategoria() != null ? renda.getCategoria() : "Outro");
        valores.put("descricao", renda.getDescricao());

        return valores;
    }

    private Renda converterParaRenda(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

        String nome = cursor.getString(cursor.getColumnIndexOrThrow("nome"));

        BigDecimal valor = new BigDecimal(
                cursor.getString(cursor.getColumnIndexOrThrow("valor"))
        );

        String dataStr = cursor.getString(cursor.getColumnIndexOrThrow("data"));
        YearMonth data = (dataStr != null && !dataStr.isEmpty()) ? YearMonth.parse(dataStr) : null;

        int indexDiaFixo = cursor.getColumnIndex("dia_recebimento_fixo");
        Integer diaFixo = (indexDiaFixo != -1 && !cursor.isNull(indexDiaFixo)) ? cursor.getInt(indexDiaFixo) : null;

        String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));

        int indexCat = cursor.getColumnIndex("categoria");
        String categoria = (indexCat != -1 && !cursor.isNull(indexCat)) ? cursor.getString(indexCat) : "Outro";

        String descricao = cursor.getString(cursor.getColumnIndexOrThrow("descricao"));

        return new Renda(id, nome, valor, data, diaFixo, tipo, categoria, descricao);
    }

    public void fechar() {
        databaseHelper.close();
    }
}