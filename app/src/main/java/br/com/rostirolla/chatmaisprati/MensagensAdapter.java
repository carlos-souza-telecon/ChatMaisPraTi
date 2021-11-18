package br.com.rostirolla.chatmaisprati;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;

public class MensagensAdapter extends BaseAdapter {

    Context context;
    ArrayList<Chat> mensagens;
    String usuarioLogado;

    public MensagensAdapter(Context context, ArrayList<Chat> mensagens, String usuarioLogado) {
        this.context = context;
        this.mensagens = mensagens;
        this.usuarioLogado = usuarioLogado;
    }

    @Override
    public int getCount() {
        return mensagens.size();
    }

    @Override
    public Object getItem(int i) {
        return mensagens.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public boolean autorDaMensagem(String usuario) {
        return usuario.equals(usuarioLogado);
    }

    public boolean autorDaMensagem(int i, String usuario) {
        return mensagens.get(i).usuario.equals(usuario);
    }

    public boolean mensagemDoUsuarioLogado(int i) {
        return mensagens.get(i).usuario.equals(usuarioLogado);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater
                    .from(context)
                    .inflate(R.layout.adapter_mensagens,
                            viewGroup,
                            false);
        }

        TextView textUsuarioMensagem = view.findViewById(R.id.textUsuarioMensagem);
        TextView textMensagem = view.findViewById(R.id.textMensagem);
        TextView textDataHoraMensagem = view.findViewById(R.id.textDataHoraMensagem);
        CardView cardMensagem = view.findViewById(R.id.cardMensagem);

        Chat chat = mensagens.get(i);
        textUsuarioMensagem.setText(
                autorDaMensagem(chat.usuario)
                        ? "Você"
                        : chat.usuario);
        textMensagem.setText(chat.mensagem);
        textDataHoraMensagem.setText(chat.dataHora);
        cardMensagem.setCardBackgroundColor(
                autorDaMensagem(chat.usuario)
                        ? view.getResources().getColor(R.color.laranja_logo_claro)
                        : view.getResources().getColor(R.color.azul_logo_claro)
        );

        return view;
    }
}
