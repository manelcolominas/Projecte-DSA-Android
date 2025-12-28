package dsa.upc.edu.listapp;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dsa.upc.edu.listapp.github.Item;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<Item> values;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public TextView itemIcon;
        public TextView itemQuantity;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            itemName = (TextView) v.findViewById(R.id.itemName);
            itemIcon = (TextView) v.findViewById(R.id.itemIcon);
            itemQuantity = (TextView) v.findViewById(R.id.itemQuantity);
        }
    }

    public void setData(List<Item> myDataset) {
        values = myDataset;
        notifyDataSetChanged();
    }

    public MyAdapter() {
        values = null;
    }

    public MyAdapter(List<Item> myDataset) {
        values = myDataset;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_inventory, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Item item = values.get(position);
        final String name = item.getName();
        holder.itemName.setText(name);
        holder.itemIcon.setText(item.getEmoji());
        holder.itemQuantity.setText("x" + item.getQuantity());
    }

    @Override
    public int getItemCount() {
        if (values == null) return 0;
        return values.size();
    }
}
