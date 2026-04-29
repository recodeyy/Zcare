class OrderItemRequest {
  final int medicineId;
  final int quantity;

  OrderItemRequest({required this.medicineId, required this.quantity});

  Map<String, dynamic> toJson() {
    return {
      'medicineId': medicineId,
      'quantity': quantity,
    };
  }
}

class CustomerOrderResponse {
  final int id;
  final String orderDate;
  final double totalAmount;
  final String status;
  // simplified since we mostly just want to list orders

  CustomerOrderResponse({
    required this.id,
    required this.orderDate,
    required this.totalAmount,
    required this.status,
  });

  factory CustomerOrderResponse.fromJson(Map<String, dynamic> json) {
    return CustomerOrderResponse(
      id: json['id'],
      orderDate: json['orderDate'] ?? '',
      totalAmount: (json['totalAmount'] ?? 0.0).toDouble(),
      status: json['status'] ?? '',
    );
  }
}
