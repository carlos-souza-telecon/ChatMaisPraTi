package br.com.rostirolla.chatmaisprati;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class Notifications {

    public static void subscribeToTopic(String topic) {
        FirebaseMessaging
                .getInstance()
                .subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Notifications",
                                    "Inscreveu-se no tópico " + topic);
                        } else {
                            Log.d("Notifications",
                                    "Erro ao inscrever-se no tópico " + topic);
                        }
                    }
                });
    }

}
