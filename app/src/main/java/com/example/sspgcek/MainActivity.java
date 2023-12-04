package com.example.sspgcek;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.sspgcek.Adapters.ChatAdapter;
import com.example.sspgcek.Models.ChatsModel;
import com.example.sspgcek.databinding.ActivityMainBinding;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<ChatsModel> list;
    ChatAdapter chatAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String API_KEY="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.stausbarcolor));

        String android_device_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
//        try {
//            ApplicationInfo applicationInfo=getApplicationContext().getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA);
//            Object value = applicationInfo.metaData.get("apikey");
//            if (value != null) {
//                API_KEY = value.toString();
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//            Toast.makeText(MainActivity.this, e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
//        }
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference(android_device_id);

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(getApplicationContext()));
        }

        list=new ArrayList<>();
        chatAdapter=new ChatAdapter(list,getApplicationContext());

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.chats.setLayoutManager(linearLayoutManager);
        binding.chats.setAdapter(chatAdapter);

        binding.send.setOnClickListener(v -> {
            String userinput=binding.userquery.getText().toString();
            databaseReference.setValue(userinput);
            if(userinput.isEmpty()){
                Toast.makeText(getApplicationContext(),"Query can't be blank",Toast.LENGTH_SHORT).show();
                return;
            }
            getResult(userinput);
//            translateText(userinput);
            binding.userquery.setText("");
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void getResult(String userinput) {
        String USER_KEY = "user";
        list.add(new ChatsModel(userinput, USER_KEY));
        if(list.size()==1){
            chatAdapter.notifyDataSetChanged();
        } else {
            chatAdapter.notifyItemInserted(list.size()-1);
        }
        Python python=Python.getInstance();
        final PyObject pyObject=python.getModule("res");

        PyObject object=null;
        object=pyObject.callAttr("backend",userinput);

        Toast.makeText(getApplicationContext(),object.toString(),Toast.LENGTH_LONG).show();

        String BOT_KEY = "bot";
        list.add(new ChatsModel(object.toString(), BOT_KEY));
        if(list.size()==1){
            chatAdapter.notifyDataSetChanged();
        } else {
            chatAdapter.notifyItemInserted(list.size()-1);
        }
    }

    private void translateText(String input){
        String targetLang = "or";
        Translate translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().getService();
        Translation translation = translate.translate(input, Translate.TranslateOption.targetLanguage(targetLang));
        String translatedText = translation.getTranslatedText();
        removePunctuationAndLowercase(translatedText);
    }

    private void removePunctuationAndLowercase(String sentence) {
        String withoutPunctuation = sentence.replaceAll("[^a-zA-Z0-9\\s]", "");
        getResult(withoutPunctuation.toLowerCase());
    }

}