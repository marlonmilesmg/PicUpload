package zw.co.technoextreme.picupload;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zw.co.technoextreme.services.APIClient;
import zw.co.technoextreme.services.UploadService;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Button buttonBrowse, buttonUpload;
    private EditText editTextDescription;
    private ImageView imageViewPhoto;
    private String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);

        buttonBrowse = findViewById(R.id.buttonBrowse);
        buttonUpload = findViewById(R.id.buttonUpload);
        editTextDescription = findViewById(R.id.editTextTextPersonName);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        buttonBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonBrowse_onClick(view);
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonUpload_onClick(view);
            }
        });

    }

    private void buttonBrowse_onClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        Intent result = Intent.createChooser(intent, getText(R.string.choose_file));
        startActivityForResult(result, 10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10) {
            if(resultCode == RESULT_OK) {
                Uri uri = data.getData();
                imagePath = getRealPathFromURI(uri);
                imageViewPhoto.setImageURI(uri);
            }
        }
    }

    private void buttonUpload_onClick(View view) {
        try{
            File file = new File(imagePath);
            RequestBody photoContent = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part photo = MultipartBody.Part.createFormData("photo", file.getName(), photoContent);
            RequestBody description =RequestBody.create(MediaType.parse("text/plain"),
                    editTextDescription.getText().toString());

            UploadService uploadService = APIClient.getClient().create(UploadService.class);
            uploadService.Upload(photo, description).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Toast.makeText(getApplicationContext(), getText(R.string.success), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch(Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri){
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj,
                null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private static void verifyStoragePermissions(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}