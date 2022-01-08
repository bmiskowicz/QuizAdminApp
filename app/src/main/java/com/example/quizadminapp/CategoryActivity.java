package com.example.quizadminapp;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    private RecyclerView cat_recycler_view;
    private Button addCatButton;
    public static List<CategoryModel> catList = new ArrayList<>();
    public static int selected_cat_index = 0;

    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addCategoryDialog;
    private EditText dialogCatName;
    private Button dialogAddB;
    private CategoryAdapter adapter;

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

        addCategoryDialog = new Dialog(CategoryActivity.this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog);
        addCategoryDialog.setCancelable(true);
        addCategoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogCatName = addCategoryDialog.findViewById(R.id.ac_cat_name);
        dialogAddB = addCategoryDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        addCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCatName.getText().clear();
                addCategoryDialog.show();
            }
        });

        dialogAddB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dialogCatName.getText().toString().isEmpty())  {
                    dialogCatName.setError("Enter category name");
                    return;
                }
                addNewCategory(dialogCatName.getText().toString());
            }
        });

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
                            String catid = doc.getString("CAT" + String.valueOf(i) + "_ID");

                            catList.add(new CategoryModel(catid, catName, "0", "1"));
                        }

                        adapter = new CategoryAdapter(catList);
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

    private void addNewCategory(String title){
        addCategoryDialog.dismiss();
        loadingDialog.show();

        Map<String, Object> categoryData = new ArrayMap<>();
        categoryData.put("NAME", title);
        categoryData.put("SETS", 0);
        categoryData.put("COUNTER", "1");

        String doc_id =  firestore.collection("Quiz").document().getId();
        firestore.collection("Quiz").document(doc_id)
                .set(categoryData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Map<String, Object> catDoc = new ArrayMap<>();
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1) + "_NAME", title);
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1) + "_ID", doc_id);
                        catDoc.put("COUNT", catList.size() + 1);

                        firestore.collection("Quiz").document("Categories")
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(CategoryActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();

                                        catList.add(new CategoryModel(doc_id, title, "0", "1"));

                                        adapter.notifyItemInserted(catList.size());

                                        loadingDialog.dismiss();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }


}