package com.example.spaceinvaders

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.spaceinvaders.db.ResultDAO
import de.codecrafters.tableview.TableView
import de.codecrafters.tableview.listeners.TableDataClickListener
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter
import de.codecrafters.tableview.toolkit.TableDataRowBackgroundProviders
import kotlinx.coroutines.*
import javax.inject.Inject

class ShowResultsActivity : AppCompatActivity(), TableDataClickListener<Array<String>> {
    private lateinit var tableView: TableView<Array<String>>

    @Inject
    lateinit var itemDao: ResultDAO

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN

        setContentView(R.layout.activity_show_results)

        (applicationContext as MyApplication).appComponent.inject(this)

        tableView = findViewById(R.id.resultTable)
        val adapterHead = SimpleTableHeaderAdapter(this, "Miejsce", "Wynik", "Gracz", "Data")
        tableView.addDataClickListener(this)
        tableView.setHeaderBackgroundColor(Color.rgb(98, 0, 238))
        tableView.setDataRowBackgroundProvider(
            TableDataRowBackgroundProviders.alternatingRowColors(
                Color.rgb(224, 224, 224),
                Color.WHITE
            )
        )
        tableView.headerAdapter = adapterHead
        adapterHead.setTextColor(Color.WHITE)
        tableView.dataAdapter = SimpleTableDataAdapter(this, arrayOf())

        GlobalScope.launch {
            val scores = withContext(Dispatchers.IO) {
                itemDao.getAll()
            }

            val dataToAdd = scores.mapIndexed { index, result ->
                arrayOf((index + 1).toString(), result.score.toString(), result.player.toString(), result.dateString.toString())
            }.toTypedArray()

            withContext(Dispatchers.Main) {
                val adapterData = SimpleTableDataAdapter(this@ShowResultsActivity, dataToAdd)
                adapterData.setTextSize(14)
                tableView.setDataAdapter(adapterData)
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            val myIntent = Intent(this, StartActivity::class.java)
            startActivity(myIntent)
        }
    }

    override fun onDataClicked(rowIndex: Int, clickedData: Array<String>?) {
    }
}
