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
            val status = order.status.ifBlank { "待处理" }
            tvOrderStage.text = stageLabel(status)
            tvOrderName.text = order.goodsInfo.ifBlank { "外贸服务订单" }
            tvOrderNo.text = "订单号 ${order.orderNumber.ifBlank { "暂无单号" }}"
            tvOrderRoute.text = routeSummary(order)
            tvOrderNextAction.text = nextAction(status)
            tvOrderTime.text = order.createdAt.ifBlank { order.createTime ?: "暂无时间" }
            tvOrderStatus.text = status
            tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor(status)))
            itemView.setOnClickListener { onOrderClick(order) }
        }

        private fun stageLabel(status: String): String {
            return when {
                status.contains("待付") || status.contains("未付") -> "款"
                status.contains("待发") -> "仓"
                status.contains("运输") -> "运"
                status.contains("完成") -> "达"
                else -> "单"
            }
        }

        private fun nextAction(status: String): String {
            return when {
                status.contains("待付") || status.contains("未付") -> "跟进付款并确认发货排期"
                status.contains("待发") -> "准备报关资料并安排承运商"
                status.contains("运输") -> "同步物流节点给海外客户"
                status.contains("完成") -> "归档订单并沉淀复购跟进"
                else -> "核对订单信息并推进下一环节"
            }
        }

        private fun routeSummary(order: Order): String {
            val trackingNumber = order.orderNumber.ifBlank { "待分配" }
            return "中国仓库 -> 海外客户 · 物流单 $trackingNumber"
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
