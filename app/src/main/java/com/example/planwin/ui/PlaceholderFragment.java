
package com.example.planwin.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITULO = "titulo";
    private static final String ARG_MENSAGEM = "mensagem";

    public static PlaceholderFragment novaInstancia(
            String titulo,
            String mensagem
    ) {
        PlaceholderFragment fragment = new PlaceholderFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITULO, titulo);
        args.putString(ARG_MENSAGEM, mensagem);

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Bundle args = getArguments();

        String titulo = args != null
                ? args.getString(ARG_TITULO, "")
                : "";

        String mensagem = args != null
                ? args.getString(ARG_MENSAGEM, "")
                : "";

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(24, 24, 24, 24);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        TextView textoTitulo = new TextView(requireContext());
        textoTitulo.setText(titulo);
        textoTitulo.setTextSize(24);
        textoTitulo.setGravity(Gravity.CENTER);

        TextView textoMensagem = new TextView(requireContext());
        textoMensagem.setText(mensagem);
        textoMensagem.setTextSize(16);
        textoMensagem.setGravity(Gravity.CENTER);
        textoMensagem.setPadding(0, 16, 0, 0);

        layout.addView(textoTitulo);
        layout.addView(textoMensagem);

        return layout;
    }
}
