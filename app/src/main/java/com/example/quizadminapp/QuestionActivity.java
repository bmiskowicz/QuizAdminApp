package com.example.quizadminapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    private RecyclerView questionsView;
    private Button questionButton;
    public static List<QuestionModel> questionsList = new ArrayList<>();
    private QuestionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        Toolbar toolbar = findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        questionsView = findViewById(R.id.question_recycler);
        questionButton = findViewById(R.id.addQuestionB);

        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questionsView.setLayoutManager(layoutManager);

        loadQuestions();

    }

    private void loadQuestions() {
        questionsList.clear();

        questionsList.add(new QuestionModel("1", "Q1", "A", "B", "C", "D", 2));
        questionsList.add(new QuestionModel("2", "Q2", "A", "B", "C", "D", 2));
        questionsList.add(new QuestionModel("3", "Q3", "A", "B", "C", "D", 2));

        adapter = new QuestionAdapter(questionsList);
        questionsView.setAdapter(adapter);
    }
}