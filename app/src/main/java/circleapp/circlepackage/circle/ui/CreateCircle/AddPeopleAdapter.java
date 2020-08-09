package circleapp.circlepackage.circle.ui.CreateCircle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class AddPeopleAdapter extends RecyclerView.Adapter<AddPeopleAdapter.ViewHolder> {
    private ArrayList<String> temp_num,temp_name;
    private Context context;
    private GlobalVariables globalVariables = new GlobalVariables();

    public AddPeopleAdapter(ArrayList<String> temp_num, Context context) {
        this.temp_num = temp_num;
        this.context = context;
    }

    @NonNull
    @Override
    public AddPeopleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_items_listview, viewGroup, false);
        return new AddPeopleAdapter.ViewHolder(view);    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull AddPeopleAdapter.ViewHolder holder, int position) {
        String temp_num = this.temp_num.get(position);
        String temp_name = this.temp_name.get(position);

        holder.person_name.setText(temp_name);
        holder.person_contact.setText(temp_num);

        holder.state.setOnClickListener(view ->{
            holder.state.setText("Remove");
            holder.state.setBackgroundColor(R.color.md_red_A100);
        });

    }

    @Override
    public int getItemCount() {
        return temp_num.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView person_name, person_contact;
        private Button state;
        public ViewHolder(View view) {
            super(view);
            person_name = view.findViewById(R.id.name_textView);
            person_contact = view.findViewById(R.id.phone_number);
            state = view.findViewById(R.id.state_btn);
        }
    }

}
