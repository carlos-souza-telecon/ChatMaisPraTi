package br.com.rostirolla.chatmaisprati;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText textEmailLogin, textSenhaLogin;
    Button btnLogar, btnCadastrar, btnEsqueciMinhaSenha;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        textEmailLogin = findViewById(R.id.textEmailLogin);
        textSenhaLogin = findViewById(R.id.textSenhaLogin);
        btnLogar = findViewById(R.id.btnLogar);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnEsqueciMinhaSenha = findViewById(R.id.btnEsqueciMinhaSenha);

        btnLogar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logarUsuario();
            }
        });

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cadastrarUsuario();
            }
        });

        btnEsqueciMinhaSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                esqueciMinhaSenha();
            }
        });

        verificarLogin();
    }

    private void verificarLogin() {
        FirebaseUser usuario = auth.getCurrentUser();
        if (usuario != null) {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void esqueciMinhaSenha() {
        String email = textEmailLogin.getText().toString();
        if (email.equals("")) {
            textEmailLogin.setError("Informe seu e-mail!");
        } else {
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,
                                        "Verifique seu e-mail para alterar sua senha!",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Falha ao resetar a senha:\n" + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void logarUsuario() {
        if (!verificarCampos()) {
            String email = textEmailLogin.getText().toString();
            String senha = textSenhaLogin.getText().toString();

            auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,
                                        "Login bem-sucedido!",
                                        Toast.LENGTH_SHORT).show();
                                verificarLogin();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Falha ao logar:\n" + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void cadastrarUsuario() {
        if (!verificarCampos()) {
            String email = textEmailLogin.getText().toString();
            String senha = textSenhaLogin.getText().toString();

            auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this,
                                        "Usuário criado com sucesso!",
                                        Toast.LENGTH_SHORT).show();
                                verificarLogin();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Falha ao cadastrar:\n" + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    private boolean verificarCampos() {
        String email = textEmailLogin.getText().toString();
        String senha = textSenhaLogin.getText().toString();
        boolean erro = false;

        if (email.equals("")) {
            textEmailLogin.setError("Preencha o e-mail!");
            erro = true;
        }
        if (senha.equals("") || senha.length() < 6) {
            textSenhaLogin.setError("A senha deve ter no mínimo 6 caracteres!");
            erro = true;
        }

        return erro;
    }
}