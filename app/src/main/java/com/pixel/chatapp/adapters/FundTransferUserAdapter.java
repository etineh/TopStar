package com.pixel.chatapp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pixel.chatapp.R;
import com.pixel.chatapp.dataModel.FundTransferUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FundTransferUserAdapter extends RecyclerView.Adapter<FundTransferUserAdapter.TransferUserViewHolder> {

    private List<FundTransferUser> userList;
    private Context context;

    public interface ProceedToTransferPage {
        void openTransferPage(FundTransferUser fundTransferUser);
    }

    private ProceedToTransferPage transferPageListener;

    public void setTransferPageListener(ProceedToTransferPage transferPageListener) {
        this.transferPageListener = transferPageListener;
    }

    public FundTransferUserAdapter(List<FundTransferUser> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public TransferUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fund_transfer_user_card,
                parent, false);

        return new FundTransferUserAdapter.TransferUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransferUserViewHolder holder, int position) {

        FundTransferUser userDetails = userList.get(position);

        //reset data
        holder.displayName.setText(null);
        holder.username.setText(null);
        holder.userImage.setImageURI( null );


        //  get all other-user details -----------------------
        String imagePath = userDetails.getImagePath();
        String displayName = userDetails.getDisplayName();
        String username = userDetails.getUsername();
        String userUid = userDetails.getUserUid();

        if(imagePath != null){
            holder.userImage.setImageURI( Uri.parse(imagePath) );
        }

        holder.displayName.setText(displayName);
        holder.username.setText(username);


        holder.itemView.setOnClickListener(v -> {

            transferPageListener.openTransferPage(userDetails);
//            Toast.makeText(context, "in progress", Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class TransferUserViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView userImage;
        private TextView displayName, username, userUid;

        public TransferUserViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.contactPic);
            displayName = itemView.findViewById(R.id.displayName);
            username = itemView.findViewById(R.id.username);
//            userUid = itemView.findViewById(R.id.textViewMsgCount);

        }
    }
}
