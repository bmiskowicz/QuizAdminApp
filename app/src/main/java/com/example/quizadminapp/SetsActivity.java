package com.example.quizadminapp;

import static com.example.quizadminapp.CategoryActivity.catList;
import static com.example.quizadminapp.CategoryActivity.selectedCatId;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SetsActivity extends AppCompatActivity {

    private RecyclerView setsView;
    private Button addSetB;
    private  SetsAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;


    public static List<String> setIDs = new ArrayList<>();
    public static int selected_set_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        Toolbar toolbar = findViewById(R.id.sa_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");

        setsView = findViewById(R.id.sets_recycler);
        addSetB = findViewById(R.id.addSetB);


        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        addSetB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSet();
            }
        });

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();
    }


    private void loadSets(){
        setIDs.clear();

        loadingDialog.show();

        firestore.collection("Quiz").document(catList.get(selectedCatId).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        long noOfSets = (long)documentSnapshot.get("SETS");
                        for(int i=1; i<=noOfSets; i++){
                            setIDs.add(documentSnapshot.getString("SET" + String.valueOf(i) + "_ID"));
                        }

                        catList.get(selectedCatId).setSetCounter(documentSnapshot.getString("COUNTER"));
                        catList.get(selectedCatId).setNoOfSets(String.valueOf(noOfSets));

                        adapter = new SetsAdapter(setIDs);
                        setsView.setAdapter(adapter);

                        loadingDialog.dismiss();;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();;
                    }
                });


    }


    private void addNewSet() {
        loadingDialog.show();

        String currentCatID = catList.get(selectedCatId).getId();
        String currentCounter = catList.get(selectedCatId).getSetCounter();

        Map<String, Object> questionsData = new ArrayMap<>();
        questionsData.put("COUNT", "0");

        firestore.collection("Quiz").document(catList.get(selectedCatId).getId())
                .collection(currentCounter).document("QUESTIONS")
                .set(questionsData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String, Object> categoryDocument = new ArrayMap<>();
                        categoryDocument.put("COUNTER", String.valueOf(Integer.valueOf(currentCounter)+1) );
                        categoryDocument.put("SET" + String.valueOf(setIDs.size() + 1) + "_ID", currentCounter);
                        categoryDocument.put("SETS", setIDs.size() +1);

                        firestore.collection("Quiz").document(currentCatID)
                                .update(categoryDocument)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(SetsActivity.this,"Set added successfully", Toast.LENGTH_SHORT);
                                        setIDs.add(currentCounter);
                                        catList.get(selectedCatId).setNoOfSets(String.valueOf(setIDs.size()));
                                        catList.get(selectedCatId).setSetCounter(String.valueOf(Integer.valueOf(currentCounter) + 1));

                                        adapter.notifyItemInserted(setIDs.size());
                                        loadingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }
}