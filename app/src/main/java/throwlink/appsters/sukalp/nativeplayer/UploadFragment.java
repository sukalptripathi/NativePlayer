package throwlink.appsters.sukalp.nativeplayer;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;


import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends Fragment {

    private TextView filename;
    private EditText title;
    private FloatingActionButton videoSelector;
    private Button upload;
    Bitmap image;
    byte[] data;
    private static final int SELECT_VIDEO = 1;

    private  Uri selectedVideoPath;
    String fileName, downloadURL,thumbDownloadUrl;

    DatabaseReference videoDatabaseReference;
    StorageReference videoStorageReference, filepath,thumbpath;

    public UploadFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        // Initializing components

        filename= view.findViewById(R.id.filename);
        title = view.findViewById(R.id.title_ed);
        videoSelector = view.findViewById(R.id.videoSelector);
        upload = view.findViewById(R.id.upload);

        // initializing the firebase components

        videoDatabaseReference= FirebaseDatabase.getInstance().getReference().child(Constants.DATABASE_PATH_UPLOADS);
        videoStorageReference=FirebaseStorage.getInstance().getReference();

        videoSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(Intent.createChooser(new Intent().
                        setAction(Intent.ACTION_GET_CONTENT).setType("video/mp4"), "Select a Video"), SELECT_VIDEO);
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filepath=videoStorageReference.child(title.getText().toString().trim()+".mp4");
                if (TextUtils.isEmpty(title.getText().toString()))
                {
                    Toast.makeText(getActivity(), "Please enter a title", Toast.LENGTH_SHORT).show();
                }else{
                    uploadVideo();
                }


            }
        });

        return view;
    }

    private void uploadVideo() {

        if (selectedVideoPath!=null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            filepath.putFile(selectedVideoPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //dismissing the progress dialog
                    progressDialog.dismiss();

                    //displaying success toast
                    Toast.makeText(getActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();

                    // getting the url of the vile uploaded
                    getDownloadURL(filepath);


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    //displaying the upload progress
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                }
            });

        }else{

            Toast.makeText(getActivity(), "Please Select a Video first", Toast.LENGTH_SHORT).show();
        }
    }

    private void getDownloadURL(StorageReference filepath) {

        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                downloadURL=uri.toString();

                Log.d("download url", downloadURL);
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                //give YourVideoUrl below
                retriever.setDataSource(downloadURL, new HashMap<String, String>());
                // this gets frame at 2nd second
                 image = retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                data = baos.toByteArray();


                uplaodThumbnail();


            }
        });

    }

    private void uplaodThumbnail() {

        thumbpath=videoStorageReference.child("thumbnails/").child(title.getText().toString().trim()+".jpeg");
        thumbpath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                thumbDownloadUrl();
            }
        });
    }

    private void thumbDownloadUrl() {

        thumbpath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                thumbDownloadUrl=uri.toString();
                //creating data map for the database insertion
                Map<String,Object> uploadMap=new HashMap<>();
                uploadMap.put("title", title.getText().toString().trim());
                uploadMap.put("url", downloadURL);
                uploadMap.put("thumb", thumbDownloadUrl);

                // inserting the data into the database
                String uploadId = videoDatabaseReference.push().getKey();
                videoDatabaseReference.child(uploadId).setValue(uploadMap);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_VIDEO) {

            selectedVideoPath = data.getData();


            if (selectedVideoPath != null) {
                Log.d("video path", selectedVideoPath.toString());

                fileName=selectedVideoPath.toString().substring(selectedVideoPath.toString().lastIndexOf("/")+1);

                Log.d("video name", fileName);
                videoSelector.hide();
                filename.setText(fileName);





            }
            else {
                Toast.makeText(getActivity(), "unable to select video", Toast.LENGTH_SHORT).show();
            }

        }

    }
}
