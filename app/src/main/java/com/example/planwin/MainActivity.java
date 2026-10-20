package com.example.planwin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.planwin.ui.home.HomeFragment;
import com.example.planwin.ui.contas.ContasFragment;
import com.example.planwin.ui.rendas.RendasFragment;
import com.example.planwin.ui.relatorios.RelatoriosFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView textTituloTela;
    private BottomNavigationView bottomNavigation;

    private static final String PREFS = "planwin_config";
    private static final String CHAVE_TEMA = "tema_escuro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        aplicarTemaSalvo();

        super.onCreate(savedInstanceState);

        // Habilita o modo Edge-to-Edge nativo moderno
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        textTituloTela = findViewById(R.id.textTituloTela);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Aplica o padding automático baseado nas barras do sistema (status bar / bateria / hora)
        LinearLayout rootLayout = findViewById(R.id.rootLayoutMain);
        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, insets) -> {
                var systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                // Aplica margem apenas no topo para o cabeçalho não ficar colado na porcentagem do celular
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        configurarNavegacao();

        findViewById(R.id.btnTema).setOnClickListener(
                view -> alternarTema()
        );

        if (savedInstanceState == null) {
            abrirTela(R.id.navInicio);
        }
    }

    private void configurarNavegacao() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            abrirTela(item.getItemId());
            return true;
        });
    }

    private void abrirTela(int itemId) {
        Fragment fragment;
        String titulo;

        if (itemId == R.id.navInicio) {
            fragment = new HomeFragment();
            titulo = "Início";

        } else if (itemId == R.id.navContas) {
            fragment = new ContasFragment();
            titulo = "Contas";

        } else if (itemId == R.id.navRendas) {
            fragment = new RendasFragment();
            titulo = "Rendas";

        } else if (itemId == R.id.navRelatorios) {
            fragment = new RelatoriosFragment();
            titulo = "Relatórios";

        } else {
            return;
        }

        textTituloTela.setText(titulo);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void aplicarTemaSalvo() {
        SharedPreferences preferencias =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        boolean escuro =
                preferencias.getBoolean(CHAVE_TEMA, false);

        AppCompatDelegate.setDefaultNightMode(
                escuro
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private void alternarTema() {
        SharedPreferences preferencias =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        boolean temaAtual =
                preferencias.getBoolean(CHAVE_TEMA, false);

        boolean novoTema = !temaAtual;

        preferencias.edit()
                .putBoolean(CHAVE_TEMA, novoTema)
                .apply();

        AppCompatDelegate.setDefaultNightMode(
                novoTema
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}