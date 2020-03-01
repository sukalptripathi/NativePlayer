package throwlink.appsters.sukalp.nativeplayer;


import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class BrowseFragment extends Fragment {

    private RecyclerView BrowseList;
    private DatabaseReference videoDatabaseReference;


    FirebaseRecyclerOptions<Video> videoFirebaseOptions;
    FirebaseRecyclerAdapter<Video, VideoViewHolder> videoAdapter;

    LinearLayoutManager linearLayoutManager;
    String strg_title,strg_url,strg_thumb,uploadID;
    DownloadManager downloadManager;
    ArrayList<Long> list = new ArrayList<>();
    BroadcastReceiver onComplete;



    public BrowseFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_browse, container, false);


        videoDatabaseReference= FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_PATH_UPLOADS);
        videoDatabaseReference.keepSynced(true);

        BrowseList=view.findViewById(R.id.itemList);
        linearLayoutManager = new LinearLayoutManager(getContext());


        BrowseList.setHasFixedSize(true);
        BrowseList.setLayoutManager(linearLayoutManager);


        downloadManager = (DownloadManager) getContext().getSystemService(getContext().DOWNLOAD_SERVICE);



        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(BrowseList.getContext(),
                linearLayoutManager.getOrientation());
        Drawable Divider = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        dividerItemDecoration.setDrawable(Divider);
        BrowseList.addItemDecoration(dividerItemDecoration);



         onComplete = new BroadcastReceiver() {

            public void onReceive(Context ctxt, Intent intent) {

                // get the refid from the download manager
                long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                // remove it from our list
                list.remove(referenceId);

                // if list is empty means all downloads completed
                if (list.isEmpty())
                {

                    // show a notification
                    Log.e("INSIDE", "" + referenceId);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getActivity())
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle("GadgetSaint")
                                    .setContentText("All Download completed");


                    NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(getContext().NOTIFICATION_SERVICE);
                    notificationManager.notify(455, mBuilder.build());


                }

            }
        };

        getActivity().registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveList();
    }

    private void retrieveList() {

        videoFirebaseOptions= new FirebaseRecyclerOptions.Builder<Video>().setQuery(videoDatabaseReference,Video.class).build();
        videoAdapter= new FirebaseRecyclerAdapter<Video, VideoViewHolder>(videoFirebaseOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final VideoViewHolder holder, int position, @NonNull Video model) {

                uploadID=getRef(position).getKey();


                videoDatabaseReference.child(uploadID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            strg_title=dataSnapshot.child("title").getValue().toString();
                            strg_url=dataSnapshot.child("url").getValue().toString();
                            strg_thumb= dataSnapshot.child("thumb").getValue().toString();



                            holder.title.setText(strg_title);

                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            //give YourVideoUrl below
                            retriever.setDataSource(strg_url, new HashMap<String, String>());
                            // this gets frame at 2nd second
                            /*Bitmap image = retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);*/
                            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            long timeInMillisec = Long.parseLong(time);
                            retriever.release();
                            String duration=convertMillieToHMmSs(timeInMillisec);


                            Picasso.get().load(strg_thumb).fit().networkPolicy(NetworkPolicy.OFFLINE).into(holder.thumbnail, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(strg_thumb).fit().into(holder.thumbnail);
                                }
                            });
                            holder.duration.setText(duration);


                        }
                        else{

                            Toast.makeText(getActivity(), "Couldn't load the videos", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                VideoViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent playerIntent= new Intent(getContext(), VideoPlayerActivity.class);
                        playerIntent.putExtra("video_url", strg_url);
                        startActivity(playerIntent);

                    }
                });

                VideoViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        /*File destination= new File(Environment.DIRECTORY_DOWNLOADS+"/Native Player/"+strg_title);*/
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(strg_url))
                                .setTitle(strg_title)
                                .setDescription("Downloading")
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Native Player/"+strg_title)
                                .setAllowedOverMetered(true)
                                .setAllowedOverRoaming(true);

                        long downloadId = downloadManager.enqueue(request);
                        list.add(downloadId);


                        return true;
                    }
                });

            }

            @NonNull
            @Override
            public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,parent,false);


                return new VideoViewHolder(view);
            }
        };
        videoAdapter.startListening();
        BrowseList.setAdapter(videoAdapter);

    }
    public static String convertMillieToHMmSs(long millie) {
        long seconds = (millie / 1000);
        long second = seconds % 60;
        long minute = (seconds / 60) % 60;
        long hour = (seconds / (60 * 60)) % 24;

        String result = "";
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute, second);
        }
        else {
            return String.format("%02d:%02d" , minute, second);
        }

    }
    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title,duration;
        static View mView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

            thumbnail=itemView.findViewById(R.id.list_thumbnail);
            title=itemView.findViewById(R.id.list_title);
            duration=itemView.findViewById(R.id.list_duration);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(onComplete);
    }
}
