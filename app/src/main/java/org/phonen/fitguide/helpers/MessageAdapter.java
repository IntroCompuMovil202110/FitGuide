package org.phonen.fitguide.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;

import org.phonen.fitguide.R;
import org.phonen.fitguide.model.Message;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {

    private ArrayList<Message> mList;
    private Context mContext;
    private int resource_receiver;
    private  int resource_sender;
    private FirebaseAuth mAuth =FirebaseAuth.getInstance();
    Message ms;
    public  class  ViewHolder
    {
        TextView message;
        TextView name;
        TextView date;
        TextView time;
    }

    public MessageAdapter(@NonNull Context context, int receiver, int sender,@NonNull ArrayList<Message> objects) {
        super(context, receiver,sender, objects);
        this.mContext= context;
        this.resource_receiver =receiver;
        this.resource_sender = sender;
        this.mList = objects;

 }


    @NonNull
    @Override

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        int viewType = getItemViewType(position);
        if(view == null)
        {getItemViewType(position);
            if(viewType==1)
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.card_view_transmit,parent,false);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(resource_sender,parent,false);

            }
            else
            {
                view = LayoutInflater.from(mContext).inflate(R.layout.card_view_receiver,parent,false);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                view = inflater.inflate(resource_receiver,parent,false);
            }

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.date = (TextView)view.findViewById(R.id.dateText);
            viewHolder.message=(TextView)view.findViewById(R.id.messageView);
            viewHolder.name =(TextView)view.findViewById(R.id.nameText);
            viewHolder.time = (TextView)view.findViewById(R.id.timeText);
            ms =mList.get(position);
            viewHolder.name.setText(ms.getNombre());
            viewHolder.message.setText(ms.getMessage());
            viewHolder.time.setText(ms.getTime());
            viewHolder.date.setText(ms.getDate());

        }
        return  view;
    }

    @Override
    public int getItemViewType(int position) {
    if(mList.get(position).getEmisor()!=null)
    {
        if(mList.get(position).getEmisor().equals(mAuth.getUid()))
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }
    else return  -1;
    }
}
