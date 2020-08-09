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
import java.util.List;

import circleapp.circlepackage.circle.Model.ObjectModels.Contacts;
import circleapp.circlepackage.circle.R;
import circleapp.circlepackage.circle.Utils.GlobalVariables;

public class AddPeopleAdapter extends RecyclerView.Adapter<AddPeopleAdapter.ViewHolder> {
    private List<Contacts> contactsList = new ArrayList<>();
    private Context context;
    private GlobalVariables globalVariables = new GlobalVariables();

    public AddPeopleAdapter(List<Contacts> contactsList, Context context) {
        this.contactsList = contactsList;
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
        String temp_num = this.contactsList.get(position).getPhn_number();
        String temp_name = this.contactsList.get(position).getUid();

        holder.person_name.setText(temp_name);
        holder.person_contact.setText(temp_num);

        holder.addPersonBtn.setOnClickListener(view ->{
            holder.addPersonBtn.setVisibility(View.GONE);
            holder.removePersonBtn.setVisibility(View.VISIBLE);
        });
        holder.removePersonBtn.setOnClickListener(view ->{
            holder.removePersonBtn.setVisibility(View.GONE);
            holder.addPersonBtn.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView person_name, person_contact;
        private Button addPersonBtn, removePersonBtn;
        public ViewHolder(View view) {
            super(view);
            person_name = view.findViewById(R.id.name_textView);
            person_contact = view.findViewById(R.id.phone_number);
            addPersonBtn = view.findViewById(R.id.add_person_btn);
            removePersonBtn = view.findViewById(R.id.remove_person_btn);
        }
    }

}
