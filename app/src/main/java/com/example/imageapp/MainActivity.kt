package com.example.imageapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity() {

    lateinit var image: ImageView
    lateinit var button: Button
    val url: String = "https://picsum.photos/200"
    lateinit var errorView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.random_image)
        button = findViewById(R.id.fetch_button)
        errorView = findViewById(R.id.error_msg)

        if(!checkForInternet(this))
            errorView.visibility = View.VISIBLE

        Glide.with(this)
            .load(url)
            .apply(RequestOptions.skipMemoryCacheOf(true)).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
            .into(image)

        button.setOnClickListener( View.OnClickListener {

            if(!checkForInternet(this))
            {
                errorView.visibility = View.VISIBLE
                Glide.with(this)
                    .load(url)
                    .apply(RequestOptions.skipMemoryCacheOf(true)).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .into(image)
            }
            else {

                GlobalScope.launch (Dispatchers.IO){
                    Glide.get(applicationContext).clearDiskCache()
                }

                errorView.visibility = View.GONE
                Glide.with(this)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(true)
                    .into(image)
            }

        })


    }

    private fun checkForInternet(context: Context): Boolean {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }


//    suspend fun saveImage(image: Bitmap): Bitmap {
//
//        var savedImagePath: String? = null
//        val imageFileName = "JPEG_" + "FILE_NAME" + ".jpg"
//        val storageDir = File(
//            Environment.getDataDirectory()
//                .toString() + "/IMAGE_APP"
//        )
//        var success = true
//        if (!storageDir.exists()) {
//            success = storageDir.mkdirs()
//        }
//        if (success) {
//            val imageFile = File(storageDir, imageFileName)
//            savedImagePath = imageFile.absolutePath
//            try {
//                val fOut: OutputStream = FileOutputStream(imageFile)
//                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
//                fOut.close()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            // Add the image to the system gallery
////            galleryAddPic(savedImagePath)
//            //Toast.makeText(this, "IMAGE SAVED", Toast.LENGTH_LONG).show() // to make this working, need to manage coroutine, as this execution is something off the main thread
//        }
//        return image
//    }

    private fun saveImage(image: Bitmap): String? {
        var savedImagePath: String? = null
        val imageFileName = "JPEG_" + "image" + ".jpg"
        val storageDir = File(
            Environment.getRootDirectory()
                .toString() + "/Comicoid"
        )
//        var success = true
//        if (!storageDir.exists()) {
//            success = storageDir.mkdirs()
//        }
//        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
            galleryAddPic(savedImagePath)
//        }
        return savedImagePath
    }

    private fun galleryAddPic(imagePath: String?) {
        imagePath?.let { path ->
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_STARTED)
            val f = File(path)
            val contentUri: Uri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
            sendBroadcast(mediaScanIntent)
        }
    }
}