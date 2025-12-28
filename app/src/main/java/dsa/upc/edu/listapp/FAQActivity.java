package dsa.upc.edu.listapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dsa.upc.edu.listapp.github.API;
import dsa.upc.edu.listapp.github.EETACBROSSystemService;
import dsa.upc.edu.listapp.github.FAQ;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FAQActivity extends AppCompatActivity {
    private static final String TAG = "FAQActivity";
    private EETACBROSSystemService api;
    private RecyclerView faqRecycler;
    private FAQAdapter adapter;
    private List<FAQ> faqList = new ArrayList<>();
    private Button profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        api = API.getGithub();

        faqRecycler = findViewById(R.id.faqRecycler);
        faqRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FAQAdapter(faqList);
        faqRecycler.setAdapter(adapter);

        profileBtn = findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(v -> finish()); // Go back to profile (or previous activity)

        loadFAQs();
    }

    private void loadFAQs() {
        api.getFAQs().enqueue(new Callback<List<FAQ>>() {
            @Override
            public void onResponse(Call<List<FAQ>> call, Response<List<FAQ>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    faqList.clear();
                    faqList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(FAQActivity.this, "Failed to load FAQs", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FAQ>> call, Throwable t) {
                Log.e(TAG, "Error loading FAQs", t);
                Toast.makeText(FAQActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {
        private List<FAQ> faqs;

        public FAQAdapter(List<FAQ> faqs) {
            this.faqs = faqs;
        }

        @Override
        public FAQViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
            return new FAQViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FAQViewHolder holder, int position) {
            FAQ faq = faqs.get(position);
            holder.bind(faq);
        }

        @Override
        public int getItemCount() {
            return faqs.size();
        }

        class FAQViewHolder extends RecyclerView.ViewHolder {
            private TextView questionText;
            private TextView answerText;
            private boolean isExpanded = false;

            public FAQViewHolder(View itemView) {
                super(itemView);
                questionText = itemView.findViewById(R.id.questionText);
                answerText = itemView.findViewById(R.id.answerText);

                itemView.setOnClickListener(v -> {
                    isExpanded = !isExpanded;
                    answerText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                });
            }

            public void bind(FAQ faq) {
                questionText.setText(faq.getQuestion());
                answerText.setText(faq.getAnswer());
                answerText.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            }
        }
    }
}
