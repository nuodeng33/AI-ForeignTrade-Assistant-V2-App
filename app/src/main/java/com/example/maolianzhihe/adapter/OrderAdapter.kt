package com.example.maolianzhihe.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.maolianzhihe.R
import com.example.maolianzhihe.model.Order

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OrderViewHolder(
        itemView: View,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderStage: TextView = itemView.findViewById(R.id.tv_order_stage)
        private val tvOrderName: TextView = itemView.findViewById(R.id.tv_order_name)
        private val tvOrderNo: TextView = itemView.findViewById(R.id.tv_order_no)
        private val tvOrderRoute: TextView = itemView.findViewById(R.id.tv_order_route)
        private val tvOrderNextAction: TextView = itemView.findViewById(R.id.tv_order_next_action)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)

        fun bind(order: Order) {
            val status = order.status.ifBlank { itemView.context.getString(R.string.ui_pending) }
            tvOrderStage.text = stageLabel(status)
            tvOrderName.text = order.goodsInfo.ifBlank { itemView.context.getString(R.string.ui_default_order) }
            tvOrderNo.text = itemView.context.getString(R.string.ui_number, order.orderNumber.ifBlank { itemView.context.getString(R.string.ui_missing_number) })
            tvOrderRoute.setText(R.string.ui_route_unavailable)
            tvOrderNextAction.text = nextAction(status)
            tvOrderTime.text = order.createdAt.ifBlank { order.createTime ?: itemView.context.getString(R.string.ui_missing_time) }
            tvOrderStatus.text = status
            tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor(status)))
            itemView.setOnClickListener { onOrderClick(order) }
        }

        private fun stageLabel(status: String): String {
            return when {
                status.contains("待付") || status.contains("未付") -> itemView.context.getString(R.string.ui_stage_pay)
                status.contains("待发") -> itemView.context.getString(R.string.ui_stage_ship)
                status.contains("运输") -> itemView.context.getString(R.string.ui_stage_transit)
                status.contains("完成") -> itemView.context.getString(R.string.ui_stage_done)
                else -> itemView.context.getString(R.string.ui_badge)
            }
        }

        private fun nextAction(status: String): String {
            return when {
                status.contains("待付") || status.contains("未付") -> itemView.context.getString(R.string.ui_next_pay)
                status.contains("待发") -> itemView.context.getString(R.string.ui_next_ship)
                status.contains("运输") -> itemView.context.getString(R.string.ui_next_transit)
                status.contains("完成") -> itemView.context.getString(R.string.ui_next_done)
                else -> itemView.context.getString(R.string.ui_next_other)
            }
        }

        private fun statusColor(status: String): Int {
            return when {
                status.contains("完成") -> R.color.green
                status.contains("待发") || status.contains("运输") -> R.color.orange
                status.contains("待付") || status.contains("未付") -> R.color.red
                else -> R.color.gray_600
            }
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id || oldItem.orderNumber == newItem.orderNumber
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
