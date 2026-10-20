package com.example.planwin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PlanWinDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "planwin.db";
    private static final int DATABASE_VERSION = 5; // Atualizado para a versão 5

    public PlanWinDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabela de contas
        db.execSQL(
                "CREATE TABLE contas (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nome TEXT NOT NULL, " +
                        "valor TEXT NOT NULL, " +
                        "data_vencimento TEXT, " +
                        "dia_vencimento_fixo INTEGER, " +
                        "data_lancamento TEXT NOT NULL, " +
                        "tipo TEXT NOT NULL, " +
                        "categoria TEXT DEFAULT 'Outro', " +
                        "status_pagamento TEXT NOT NULL DEFAULT 'PENDENTE', " +
                        "data_pagamento TEXT" +
                        ")"
        );

        // Tabela de rendas atualizada com categoria e dia fixo
        db.execSQL(
                "CREATE TABLE rendas (" +
                        "id INTEGER PRIMARY KEY, " +
                        "nome TEXT NOT NULL, " +
                        "valor TEXT NOT NULL, " +
                        "data TEXT, " +
                        "dia_recebimento_fixo INTEGER, " +
                        "tipo TEXT NOT NULL, " +
                        "categoria TEXT DEFAULT 'Outro', " +
                        "descricao TEXT" +
                        ")"
        );

        // Tabela de resumos financeiros
        db.execSQL(
                "CREATE TABLE resumos_financeiros (" +
                        "id INTEGER PRIMARY KEY, " +
                        "mes_ano TEXT NOT NULL UNIQUE, " +
                        "total_lucro TEXT NOT NULL, " +
                        "total_despesa TEXT NOT NULL, " +
                        "total_final TEXT NOT NULL" +
                        ")"
        );

        db.execSQL("CREATE INDEX idx_contas_data ON contas(data_lancamento)");
        db.execSQL("CREATE INDEX idx_rendas_data ON rendas(data)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE contas ADD COLUMN status_pagamento TEXT NOT NULL DEFAULT 'PENDENTE'");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE contas ADD COLUMN data_pagamento TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE contas ADD COLUMN categoria TEXT DEFAULT 'Outro'");
            db.execSQL("ALTER TABLE contas ADD COLUMN dia_vencimento_fixo INTEGER");
        }
        // Migração para a versão 5 (Adiciona Categoria e Dia Fixo nas Rendas)
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE rendas ADD COLUMN categoria TEXT DEFAULT 'Outro'");
            db.execSQL("ALTER TABLE rendas ADD COLUMN dia_recebimento_fixo INTEGER");
        }
    }
}