package com.example.map_gis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private var notifications: List<DataNotification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private var unreadNotifications: Set<Int> = HashSet()


    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val viewIndikator : View = itemView.findViewById(R.id.indicatorView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_card, parent, false)
        return NotificationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val currentNotification = notifications[position]
        holder.titleTextView.text = currentNotification.title
        holder.messageTextView.text = currentNotification.message
        if (unreadNotifications.contains(position)) {
            holder.viewIndikator.visibility = View.VISIBLE
        } else {
            holder.viewIndikator.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = notifications.size

    fun setNotifications(notifications: List<DataNotification>) {
        this.notifications = notifications
        this.unreadNotifications = unreadNotifications
        notifyDataSetChanged()
    }
}
