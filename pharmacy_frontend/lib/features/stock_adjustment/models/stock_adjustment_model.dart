class StockAdjustmentRequest {
  final int medicineId;
  final String adjustmentType; // e.g. PURCHASE, RETURN, EXPIRED, DAMAGED
  final int quantityChange;
  final String reason;
  final String? referenceNumber;

  StockAdjustmentRequest({
    required this.medicineId,
    required this.adjustmentType,
    required this.quantityChange,
    required this.reason,
    this.referenceNumber,
  });

  Map<String, dynamic> toJson() {
    return {
      'medicineId': medicineId,
      'adjustmentType': adjustmentType,
      'quantityChange': quantityChange,
      'reason': reason,
      'referenceNumber': referenceNumber,
    };
  }
}

class StockAdjustmentResponse {
  final int id;
  final int medicineId;
  final String adjustmentType;
  final int quantityChange;
  final String reason;
  final String adjustmentDate;

  StockAdjustmentResponse({
    required this.id,
    required this.medicineId,
    required this.adjustmentType,
    required this.quantityChange,
    required this.reason,
    required this.adjustmentDate,
  });

  factory StockAdjustmentResponse.fromJson(Map<String, dynamic> json) {
    return StockAdjustmentResponse(
      id: json['id'],
      medicineId: json['medicineId'] ?? 0,
      adjustmentType: json['adjustmentType'] ?? '',
      quantityChange: json['quantityChange'] ?? 0,
      reason: json['reason'] ?? '',
      adjustmentDate: json['adjustmentDate'] ?? '',
    );
  }
}
