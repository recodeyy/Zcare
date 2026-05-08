class OrderItemRequest {
  final int medicineId;
  final String? batchNumber;
  final int quantity;

  OrderItemRequest({
    required this.medicineId,
    this.batchNumber,
    required this.quantity,
  });

  Map<String, dynamic> toJson() {
    return {
      'medicineId': medicineId,
      'batchNumber': batchNumber,
      'quantity': quantity,
    };
  }
}

class OrderItemResponse {
  final int id;
  final String medicineName;
  final String? batchNumber;
  final int quantity;
  final double unitPrice;
  final double totalPrice;

  OrderItemResponse({
    required this.id,
    required this.medicineName,
    this.batchNumber,
    required this.quantity,
    required this.unitPrice,
    required this.totalPrice,
  });

  factory OrderItemResponse.fromJson(Map<String, dynamic> json) {
    return OrderItemResponse(
      id: json['id'],
      medicineName: json['medicineName'],
      batchNumber: json['batchNumber'],
      quantity: json['quantity'],
      unitPrice: (json['unitPrice'] as num).toDouble(),
      totalPrice: (json['totalPrice'] as num).toDouble(),
    );
  }
}

class CustomerOrderResponse {
  final int id;
  final DateTime orderDate;
  final String createdBy;
  final double totalAmount;
  final List<OrderItemResponse> items;

  CustomerOrderResponse({
    required this.id,
    required this.orderDate,
    required this.createdBy,
    required this.totalAmount,
    required this.items,
  });

  factory CustomerOrderResponse.fromJson(Map<String, dynamic> json) {
    return CustomerOrderResponse(
      id: json['id'],
      orderDate: DateTime.parse(json['orderDate']),
      createdBy: json['createdBy'],
      totalAmount: (json['totalAmount'] as num).toDouble(),
      items: (json['items'] as List)
          .map((e) => OrderItemResponse.fromJson(e))
          .toList(),
    );
  }
}
