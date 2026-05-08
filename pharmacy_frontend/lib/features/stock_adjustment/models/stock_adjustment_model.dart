enum AdjustmentType {
  SALE,
  PURCHASE,
  RETURN,
  MANUAL_ADJUSTMENT,
  WRITE_OFF,
  DAMAGE,
  EXPIRY
}

class StockAdjustmentRequest {
  final int medicineId;
  final AdjustmentType adjustmentType;
  final int quantityChange;
  final String? reason;
  final String? referenceNumber;
  final String? batchNumber;

  StockAdjustmentRequest({
    required this.medicineId,
    required this.adjustmentType,
    required this.quantityChange,
    this.reason,
    this.referenceNumber,
    this.batchNumber,
  });

  Map<String, dynamic> toJson() {
    return {
      'medicineId': medicineId,
      'adjustmentType': adjustmentType.name,
      'quantityChange': quantityChange,
      'reason': reason,
      'referenceNumber': referenceNumber,
      'batchNumber': batchNumber,
    };
  }
}

class StockAdjustmentResponse {
  final int id;
  final int medicineId;
  final String medicineName;
  final AdjustmentType adjustmentType;
  final int quantityChange;
  final int previousStockQuantity;
  final int newStockQuantity;
  final String? reason;
  final String? referenceNumber;
  final String createdBy;
  final DateTime createdAt;
  final String? batchNumber;

  StockAdjustmentResponse({
    required this.id,
    required this.medicineId,
    required this.medicineName,
    required this.adjustmentType,
    required this.quantityChange,
    required this.previousStockQuantity,
    required this.newStockQuantity,
    this.reason,
    this.referenceNumber,
    required this.createdBy,
    required this.createdAt,
    this.batchNumber,
  });

  factory StockAdjustmentResponse.fromJson(Map<String, dynamic> json) {
    return StockAdjustmentResponse(
      id: json['id'],
      medicineId: json['medicineId'],
      medicineName: json['medicineName'],
      adjustmentType: AdjustmentType.values.firstWhere((e) => e.name == json['adjustmentType']),
      quantityChange: json['quantityChange'],
      previousStockQuantity: json['previousStockQuantity'],
      newStockQuantity: json['newStockQuantity'],
      reason: json['reason'],
      referenceNumber: json['referenceNumber'],
      createdBy: json['createdBy'],
      createdAt: DateTime.parse(json['createdAt']),
      batchNumber: json['batchNumber'],
    );
  }
}
