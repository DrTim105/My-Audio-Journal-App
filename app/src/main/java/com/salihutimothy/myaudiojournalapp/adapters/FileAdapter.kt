package com.salihutimothy.myaudiojournalapp.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salihutimothy.myaudiojournalapp.R
import com.salihutimothy.myaudiojournalapp.database.DBHelper
import com.salihutimothy.myaudiojournalapp.entities.RecordingItem
import com.salihutimothy.myaudiojournalapp.interfaces.OnDatabaseChangedListener
import java.util.*
import java.util.concurrent.TimeUnit

class FileAdapter(
    var context: Context,
    var arrayList: ArrayList<RecordingItem>,
    var llm: LinearLayoutManager
) :
    RecyclerView.Adapter<FileAdapter.FileViewerViewHolder?>(), OnDatabaseChangedListener {
    private val dbHelper: DBHelper = DBHelper(context)

    override fun onBindViewHolder(holder: FileAdapter.FileViewerViewHolder, position: Int) {
        val recordingItem: RecordingItem = arrayList[position]
        val minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItem.length)
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(recordingItem.length) - TimeUnit.MINUTES.toSeconds(
                minutes
            )
        holder.tvRecordName!!.text = recordingItem.name
        holder.tvRecordLength!!.text = String.format("%02d:%02d", minutes, seconds)
        holder.tvRecordTime!!.text = DateUtils.formatDateTime(
            context, recordingItem.time_added,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_SHOW_TIME
                    or DateUtils.FORMAT_SHOW_YEAR
        )

        holder.cardView!!.setOnClickListener {
            //                    val playbackFragment = PlaybackFragment()
            //                    val b = Bundle()
            //                    b.putSerializable("item", arrayList.get(getAdapterPosition()))
            //                    playbackFragment.setArguments(b)
            //                    val fragmentTransaction: FragmentTransaction = (context as FragmentActivity)
            //                        .getSupportFragmentManager()
            //                        .beginTransaction()
            //                    playbackFragment.show(fragmentTransaction, "dialog_playback")
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onNewDatabaseEntryAdded(recordingItem: RecordingItem?) {
        if (recordingItem != null) {
            arrayList.add(recordingItem)
        }

        notifyItemInserted(arrayList.size - 1)

    }

    init {
        dbHelper.setOnDatabaseChangedListener(this)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FileAdapter.FileViewerViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_view, parent, false)
        return FileViewerViewHolder(itemView)
    }

    class FileViewerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvRecordName: TextView? = itemView.findViewById(R.id.file_name_text)
        var tvRecordLength: TextView? = itemView.findViewById(R.id.file_length_text)
        var tvRecordTime: TextView? = itemView.findViewById(R.id.file_time_added)
        var ivRecordImage: ImageView? = itemView.findViewById(R.id.imageView)
        var cardView: CardView? = itemView.findViewById(R.id.card_view)

    }

}

