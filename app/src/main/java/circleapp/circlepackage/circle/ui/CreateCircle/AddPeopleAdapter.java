package circleapp.circlepackage.circle.ui.CreateCircle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import circleapp.circlepackage.circle.ViewModels.CreateCircle.AddPeopleInterface;

public class AddPeopleAdapter extends RecyclerView.Adapter<AddPeopleAdapter.ViewHolder> {
    private List<Contacts> contactsList;
    private List<Contacts> tempList = new ArrayList<>();;
    private Context context;
    private GlobalVariables globalVariables = new GlobalVariables();
    private List<String> usersList = new ArrayList<>();

    public AddPeopleAdapter(List<Contacts> contactsList, Context context) {
        this.contactsList = contactsList;
        this.context = context;
    }

    @NonNull
    @Override
    public AddPeopleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = View.inflate(context, R.layout.contact_items_listview,null);
        return new AddPeopleAdapter.ViewHolder(view);

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull AddPeopleAdapter.ViewHolder holder, int position) {
        for (Contacts i : this.contactsList){
            if (!tempList.contains(i)){
                tempList.add(i);
            }
        }

        Log.d("Adapter",tempList.toString());
        String temp_num = tempList.get(position).getPhn_number();
        String temp_name = tempList.get(position).getUid();

        if(globalVariables.getUsersList()!=null)
            usersList = globalVariables.getUsersList();
        List<String> tempUsersList = globalVariables.getTempUsersList();

        holder.person_name.setText(temp_name);
        holder.person_contact.setText(temp_num);

        holder.addPersonBtn.setOnClickListener(view ->{
            holder.addPersonBtn.setVisibility(View.GONE);
            holder.removePersonBtn.setVisibility(View.VISIBLE);
            usersList.add(tempUsersList.get(position));
            globalVariables.setUsersList(usersList);
        });
        holder.removePersonBtn.setOnClickListener(view ->{
            holder.removePersonBtn.setVisibility(View.GONE);
            holder.addPersonBtn.setVisibility(View.VISIBLE);
            usersList.remove(tempUsersList.get(position));
            globalVariables.setUsersList(usersList);
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
