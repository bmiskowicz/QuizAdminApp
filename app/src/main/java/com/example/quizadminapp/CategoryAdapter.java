package com.example.quizadminapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public List<CategoryModel> catList;
    public CategoryAdapter(List<CategoryModel> catList) {
        this.catList = catList;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i ) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cat_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder ViewHolder, int pos) {
        String title = catList.get(pos).getName();

        ViewHolder.setData(title, pos, this);
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView deleteButton;
        private Dialog loadingDialog, updateDialog;
        private EditText updateCatName;
        private Button updateCatButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            catName =  itemView.findViewById(R.id.catName);
            deleteButton = itemView.findViewById(R.id.catDelB);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            updateDialog = new Dialog(itemView.getContext());
            updateDialog.setContentView(R.layout.edit_category_dialog);
            updateDialog.setCancelable(true);
            updateDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            updateCatName = updateDialog.findViewById(R.id.ec_cat_name);
            updateCatButton = updateDialog.findViewById(R.id.ec_add_btn);
        }

        private void setData(String title, int pos, CategoryAdapter adapter) {
            catName.setText(title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CategoryActivity.selected_cat_index = pos;
                    Intent intent = new Intent(itemView.getContext(), SetsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateCatName.setText(catList.get(pos).getName());
                    updateDialog.show();
                    return false;
                }
            });

            updateCatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(updateCatName.getText().toString().isEmpty()){
                        updateCatName.setError("Entry category name");
                        return;
                    }
                    updateCategory(updateCatName.getText().toString(), pos, itemView.getContext(), adapter);
                }
            });


            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete category")
                            .setMessage("Do you want to delete this category?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCategory(pos, itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }

        private void deleteCategory(int id, Context context, CategoryAdapter adapter){
            loadingDialog.show();

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            Map<String, Object> categoryDocument = new ArrayMap<>();

            int index = 1;
            String deleteId = "";

            for (int i = 0; i<catList.size(); i++)
            {
                if(i!=id)
                {
                    categoryDocument.put("CAT" + String.valueOf(index) + "_ID", catList.get(i).getId());
                    categoryDocument.put("CAT" + String.valueOf(index) + "_NAME", catList.get(i).getName());
                    index++;
                }
                else
                {
                    deleteId = catList.get(i).getId();
                }
            }


            categoryDocument.put("COUNT", index-1);

            String finalDeleteId1 = deleteId;
            firestore.collection("Quiz").document(deleteId)
                    .collection(String.valueOf(deleteId)).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch = firestore.batch();
                            for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                                batch.delete(doc.getReference());
                            }
                            batch.commit();
                            firestore.collection("Quiz").document(finalDeleteId1)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            firestore.collection("Quiz").document("Categories")
                                                    .set(categoryDocument)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                                                            CategoryActivity.catList.remove(id);
                                                            adapter.notifyDataSetChanged();
                                                            loadingDialog.dismiss();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            loadingDialog.dismiss();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
        }

        private void updateCategory(String catNewName, int pos, Context context, CategoryAdapter adapter) {
            updateDialog.dismiss();
            loadingDialog.show();
            Map<String, Object> catData = new ArrayMap<>();
            catData.put("NAME", catNewName);

            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Quiz").document(catList.get(pos).getId())
                    .update(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Map<String, Object> catDoc  = new ArrayMap<>();
                            catDoc.put("CAT" + String.valueOf(pos+1) + "_NAME", catNewName);
                            firestore.collection("Quiz").document("Categories")
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(context, "Category name changed successfully", Toast.LENGTH_SHORT).show();
                                            CategoryActivity.catList.get(pos).setName(catNewName);
                                            adapter.notifyDataSetChanged();

                                            loadingDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });

        }


    }

}