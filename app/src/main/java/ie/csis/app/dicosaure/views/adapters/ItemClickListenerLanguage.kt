package ie.csis.app.dicosaure.views.adapters

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.widget.AdapterView
import ie.csis.app.dicosaure.views.activities.SetLanguageKot
import java.util.*

/**
 * Created by dineen on 16/06/2016.
 */
//Switch language listener
class ItemClickListenerLanguage(baseCtx : Context, appCtx : Context, lng : SetLanguageKot): AdapterView.OnItemClickListener {

    val baseCtx : Context = baseCtx
    val appCtx : Context = appCtx
    val setLanguage : SetLanguageKot = lng

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent!!.getItemAtPosition(position) as HashMap<String, String>

        // Change application language with the selected language
        val locale = Locale(item["code"])
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        this.baseCtx.getResources().updateConfiguration(config,
                this.baseCtx.getResources().getDisplayMetrics())
        this.appCtx.getResources().updateConfiguration(config, this.baseCtx.getResources().getDisplayMetrics())

        // Come back to the Home
        this.setLanguage.startMainActivity()
    }

}