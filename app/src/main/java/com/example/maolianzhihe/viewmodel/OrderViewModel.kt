package com.example.maolianzhihe.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.maolianzhihe.data.repository.OrderRepository
import com.example.maolianzhihe.model.Order
import com.example.maolianzhihe.model.OrderRequest
import com.example.maolianzhihe.ui.UiState
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: OrderRepository = OrderRepository()
) : ViewModel() {
    private val _ordersState = MutableLiveData<UiState<List<Order>>>(UiState.Idle)
    val ordersState: LiveData<UiState<List<Order>>> = _ordersState

    private val _createOrderState = MutableLiveData<UiState<Order>>(UiState.Idle)
    val createOrderState: LiveData<UiState<Order>> = _createOrderState

    private val _deleteOrderState = MutableLiveData<UiState<Order>>(UiState.Idle)
    val deleteOrderState: LiveData<UiState<Order>> = _deleteOrderState

    fun loadOrders() {
        _ordersState.value = UiState.Loading
        viewModelScope.launch {
            _ordersState.value = repository.getOrders()
        }
    }

    fun createOrder(request: OrderRequest) {
        _createOrderState.value = UiState.Loading
        viewModelScope.launch {
            _createOrderState.value = repository.createOrder(request)
        }
    }

    fun deleteOrder(order: Order) {
        val documentId = order.documentId
        if (documentId.isNullOrBlank()) {
            _deleteOrderState.value = UiState.Error("当前订单缺少 documentId，无法通过 Strapi 删除")
            return
        }

        _deleteOrderState.value = UiState.Loading
        viewModelScope.launch {
            _deleteOrderState.value = repository.deleteOrder(documentId)
        }
    }
}
