package br.com.rostirolla.chatmaisprati;

public class Chat {

    public String dataHora, mensagem, usuario;
    private String key;

    public Chat() {}

    public Chat(String dataHora, String mensagem, String usuario) {
        this.dataHora = dataHora;
        this.mensagem = mensagem;
        this.usuario = usuario;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
