package circleapp.circlepackage.circle.ui.CreateCircle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.List;

import circleapp.circlepackage.circle.Model.ObjectModels.Contacts;
import circleapp.circlepackage.circle.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class AddpeopleCircluarAdapter extends RecyclerView.Adapter<AddpeopleCircluarAdapter.ViewHolder> {
    private Context context;
    private List<String> contactsList;

    public AddpeopleCircluarAdapter(Context context, List<String> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    @NonNull
    @Override
    public AddpeopleCircluarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.contact_item_circlur_view,null);
        return new AddpeopleCircluarAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddpeopleCircluarAdapter.ViewHolder holder, int position) {
        String name = contactsList.get(position);
        holder.setContactview(name);
    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;

        public ViewHolder(View view) {
            super(view);
            profilePic = view.findViewById(R.id.contact_circular_logo);

        }
        public void setContactview(String name){
            char firstLetter = name.charAt(0);
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            int color = generator.getColor(name);
            TextDrawable drawable = TextDrawable.builder()
                    .buildRound(firstLetter+"",color);
            profilePic.setBackground(drawable);
        }
    }
}
