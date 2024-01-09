package com.example.sspgcek;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.sspgcek.Adapters.ChatAdapter;
import com.example.sspgcek.Models.ChatsModel;
import com.example.sspgcek.databinding.ActivityMainBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    String feeStructureURL="https://firebasestorage.googleapis.com/v0/b/sspgcek.appspot.com/o/IMG_20240103_224134.pdf?alt=media&token=a8d23079-194c-48a5-806a-e8d8c746939c";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(Color.parseColor("#36c5fe"));

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Student Support System(GCEK)");

        registerForContextMenu(binding.chats);
        firebaseAuth=FirebaseAuth.getInstance();
        id= Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        firebaseDatabase=FirebaseDatabase.getInstance();

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
            if (userinput.equals("କଲେଜ ୱେବସାଇଟ୍")) {
                openURL(userinput,"https://www.gcekbpatna.ac.in/");
            } else if (userinput.equals("ଅନଲାଇନ୍ ଦେୟ")) {
                openURL(userinput,"https://www.gcekbpatna.ac.in/billpayment/");
            } else if (userinput.equals("କଲେଜ ଅଧ୍ୟୟନ ଦେୟ ବିବରଣୀ")) {
                new DownloadFileFromURL().execute(feeStructureURL,"/feedetails.pdf");
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        //Background work here
                        int count;
                        try {
                            URL url = new URL(feeStructureURL);
                            URLConnection conection = url.openConnection();
                            conection.connect();
                            int lenghtOfFile = conection.getContentLength();
                            InputStream input = new BufferedInputStream(url.openStream(), 8192);
                            OutputStream output = Files.newOutputStream(Paths.get(Environment.getExternalStorageDirectory().toString() + feeStructureURL));
                            byte[] data = new byte[1024];
                            long total = 0;
                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }
                            output.flush();
                            output.close();
                            input.close();
                        } catch (Exception e) {
                            Log.e("Error: ", Objects.requireNonNull(e.getMessage()));
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //UI Thread work here
                                Toast.makeText(MainActivity.this, "File downloaded", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
                            }
                        });
                    }
                });
            } else {
                if (userinput.matches("^[a-zA-Z][a-zA-Z\\s]+$")) {
                    String translateduserinput=translateText(userinput);
                    getResult(translateduserinput);
                } else {
                    getResult(userinput);
                }
            }
            binding.userquery.setText("");
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
        addChat(userinput,USER_KEY);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String response1 = jsonObject.getString("response");
                addChat(response1,BOT_KEY);
            } catch (JSONException e) {
                addChat(e.getMessage(),BOT_KEY);
            }
        }, error -> {
                    addChat("Server error.",BOT_KEY);
//                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
    }
    private void addChat(String data, String msgKey){
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        list.add(new ChatsModel(data, msgKey,time,name));
        chatAdapter.notifyItemInserted(list.size()-1);
        binding.chats.scrollToPosition(list.size()-1);
        databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
        databaseReference.setValue(new ChatsModel(data,msgKey,time,name));
    }
    private String translateText(String input){
        String targetLang = "or";
        Translate translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().getService();
        Translation translation = translate.translate(input, Translate.TranslateOption.targetLanguage(targetLang));
        return translation.getTranslatedText();
    }
    private void openURL(String userinput,String url) {
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault()).format(System.currentTimeMillis());
        String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        list.add(new ChatsModel(userinput, "user",time,name));
        chatAdapter.notifyItemInserted(list.size()-1);
        binding.chats.scrollToPosition(list.size()-1);
        databaseReference= firebaseDatabase.getReference().child("Users").child(id).child("Chats").child(name);
        databaseReference.setValue(new ChatsModel(userinput,"user",time,name));
        Intent urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(urlIntent);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.signout) {
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, SigninActivity.class));
            finish();
        } else if (item.getItemId()==R.id.download) {
            startActivity(new Intent(MainActivity.this, DownloadActivity.class));
        }
        return (super.onOptionsItemSelected(item));
    }
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (Objects.equals(item.getTitle(), "Share")) {
            chatAdapter.shareText(item.getGroupId());
//            Toast.makeText(MainActivity.this, "Chat Shared", Toast.LENGTH_SHORT).show();
        } else if (Objects.equals(item.getTitle(), "Delete")) {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setMessage("Delete message?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        chatAdapter.removeItem(item.getGroupId());
                        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).show();
        }
        return super.onContextItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}