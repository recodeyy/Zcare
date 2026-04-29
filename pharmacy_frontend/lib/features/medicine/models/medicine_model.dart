class MedicineResponse {
  final int? id;
  final String name;
  final String? genericName;
  final String category;
  final double price;
  final int stockQuantity;
  final String expiryDate;
  final String batchNumber;
  final String? manufacturer;
  final String? unit;

  MedicineResponse({
    this.id,
    required this.name,
    this.genericName,
    required this.category,
    required this.price,
    required this.stockQuantity,
    required this.expiryDate,
    required this.batchNumber,
    this.manufacturer,
    this.unit,
  });

  factory MedicineResponse.fromJson(Map<String, dynamic> json) {
    return MedicineResponse(
      id: json['id'],
      name: json['name'] ?? '',
      genericName: json['genericName'],
      category: json['category'] ?? '',
      price: (json['price'] ?? 0.0).toDouble(),
      stockQuantity: json['stockQuantity'] ?? 0,
      expiryDate: json['expiryDate'] ?? '',
      batchNumber: json['batchNumber'] ?? '',
      manufacturer: json['manufacturer'],
      unit: json['unit'],
    );
  }
}

class MedicineRequest {
  final String name;
  final String? genericName;
  final String category;
  final double price;
  final int stockQuantity;
  final String expiryDate;
  final String batchNumber;
  final String? manufacturer;
  final String? unit;

  MedicineRequest({
    required this.name,
    this.genericName,
    required this.category,
    required this.price,
    required this.stockQuantity,
    required this.expiryDate,
    required this.batchNumber,
    this.manufacturer,
    this.unit,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'genericName': genericName,
      'category': category,
      'price': price,
      'stockQuantity': stockQuantity,
      'expiryDate': expiryDate,
      'batchNumber': batchNumber,
      'manufacturer': manufacturer,
      'unit': unit,
    };
  }
}
