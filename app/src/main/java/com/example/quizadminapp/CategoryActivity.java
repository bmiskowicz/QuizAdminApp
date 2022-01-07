package com.example.quizadminapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView cat_recycler_view;
    private Button addCatButton;
    public static List<String> catList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        cat_recycler_view = findViewById(R.id.cat_recycler);
        addCatButton = findViewById(R.id.addCatB);

        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycler_view.setLayoutManager(layoutManager);

        loadData();
    }

    private void loadData()
    {
        loadingDialog.show();

        catList.clear();

        firestore.collection("Quiz").document("Categories")
                .get().addOnCompleteListener((task) -> {

                if(task.isSuccessful())
                {
                    DocumentSnapshot doc = task.getResult();

                    if(doc.exists())
                    {
                        long count = (long)doc.get("COUNT");

                        for(int i=1; i <= count; i++)
                        {
                            String catName = doc.getString("CAT" + String.valueOf(i) + "_NAME");

                            catList.add(catName);
                        }

                        CategoryAdapter adapter = new CategoryAdapter(catList);
                        cat_recycler_view.setAdapter(adapter);
                    }
                    else
                    {
                        Toast.makeText(CategoryActivity.this,"No Category Document Exists!",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                }
                else
                {

                    Toast.makeText(CategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }

                loadingDialog.dismiss();

        });

    }

}