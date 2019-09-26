package com.darkraha.appbackend

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.darkraha.backend.Backend
import com.darkraha.backend.QueryCallback
import com.darkraha.backend.UserQuery

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    val imageManager = Backend.sharedInstance.imageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        //  imageManager.getDiskcache().buildClear().exeSync()
        // val url = "https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"
        val url =
        "https://i.kinja-img.com/gawker-media/image/upload/s--mNVOtE0R--/c_scale,f_auto,fl_progressive,q_80,w_1600/ttgduophm1etj8iaobir.gif"
        //  "https://gamespot1.cbsistatic.com/uploads/screen_kubrick/1578/15787979/3584344-0257016877-world.jpg"
        //"https://i.gifer.com/Vimq.gif"
        imageManager.load(
            url,
            image, object : QueryCallback {
                override fun onError(query: UserQuery) {
                    println("MainActivity error " + query.errorMessage())
                }

                override fun onSuccess(query: UserQuery) {
                    println("MainActivity success ")
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
