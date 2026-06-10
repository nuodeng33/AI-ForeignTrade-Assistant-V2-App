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
        private val tvOrderName: TextView = itemView.findViewById(R.id.tv_order_name)
        private val tvOrderNo: TextView = itemView.findViewById(R.id.tv_order_no)
        private val tvOrderTime: TextView = itemView.findViewById(R.id.tv_order_time)
        private val tvOrderStatus: TextView = itemView.findViewById(R.id.tv_order_status)

        fun bind(order: Order) {
            tvOrderName.text = order.goodsInfo.ifBlank { "外贸服务订单" }
            tvOrderNo.text = "单号：${order.orderNumber.ifBlank { "暂无单号" }}"
            tvOrderTime.text = "创建时间：${order.createdAt.ifBlank { "暂无时间" }}"
            tvOrderStatus.text = order.status.ifBlank { "待处理" }
            tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor(order.status)))
            itemView.setOnClickListener { onOrderClick(order) }
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
