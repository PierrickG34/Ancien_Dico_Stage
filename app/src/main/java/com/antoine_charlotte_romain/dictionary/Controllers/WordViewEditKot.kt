package com.antoine_charlotte_romain.dictionary.Controllers

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.antoine_charlotte_romain.dictionary.Controllers.activities.MainActivityKot
import com.antoine_charlotte_romain.dictionary.R
import com.antoine_charlotte_romain.dictionary.business.dictionary.Dictionary
import com.antoine_charlotte_romain.dictionary.business.word.Word
import com.antoine_charlotte_romain.dictionary.business.word.WordSQLITE
import com.dicosaure.Business.Translate.Translate
import com.dicosaure.Business.Translate.TranslateSQLITE
import com.dicosaure.XML.TranslationXML
import org.jetbrains.anko.childrenSequence
import org.jetbrains.anko.ctx
import org.jetbrains.anko.firstChild
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by dineen on 12/07/2016.
 */
class WordViewEditKot : AppCompatActivity() {

    var mRecorder : MediaRecorder? = null
    var fileAudioPath : String? = null
    var imgWord : Bitmap? = null
    var word : WordSQLITE? = null
    var translations : List<Word>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.view_word_edit)

        //Set the toolbar on the view
        var toolbar = super.findViewById(R.id.tool_bar) as Toolbar
        super.setSupportActionBar(toolbar)
        this.supportActionBar!!.setTitle(R.string.details)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Set the word and dictionary, come from the segue
        var wordIntent = this.intent.getSerializableExtra(MainActivityKot.EXTRA_WORD) as Word
        this.word = WordSQLITE(this.ctx, wordIntent.idWord, wordIntent.note,
                wordIntent.image, wordIntent.sound, wordIntent.headword, wordIntent.dateView,
                wordIntent.idDictionary)
        var dictionary = this.intent.getSerializableExtra(MainActivityKot.EXTRA_DICTIONARY) as Dictionary

        //Set fields
        (super.findViewById(R.id.edit_dictionary) as TextView).text = dictionary.getNameDictionary()
        (super.findViewById(R.id.edit_word) as TextView).text = this.word!!.headword
        if (this.word!!.note == null) {
            (super.findViewById(R.id.edit_note) as TextView).text = this.resources.getString(R.string.no_note)
        }
        else {
            (super.findViewById(R.id.edit_note) as TextView).text = this.word!!.headword
        }
        this.translations = this.word!!.selectAllTranslations()
        var translationField = super.findViewById(R.id.edit_translation) as TextView
        if (this.translations!!.count() > 0) {
            var strTranslations = ""
            for (tr in this.translations!!) {
                strTranslations = strTranslations.plus("- " + tr.headword + "\n")
            }
            translationField.text = strTranslations
        }
        if (this.word!!.image != null) {
            var img = BitmapFactory.decodeByteArray(this.word!!.image, 0, this.word!!.image!!.size)
            (super.findViewById(R.id.image_word) as ImageView).setImageBitmap(img)
        }

        (super.findViewById(R.id.play_button) as Button).isEnabled = false
    }

    fun add_translation(view: View) {
        //Creating the dialog builder
        val builder = AlertDialog.Builder(this)
        val layout = LayoutInflater.from(ctx).inflate(R.layout.add_translation, null)
        val field = layout.findViewById(R.id.edit_translation) as EditText

        //Adding the layout to the dialog
        builder.setView(layout)
        builder.setPositiveButton(R.string.add) { dialog, which ->
            if (!field.text.toString().isEmpty()) {
                val translateTxt = field.text.toString().trim { it <= ' ' }
                val wordFrom = WordSQLITE(this.ctx, headword = translateTxt, idDictionary = "0", note = "")
                wordFrom.save()
                val translate = TranslateSQLITE(this.ctx, this.word, wordFrom)
                if (translate.save() > 0) {
                    var translationField = super.findViewById(R.id.edit_translation) as TextView
                    val tmp = translationField.text.toString()
                    translationField.text = tmp.plus("- " + field.text.toString() + "\n")
                    Toast.makeText(this.ctx, R.string.translation_success, 2000)
                }
                else {
                    Toast.makeText(this.ctx, R.string.translation_error, 2000)
                }
            }
            dialog.cancel()
        }
        builder.create()
        builder.show()
    }

    fun remove_translation(view: View) {
        //Creating the dialog builder
        val builder = AlertDialog.Builder(this)

        var layout = LayoutInflater.from(ctx).inflate(R.layout.remove_translation, null).findViewById(R.id.global_layout) as RelativeLayout
        var gridLayout = layout!!.firstChild { true } as GridLayout
        gridLayout.rowCount = this.translations!!.size

        var tmp = gridLayout.findViewById(R.id.check_translation) as CheckBox
        var checkbox : CheckBox
        layout.removeView(gridLayout)
        gridLayout.removeView(tmp)

        for (tr in this.translations!!) {
            checkbox = CheckBox(this.ctx)
            checkbox.isChecked = true
            checkbox.text = tr.headword

            gridLayout.addView(checkbox)
        }
        layout.addView(gridLayout)

        //Adding the layout to the dialog
        builder.setView(layout)
        builder.setPositiveButton(R.string.add) { dialog, which ->
        }
        builder.create()
        builder.show()
    }

    fun loadImagefromGallery(view: View) {
        // Create intent to Open Image applications like Gallery, Google Photos
        val galleryIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK && null != data) {
                // Get the Image from data
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                if (Build.VERSION.SDK_INT >= 23) {
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        }
                        else {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

                            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                }
                // Get the cursor
                val cursor = contentResolver.query(selectedImage,
                        filePathColumn, null, null, null)
                //val filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
                // Move to first row
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                var imgDecodableString = cursor.getString(columnIndex)
                cursor.close()

                val imgView = findViewById(R.id.image_word) as ImageView
                var img = BitmapFactory.decodeFile(imgDecodableString)
                println(imgDecodableString)
                val exif = ExifInterface(imgDecodableString)
                if (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) == ExifInterface.ORIENTATION_ROTATE_90) {
                    val matrix = Matrix();
                    matrix.postRotate(90f);
                    img = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true);
                }
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(img)
                this.imgWord = img
            }
            else {
                Toast.makeText(this, this.resources.getString(R.string.error_picked_image), Toast.LENGTH_LONG).show()
            }
        }
        catch (e: Exception) {
            Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording(view: View) {
        var btnRecord = (super.findViewById(R.id.start_recording) as Button)
        if (btnRecord.text == this.resources.getString(R.string.record)) {
            try {
                val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1
                if (Build.VERSION.SDK_INT >= 23) {
                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(applicationContext,
                            Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.RECORD_AUDIO)) {

                            // Show an expanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.

                        } else {

                            // No explanation needed, we can request the permission.

                            ActivityCompat.requestPermissions(this,
                                    arrayOf(Manifest.permission.RECORD_AUDIO),
                                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO)

                            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    }
                }
                //create file audio
                btnRecord.text = this.resources.getString(R.string.recording)
                val dir = File("""${this.applicationContext.filesDir}/nfs/dicosaure""")
                dir.mkdirs() //create folders where write files
                this.fileAudioPath = dir.getPath() //Environment.getExternalStorageDirectory().getAbsolutePath();
                this.fileAudioPath += "/audiorecord.3gp"

                this.mRecorder = MediaRecorder()
                this.mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                this.mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                this.mRecorder!!.setMaxDuration(10000)
                this.mRecorder!!.setOutputFile(this.fileAudioPath)
                this.mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    this.mRecorder!!.prepare()
                } catch (e: IOException) {
                    Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
                }
                this.mRecorder!!.start()
            } catch (e: Exception) {
                Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            this.mRecorder!!.stop()
            this.mRecorder!!.release()
            btnRecord.text = this.resources.getString(R.string.record)
            (super.findViewById(R.id.play_button) as Button).isEnabled = true
        }
    }

    fun playRecord(view: View) {
        var btnPlay = super.findViewById(R.id.play_button) as Button
        if (btnPlay.isEnabled) {
            var mPlayer = MediaPlayer()
            try {
                mPlayer.setDataSource(this.fileAudioPath)
                mPlayer.prepare()
                mPlayer.start()
            }
            catch (e: IOException) {
                Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    companion object {
        private val RESULT_LOAD_IMG = 1
        private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2
    }
}