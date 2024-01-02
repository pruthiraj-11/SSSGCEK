package com.example.sspgcek;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sspgcek.Adapters.ChatAdapter;
import com.example.sspgcek.Models.ChatsModel;
import com.example.sspgcek.databinding.ActivityMainBinding;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<ChatsModel> list;
    ChatAdapter chatAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    private long pressedTime;
    String API_KEY="";
    String id;
    String url = "https://pruthiraj2002routray-c88477d6-455a-467f-ba6b-1c680d998cf5.socketxp.com/predict";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.parseColor("#36c5fe"));
//        if (! Python.isStarted()) {
//            Python.start(new AndroidPlatform(getApplicationContext()));
//        }
//        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        firebaseAuth=FirebaseAuth.getInstance();
        id= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        firebaseDatabase=FirebaseDatabase.getInstance();
//        databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);

        list=new ArrayList<>();
        chatAdapter=new ChatAdapter(list,getApplicationContext());

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        binding.chats.setLayoutManager(linearLayoutManager);
        binding.chats.setAdapter(chatAdapter);
        firebaseDatabase.getReference().child("Users").child(id).child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatsModel chatsModel=dataSnapshot.getValue(ChatsModel.class);
                    list.add(chatsModel);
                }
                chatAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        binding.send.setOnClickListener(v -> {
            String userinput=binding.userquery.getText().toString().trim();
            if(userinput.isEmpty()){
                Toast.makeText(getApplicationContext(),"Query can't be blank",Toast.LENGTH_SHORT).show();
                return;
            }
            if (userinput.equals("signout")) {
                firebaseAuth.signOut();
                startActivity(new Intent(MainActivity.this, SigninActivity.class));
                finish();
            } else if (userinput.equals("କଲେଜ ୱେବସାଇଟ୍")) {
                String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
                list.add(new ChatsModel(userinput, "user",time));
                chatAdapter.notifyDataSetChanged();
                String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
                databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
                databaseReference.setValue(new ChatsModel(userinput,"user",time));
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gcekbpatna.ac.in/"));
                startActivity(urlIntent);
            } else {
//                String translateduserinput=translateText(userinput);
                getResult(userinput);
                binding.userquery.setText("");
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (pressedTime + 2000 > System.currentTimeMillis()) {
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
                }
                pressedTime = System.currentTimeMillis();
            }
        });
    }

    private void getResult(String userinput) {
        String USER_KEY = "user";
        String BOT_KEY = "bot";
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        list.add(new ChatsModel(userinput, USER_KEY,time));
        chatAdapter.notifyDataSetChanged();
        databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
        databaseReference.setValue(new ChatsModel(userinput,USER_KEY,time));
//        if(list.size()==1){
//            chatAdapter.notifyDataSetChanged();
//        } else {
//            chatAdapter.notifyItemInserted(list.size()-1);
//        }
//        Python python=Python.getInstance();
//        final PyObject pyObject=python.getModule("res");
//
//        PyObject object=null;
//        object=pyObject.callAttr("backend",userinput);
//
//        Toast.makeText(getApplicationContext(),object.toString(),Toast.LENGTH_LONG).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String response1 = jsonObject.getString("response");
                            String time1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
                            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
                            list.add(new ChatsModel(response1, BOT_KEY,time1));
                            databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
                            databaseReference.setValue(new ChatsModel(response1,BOT_KEY,time1));
                        } catch (JSONException e) {
                            String time1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
                            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
                            list.add(new ChatsModel(e.getMessage(), BOT_KEY,time1));
                            databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
                            databaseReference.setValue(new ChatsModel(e.getMessage(),BOT_KEY,time1));
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String time1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
                        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
                        list.add(new ChatsModel(error.getMessage(), BOT_KEY,time1));
                        databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
                        databaseReference.setValue(new ChatsModel(error.getMessage(),BOT_KEY,time1));
//                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String,String>();
                params.put("input_query",userinput);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        queue.add(stringRequest);

        chatAdapter.notifyDataSetChanged();
//        if(list.size()==1){
//            chatAdapter.notifyDataSetChanged();
//        } else {
//            chatAdapter.notifyItemInserted(list.size()-1);
//        }
    }

    private String translateText(String input){
        String targetLang = "or";
        Translate translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().getService();
        Translation translation = translate.translate(input, Translate.TranslateOption.targetLanguage(targetLang));
        return translation.getTranslatedText();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}