package com.example.aplicacion_rey_2023;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.aplicacion_rey_2023.ml.Modelo;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static int REQUEST_GALLERY = 222;

    TextView tv;
    ImageView imageview;
    Bitmap bitmap;


    List<String> etiquetas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        {
            imageview=findViewById(R.id.imageview);
            tv=findViewById(R.id.resultados);
        }
        etiquetas=new ArrayList<>();
        etiquetas.add("Cristiano Ronaldo");
        etiquetas.add("Kane Williamson");
        etiquetas.add("Maria Sharapova");
        etiquetas.add("Kobe Bryant");
    }

    public void abrirGaleria (View view){
        Intent i = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            try {
                if (requestCode == REQUEST_GALLERY)
                {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    imageview.setImageBitmap(bitmap);
                    processImage();
                }
            } catch (IOException e) {
                Log.i("setsoadf",e.getMessage());
            }
        }
    }



    private void processImage()
    {
        try
        {
            Modelo model = Modelo.newInstance(this);

            ImageProcessor imageProcessor =
                    new ImageProcessor.Builder()
                            .add(new ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                            .build();

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);

            inputFeature0.loadBuffer(tensorImage.getBuffer());

            // Runs model inference and gets result.
            Modelo.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();



           int numeromayor_pos=retornar_mayor_posicion(outputFeature0.getFloatArray());


            String rtt=etiquetas.get(numeromayor_pos)+"\n"+outputFeature0.getFloatArray()[numeromayor_pos]*100+" %";

            tv.setText(rtt);

            model.close();
        } catch (IOException e) {

        }
    }


    public int retornar_mayor_posicion(float [] array)
    {
        float numeromayor=0;
        int numeromayor_pos=0;
        for(int i=0; i<array.length; i++){
            if(array[i]>numeromayor){ //
                numeromayor = array[i];
                numeromayor_pos=i;
            }
        }
        return numeromayor_pos;
    }

}