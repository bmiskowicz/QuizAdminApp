package com.example.quizadminapp;

import static com.example.quizadminapp.CategoryActivity.catList;
import static com.example.quizadminapp.CategoryActivity.selectedCatId;
import static com.example.quizadminapp.SetsActivity.selected_set_index;
import static com.example.quizadminapp.SetsActivity.setIDs;

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
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView questionsView;
    private Button questionButton;
    public static List<QuestionModel> questionsList = new ArrayList<>();
    private QuestionAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        questionsView = findViewById(R.id.question_recycler);
        questionButton = findViewById(R.id.addQuestionB);

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questionsView.setLayoutManager(layoutManager);

        firestore = FirebaseFirestore.getInstance();

        loadQuestions();

    }

    private void loadQuestions() {
        questionsList.clear();

        loadingDialog.show();

        firestore.collection("Quiz").document(catList.get(selectedCatId).getId())
                .collection(setIDs.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String, QueryDocumentSnapshot> documentList = new ArrayMap<>();

                        for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                            documentList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot questionListDocument = documentList.get("QUESTIONS");

                        String count = questionListDocument.getString("COUNT");
                        for(int i=0; i< Integer.valueOf(count); i++){
                            String questionID = questionListDocument.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot questionDocument = documentList.get(questionID);
                            questionsList.add(new QuestionModel(
                                    questionID,
                                    questionDocument.getString("QUESTION"),
                                    questionDocument.getString("A"),
                                    questionDocument.getString("B"),
                                    questionDocument.getString("C"),
                                    questionDocument.getString("D"),
                                    Integer.parseInt(questionDocument.getString("ANSWER"))
                            ));
                        }

                        adapter = new QuestionAdapter(questionsList);
                        questionsView.setAdapter(adapter);

                        loadingDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });

    }
}