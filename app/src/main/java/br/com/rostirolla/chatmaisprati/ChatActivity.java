package br.com.rostirolla.chatmaisprati;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.cloudmessaging.CloudMessage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.NotificationParams;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;
import com.google.firebase.messaging.RemoteMessageCreator;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ListView listMensagens;
    FloatingActionButton fabEnviarMensagem;
    EditText textMensagemDigitada;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference refChat;
    RequestQueue queue;
    MensagensAdapter adapter;
    ArrayList<Chat> mensagens = new ArrayList<Chat>();
    String usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        queue = Volley.newRequestQueue(ChatActivity.this);
        db = FirebaseDatabase.getInstance();
        refChat = db.getReference("chat");
        auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuario = auth.getCurrentUser();
                if (usuario != null) {
                    usuarioLogado = usuario.getEmail();
                    getSupportActionBar().setTitle("Chat");
                    getSupportActionBar().setSubtitle("Logado como: " + usuarioLogado);
                    inicializarPesquisaDeMensagens();
                    Notifications.subscribeToTopic("geral");
                } else {
                    Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        listMensagens = findViewById(R.id.listMensagens);
        textMensagemDigitada = findViewById(R.id.textMensagemDigitada);
        fabEnviarMensagem = findViewById(R.id.fabEnviarMensagem);

        listMensagens.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listMensagens.setStackFromBottom(true);
    }

    private void inicializarPesquisaDeMensagens() {
        adapter = new MensagensAdapter(ChatActivity.this, mensagens, usuarioLogado);
        listMensagens.setAdapter(adapter);
        refChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mensagens.clear();
                for (DataSnapshot dataChat : snapshot.getChildren()) {
                    Chat chat = dataChat.getValue(Chat.class);
                    chat.setKey(dataChat.getKey());
                    mensagens.add(chat);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this,
                        "Falha ao pesquisar as mensagens:\n" + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
        fabEnviarMensagem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensagem();
            }
        });

        listMensagens.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapter.mensagemDoUsuarioLogado(i)) {
                    refChat.child(mensagens.get(i).getKey()).removeValue();
                }
                return true;
            }
        });
    }

    private void enviarMensagem() {
        String mensagem = textMensagemDigitada.getText().toString();
        if (mensagem.equals("")) {
            textMensagemDigitada.setError("Digite a mensagem!");
        } else {
            Chat chat = new Chat();
            chat.usuario = usuarioLogado;
            chat.mensagem = mensagem;
            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "HH:mm dd/MM/yyyy",
                            Locale.getDefault());
            chat.dataHora = sdf.format(new Date());
            refChat.push()
                    .setValue(chat)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            textMensagemDigitada.setText("");
                            enviarNotificacao(mensagem);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this,
                            "Falha ao enviar a mensagem!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void enviarNotificacao(String mensagem) {
        try {
            String chave = "AAAA8z-2kGY:APA91bEsaFRHuoIQUkGSTYGxbYwRPOQaEgZDwORrEJ-uQI_4JAZEjMQS_-rnH-G-3wfZtPznMzWe71ZfQ0yyHv_qAOqc-MdNbzVM0541ODP8l4d8Vc-ACCWWwckND0v9AZwSvmsnVCW9";
            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject notification = new JSONObject();
            notification.put("body", mensagem);
            notification.put("title", usuarioLogado);
            JSONObject message = new JSONObject();
            message.put("to", "/topics/geral");
            message.put("notification", notification);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    message,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("NotificaçãoEnvio",
                                    "Notificação de mensagem enviada!");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("NotificaçãoEnvio",
                                    "Erro ao enviar notificação de mensagem enviada (volley):\n"
                                            + error.getMessage());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key= " + chave);
                    return headers;
                }
            };
            queue.add(request);
        } catch (Exception e) {
            Log.e("NotificaçãoEnvio",
                    "Erro ao enviar notificação de mensagem enviada:\n"
                            + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sair:
                sair();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sair() {
        auth.signOut();
    }
}