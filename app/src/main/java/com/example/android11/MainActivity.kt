package com.example.android11

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getFile.setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            //以下路径在android11上依旧可用
//            println(File(cacheDir.absolutePath, "test.txt").exists())
//            println(File(externalCacheDir?.absolutePath ?: "", "test.txt").exists())
//            val testFile = File(externalCacheDir, "myDir")
//            if (!testFile.exists()) {
//                testFile.mkdirs()
//            }
//            println(filesDir.absolutePath)
            //这个方法在10上还可以用，但是在11上禁止使用了
//            println(File(getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test.txt").exists())
//            val testFile = File(externalCacheDir, "test.txt")
//            if (!testFile.exists()) {
//                testFile.createNewFile()
//            }
//            val byteBuffer = ByteBuffer.wrap("haha".toByteArray())
//            FileOutputStream(testFile).channel.write(byteBuffer)

            pendingNewMediaFile()
        }

        mediaNew.setOnClickListener {
            val file = File("/storage/emulated/0/Pictures/lufei.jpeg")

            searchFile()
//            println(FileProvider.getUriForFile(this, "com.example.android11.fileprovider", file))
//            val intent = MediaStore.createDeleteRequest(contentResolver, arrayListOf(
//                    Uri.parse("content://com.example.android11.fileprovider/external_path/Pictures/lufei.jpeg")
//            ))
//            intent.send()
//            contentResolver.delete(
//                    Uri.parse("content://media/external/images/media/29"),
//                    null
//            )
//            searchFile()
        }
    }

    @SuppressLint("InlinedApi")
    private fun searchFile() {
        //可以获取到媒体库文件
        val collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        contentResolver.query(collection, null, null, null).use { c ->
            c?.apply {
                if (moveToFirst()) {
                    do {
                        val title = c.getString(c.getColumnIndex("_data"))
                        println(title)
                    } while (moveToNext())
                }
                close()
            }
        }
    }

    /**
     * 媒体文件插入，这个文件会放在对应VOLUME的MUSIC，PICTURES，等文件夹下，这些文件夹在11不能直接访问
     */
    @SuppressLint("InlinedApi")
    private fun pendingNewMediaFile() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "lufei.jpeg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collection = MediaStore.Images.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL)
        val item = contentResolver.insert(collection, values)
        item?.apply {
            contentResolver.openFileDescriptor(item, "w", null).use { pfd ->
                // Write data into the pending image.
                pfd?.apply {
                    val inputStream = assets.open("lufei.jpeg")
                    val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(this)
                    writeFile(
                            inputStream = BufferedInputStream(inputStream),
                            outputStream = BufferedOutputStream(outputStream)
                    )
                }
            }
            // Now that we're finished, release the "pending" status, and allow other apps
// to view the image.
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(item, values, null, null)
        }
    }

    private fun writeFile(inputStream: BufferedInputStream, outputStream: BufferedOutputStream) {
        try {
            var hasRead: Int
            val basket = ByteArray(1024)
            while (inputStream.read(basket).also {
                        hasRead = it
                    } != -1) {
                outputStream.write(basket, 0, hasRead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream.close()
            outputStream.close()
        }
    }
}