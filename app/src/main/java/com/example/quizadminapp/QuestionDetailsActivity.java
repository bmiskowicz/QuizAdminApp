package com.example.quizadminapp;

import static com.example.quizadminapp.CategoryActivity.catList;
import static com.example.quizadminapp.CategoryActivity.selected_cat_index;
import static com.example.quizadminapp.QuestionActivity.questionsList;
import static com.example.quizadminapp.SetsActivity.selected_set_index;
import static com.example.quizadminapp.SetsActivity.setIDs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class QuestionDetailsActivity extends AppCompatActivity {

    private EditText question, optionA, optionB, optionC, optionD, answer;
    private Button addQuestionButton;
    private String questionString, aString, bString, cString, dString, answerString;
    private Dialog loadingDialog;
    private FirebaseFirestore firestore;
    private String action;
    private int questionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        Toolbar toolbar = findViewById(R.id.qdetails_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        question = findViewById(R.id.question);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        answer = findViewById(R.id.answer);
        addQuestionButton = findViewById(R.id.addQuestionB);

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");
        if(action.compareTo("EDIT") == 0){
            questionID = getIntent().getIntExtra("Q_ID", 0);
            loadData(questionID);
            getSupportActionBar().setTitle("Question " + String.valueOf(questionID + 1));
            addQuestionButton.setText("EDIT");
        }
        else{
            getSupportActionBar().setTitle("Question " + String.valueOf(questionsList.size() + 1));
            addQuestionButton.setText("ADD");
        }

        addQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionString = question.getText().toString();
                aString = optionA.getText().toString();
                bString = optionB.getText().toString();
                cString = optionC.getText().toString();
                dString = optionD.getText().toString();
                answerString = answer.getText().toString();

                if (questionString.isEmpty()){
                    question.setError("Enter question");
                    return;
                }
                else if (aString.isEmpty()){
                    optionA.setError("Enter option A");
                    return;
                }
                else if (bString.isEmpty()){
                    optionB.setError("Enter option B");
                    return;
                }
                else if (cString.isEmpty()){
                    optionC.setError("Enter option C");
                    return;
                }
                else if (dString.isEmpty()){
                    optionD.setError("Enter option D");
                    return;
                }
                else if (answerString.isEmpty()){
                    answer.setError("Enter correct answer");
                    return;
                }

                if(action.compareTo("EDIT") == 0){
                    editQuestion();
                }
                else{
                    addNewQuestion();
                }

            }
        });

    }

    private void addNewQuestion() {
        loadingDialog.show();

        Map<String, Object> questionData = new ArrayMap<>();

        questionData.put("QUESTION", questionString);
        questionData.put("A", aString);
        questionData.put("B", bString);
        questionData.put("C", cString);
        questionData.put("D", dString);
        questionData.put("ANSWER", answerString);

        String documentID = firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                .collection(setIDs.get(selected_set_index)).document().getId();

        firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                .collection(setIDs.get(selected_set_index)).document(documentID)
                .set(questionData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Map<String, Object> questionDocument = new ArrayMap<>();
                        questionDocument.put("Q" + String.valueOf(questionsList.size() + 1) + "_ID", documentID);
                        questionDocument.put("COUNT", String.valueOf(questionsList.size() + 1));

                        firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                                .collection(setIDs.get(selected_set_index)).document("QUESTIONS")
                                .update(questionDocument)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(QuestionDetailsActivity.this, "Question added successfully", Toast.LENGTH_SHORT);
                                        questionsList.add(new QuestionModel(documentID, questionString, aString, bString, cString, dString, Integer.valueOf(answerString)));
                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                                        loadingDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        loadingDialog.dismiss();
                    }
                });
    }

    private void loadData(int ID){
        question.setText(questionsList.get(ID).getQuestion());
        optionA.setText(questionsList.get(ID).getOptionA());
        optionB.setText(questionsList.get(ID).getOptionB());
        optionC.setText(questionsList.get(ID).getOptionC());
        optionD.setText(questionsList.get(ID).getOptionD());
        answer.setText(String.valueOf(questionsList.get(ID).getCorrectAnswer()));
    }

    private void editQuestion(){
        loadingDialog.show();
        Map<String, Object> questionData = new ArrayMap<>();
        questionData.put("QUESTION", questionString);
        questionData.put("A", aString);
        questionData.put("B", bString);
        questionData.put("C", cString);
        questionData.put("D", dString);
        questionData.put("ANSWER", answerString);

        firestore.collection("Quiz").document(catList.get(selected_cat_index).getId())
                .collection(setIDs.get(selected_set_index)).document(questionsList.get(questionID).getQuestionID())
                .set(questionData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(QuestionDetailsActivity.this, "Question updated successfully", Toast.LENGTH_SHORT);

                        questionsList.get(questionID).setQuestion(questionString);
                        questionsList.get(questionID).setOptionA(aString);
                        questionsList.get(questionID).setOptionB(bString);
                        questionsList.get(questionID).setOptionC(cString);
                        questionsList.get(questionID).setOptionD(dString);
                        questionsList.get(questionID).setCorrectAnswer(Integer.valueOf(answerString));

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                        loadingDialog.dismiss();
                    }
                });
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}