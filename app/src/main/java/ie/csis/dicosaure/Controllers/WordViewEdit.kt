package ie.csis.dicosaure.Controllers

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
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import ie.csis.dicosaure.Controllers.activities.MainActivity
import ie.csis.dicosaure.business.Translate.Translate
import ie.csis.dicosaure.business.Translate.TranslateSQLITE
import ie.csis.dicosaure.business.dictionary.Dictionary
import ie.csis.dicosaure.business.word.Word
import ie.csis.dicosaure.business.word.WordSQLITE
import org.jetbrains.anko.ctx
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Created by dineen on 12/07/2016.
 */
class WordViewEdit : AppCompatActivity() {

    var mRecorder : MediaRecorder? = null
    var imgWord : Bitmap? = null
    var word : WordSQLITE? = null
    var translations = ArrayList<Word>()
    var translationsRemoveList = ArrayList<Word>()
    var headwordField: TextView? = null
    var noteField : TextView? = null
    var action : Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.view_word_edit)

        //Set the toolbar on the view
        var toolbar = super.findViewById(R.id.tool_bar) as Toolbar
        super.setSupportActionBar(toolbar)
        this.supportActionBar!!.setTitle(R.string.details)
        this.supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        var dictionary = this.intent.getSerializableExtra(MainActivity.EXTRA_DICTIONARY) as Dictionary
        (super.findViewById(R.id.edit_dictionary) as TextView).text = dictionary.getNameDictionary()
        this.headwordField = super.findViewById(R.id.edit_word) as TextView
        this.noteField = super.findViewById(R.id.edit_note) as TextView

        var intent = this.intent.getSerializableExtra(MainActivity.EXTRA_WORD)
        if (intent == null) {
            this.action = NEW_WORD
            this.word = WordSQLITE(this.ctx, idDictionary = dictionary.idDictionary)
        }
        else {
            //Set the word and dictionary, come from the segue
            this.action = UPDATE_WORD
            var wordIntent = intent as Word
            this.word = WordSQLITE(this.ctx, wordIntent.idWord, wordIntent.note,
                    wordIntent.image, wordIntent.sound, wordIntent.headword, wordIntent.dateView,
                    wordIntent.idDictionary)
            this.headwordField!!.text = this.word!!.headword

            if (this.word!!.note == null) {
                this.noteField!!.text = this.resources.getString(R.string.no_note)
            }
            else {
                this.noteField!!.text = this.word!!.note
            }
            this.initTranslationsList()
            if (this.word!!.image != null) {
                this.imgWord = BitmapFactory.decodeByteArray(this.word!!.image, 0, this.word!!.image!!.size)
                (super.findViewById(R.id.image_word) as ImageView).setImageBitmap(this.imgWord)
                (super.findViewById(R.id.text_image) as TextView).text = this.resources.getString(R.string.modify_image)
            }
            if (this.word!!.sound != null) {
                this.initMediaPlayerWithByteArrray(this.word!!.sound!!)
                (super.findViewById(R.id.play_button) as Button).isEnabled = true
            }
            else {
                (super.findViewById(R.id.play_button) as Button).isEnabled = false
                (super.findViewById(R.id.delete_btn) as Button).visibility = View.INVISIBLE
            }
        }
    }

    fun initMediaPlayerWithByteArrray(ba : ByteArray) {
        val soundFile = File("""${this.cacheDir}/audiorecord.3gp""")
        val fos = FileOutputStream(soundFile)
        fos.write(ba)
        fos.close()
    }

    fun removeSound(view: View) {
        (super.findViewById(R.id.delete_btn) as Button).visibility = View.INVISIBLE
        (super.findViewById(R.id.play_button) as Button).isEnabled = false
    }

    fun initTranslationsList() {
        this.translations = ArrayList(this.word!!.selectAllTranslations())
        this.initFrameTranslation()
    }

    fun initFrameTranslation() {
        var translationField = super.findViewById(R.id.edit_translation) as TextView
        if (this.translations!!.count() > 0) {
            var strTranslations = ""
            for (tr in this.translations!!) {
                strTranslations = strTranslations.plus("- " + tr.headword + "\n")
            }
            translationField.text = strTranslations
        }
        else {
            translationField.text = ""
            val btn = super.findViewById(R.id.delete_translation) as Button
            btn.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        super.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
//                val wordFrom = WordSQLITE(this.ctx, headword = translateTxt, idDictionary = "0", note = "")
//                wordFrom.save()
//                val translate = TranslateSQLITE(this.ctx, this.word, wordFrom)
//
//                if (translate.save() > 0) {
//                    Toast.makeText(this.ctx, R.string.translation_success, 10000).show()
//                    if (this.translations!!.size == 0) {
//                        val btn = super.findViewById(R.id.delete_translation) as Button
//                        btn.isEnabled = true
//                    }
//                    this.initTranslationsList()
//                }
//                else {
//                    Toast.makeText(this.ctx, R.string.translation_error, 10000).show()
//                }
                val wordTo = Word(headword = translateTxt, idDictionary = "0", note = "")
                val translate = Translate(this.word, wordTo)

                if (!this.translations!!.contains(wordTo)) {
                    Toast.makeText(this.ctx, R.string.translation_success, 10000).show()
                    if (this.translations!!.size == 0) {
                        val btn = super.findViewById(R.id.delete_translation) as Button
                        btn.isEnabled = true
                    }
                    this.translations!!.add(wordTo)
                    this.initFrameTranslation()
                }
                else {
                    Toast.makeText(this.ctx, R.string.translation_error, 10000).show()
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
        var gridLayout = layout.findViewById(R.id.popup_layout) as GridLayout
        val txtTitle = gridLayout.findViewById(R.id.title_translation)
        gridLayout.removeAllViews()

        gridLayout.rowCount = this.translations!!.size + 1
        gridLayout.addView(txtTitle)

        var checkbox : CheckBox
        var listCheckBox = ArrayList<CheckBox>()

        for (tr in this.translations!!) {
            checkbox = CheckBox(this.ctx)
            checkbox.isChecked = false
            checkbox.text = tr.headword
            checkbox.tag = tr

            gridLayout.addView(checkbox)

            listCheckBox.add(checkbox)
        }

        //Adding the layout to the dialog
        builder.setView(layout)
        builder.setPositiveButton(R.string.delete) { dialog, which ->
            for (cb in listCheckBox) {
                if (cb.isChecked) {
                    this.translations!!.remove(cb.tag)
                    if (cb.tag is WordSQLITE) {
                        this.translationsRemoveList.add(cb.tag as Word)
                    }
                    //val tr = TranslateSQLITE(this.ctx, this.word, cb.tag as Word)
                    //tr.delete()
                }
            }
            //this.initTranslationsList()
            this.initFrameTranslation()
            dialog.cancel()
        }
        builder.create()
        builder.show()
    }

    fun loadImagefromGallery(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(this.resources.getString(R.string.what_do_you_want))
        builder.setPositiveButton(R.string.add) { dialog, which ->
            // Create intent to Open Image applications like Gallery, Google Photos
            val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // Start the Intent
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG)
            dialog.cancel()
        }
        builder.setNegativeButton(R.string.cancel) {
            dialog, which -> dialog.cancel()
        }
        builder.setNeutralButton(R.string.delete) {
            dialog, which ->
            (super.findViewById(R.id.image_word) as ImageView).setImageBitmap(null)
            (super.findViewById(R.id.text_image) as TextView).text = this.resources.getString(R.string.add_txt_img)
            this.imgWord = null
            dialog.cancel()
        }
        builder.create()
        builder.show()
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

                val exif = ExifInterface(imgDecodableString)
                if (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) == ExifInterface.ORIENTATION_ROTATE_90) {
                    val matrix = Matrix();
                    matrix.postRotate(90f);
                    img = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true);
                }
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(img)
                this.imgWord = img
                (super.findViewById(R.id.text_image) as TextView).text = this.resources.getString(R.string.modify_image)
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
                var soundFile = File("""${this.cacheDir}/audiorecord.3gp""")

                this.mRecorder = MediaRecorder()
                this.mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                this.mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                this.mRecorder!!.setMaxDuration(10000)
                this.mRecorder!!.setOutputFile(soundFile.path)
                this.mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    this.mRecorder!!.prepare()
                } catch (e: IOException) {
                    Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
                }
                this.mRecorder!!.start()
                (super.findViewById(R.id.delete_btn) as Button).visibility = View.VISIBLE
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
                mPlayer.setDataSource("""${this.cacheDir}/audiorecord.3gp""")
                mPlayer.prepare()
                mPlayer.start()
            }
            catch (e: IOException) {
                Toast.makeText(this, this.resources.getString(R.string.permission_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    fun audioFileIntoByte() : ByteArray {
        val fis = File("""${this.cacheDir}/audiorecord.3gp""").inputStream()
        val bos = ByteArrayOutputStream()
        val b : ByteArray = ByteArray(1024)
        var bytesRead = fis.read(b)
        while (bytesRead != -1) {
            bos.write(b, 0, bytesRead);
            bytesRead = fis.read(b)
        }
        return bos.toByteArray();
    }

    fun imageIntoByte() : ByteArray {
        var bos: ByteArrayOutputStream? = ByteArrayOutputStream();
        this.imgWord!!.compress(Bitmap.CompressFormat.JPEG, 10, bos);
        return bos!!.toByteArray()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menuInflater.inflate(R.menu.menu_new_word, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val handler = Handler(object : Handler.Callback {
            override fun handleMessage(p0: Message?): Boolean {
                if (p0!!.arg1 == UPDATE_SUCCESS) {
                    Toast.makeText(ctx, resources.getString(R.string.succes_update_word), 10000).show()
                    onBackPressed()
                } else if (p0!!.arg1 == CREATE_SUCCESS) {
                    Toast.makeText(ctx, resources.getString(R.string.succes_create_word), 10000).show()
                    onBackPressed()
                } else if (p0!!.arg1 == NAME_EXIST) {
                    Toast.makeText(ctx, resources.getString(R.string.name_already_exists), 10000).show()
                } else {
                    Toast.makeText(ctx, resources.getString(R.string.headword_missing), 10000).show()
                }
                return false
            }
        })

        var msg = Message()

        Thread(object : Runnable {
            override fun run() {
                if (headwordField!!.text.toString().isEmpty()) {
                    msg.arg1 = HEADWORD_MISSING
                }
                else {
                    word!!.headword = headwordField!!.text.toString()
                    word!!.sound = if ((findViewById(R.id.play_button) as Button).isEnabled == false ) null else audioFileIntoByte()
                    word!!.image = if (imgWord == null) null else imageIntoByte()
                    word!!.note = noteField!!.text.toString()
                    val log : Int
                    if (action == UPDATE_WORD) {
                        log = word!!.update()
                    }
                    else {
                        log = word!!.save()
                    }
                    if (log > 0) {
                        for (tr in translations!!) {
                            val wordFrom = WordSQLITE(ctx, headword = tr.headword, idDictionary = tr.idDictionary, note = tr.note)
                            wordFrom.save()
                            val translate = TranslateSQLITE(ctx, word, wordFrom)
                            translate.save()
                        }
                        for (tr in translationsRemoveList) {
                            val tr = TranslateSQLITE(ctx, word, tr)
                            tr.delete()
                        }
                        if (action == UPDATE_WORD) {
                            msg.arg1 = UPDATE_SUCCESS
                        }
                        else {
                            msg.arg1 = CREATE_SUCCESS
                        }
                    }
                    else {
                        msg.arg1 = NAME_EXIST
                    }
                }
                handler.sendMessage(msg)
            }
    }).start()

        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val RESULT_LOAD_IMG = 1
        private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2
        private val NEW_WORD = true
        private val UPDATE_WORD = false
        private val UPDATE_SUCCESS = 0
        private val CREATE_SUCCESS = 1
        private val NAME_EXIST = 2
        private val HEADWORD_MISSING = 3
        private val FILE_NAME_SOUND = "audiorecord"
        private val FILE_NAME_EXTENSION = "3gp"
    }
}