class MedicineResponse {
  final int? id;
  final String name;
  final String? category;
  final double price;
  final double? sellingPrice;
  final int stockQuantity;
  final String? expiryDate;
  final String? manufacturer;
  final String? barcode;
  final String? sku;
  final String? batchNumber;
  final String? manufactureDate;
  final String? dosageForm;
  final String? strength;
  final String? genericName;
  final String? brandName;
  final bool? prescriptionRequired;
  final int? minStockLevel;
  final int? maxStockLevel;
  final String? unitOfMeasure;
  final String? rackLocation;
  final double? costPrice;
  final double? taxRate;
  final String? imageUrl;
  final String? activeIngredient;
  final String? storageConditions;
  final bool? isActive;
  final bool? lowStock;
  final bool? expiringSoon;
  final int? daysUntilExpiry;

  MedicineResponse({
    this.id,
    required this.name,
    this.category,
    required this.price,
    this.sellingPrice,
    required this.stockQuantity,
    this.expiryDate,
    this.manufacturer,
    this.barcode,
    this.sku,
    this.batchNumber,
    this.manufactureDate,
    this.dosageForm,
    this.strength,
    this.genericName,
    this.brandName,
    this.prescriptionRequired,
    this.minStockLevel,
    this.maxStockLevel,
    this.unitOfMeasure,
    this.rackLocation,
    this.costPrice,
    this.taxRate,
    this.imageUrl,
    this.activeIngredient,
    this.storageConditions,
    this.isActive,
    this.lowStock,
    this.expiringSoon,
    this.daysUntilExpiry,
  });

  factory MedicineResponse.fromJson(Map<String, dynamic> json) {
    return MedicineResponse(
      id: json['id'],
      name: json['name'] ?? '',
      category: json['category'],
      price: (json['price'] ?? 0.0).toDouble(),
      sellingPrice: (json['sellingPrice'] ?? 0.0).toDouble(),
      stockQuantity: json['stockQuantity'] ?? 0,
      expiryDate: json['expiryDate'],
      manufacturer: json['manufacturer'],
      barcode: json['barcode'],
      sku: json['sku'],
      batchNumber: json['batchNumber'],
      manufactureDate: json['manufactureDate'],
      dosageForm: json['dosageForm'],
      strength: json['strength'],
      genericName: json['genericName'],
      brandName: json['brandName'],
      prescriptionRequired: json['prescriptionRequired'],
      minStockLevel: json['minStockLevel'],
      maxStockLevel: json['maxStockLevel'],
      unitOfMeasure: json['unitOfMeasure'],
      rackLocation: json['rackLocation'],
      costPrice: (json['costPrice'] ?? 0.0).toDouble(),
      taxRate: (json['taxRate'] ?? 0.0).toDouble(),
      imageUrl: json['imageUrl'],
      activeIngredient: json['activeIngredient'],
      storageConditions: json['storageConditions'],
      isActive: json['isActive'],
      lowStock: json['lowStock'],
      expiringSoon: json['expiringSoon'],
      daysUntilExpiry: json['daysUntilExpiry'],
    );
  }
}

class MedicineRequest {
  final String name;
  final String? category;
  final double price;
  final double? sellingPrice;
  final int stockQuantity;
  final String? expiryDate;
  final String? manufacturer;
  final String? barcode;
  final String? sku;
  final String? batchNumber;
  final String? manufactureDate;
  final String? dosageForm;
  final String? strength;
  final String? genericName;
  final String? brandName;
  final bool? prescriptionRequired;
  final int? minStockLevel;
  final int? maxStockLevel;
  final String? unitOfMeasure;
  final String? rackLocation;
  final double? costPrice;
  final double? taxRate;
  final String? imageUrl;
  final String? activeIngredient;
  final String? storageConditions;
  final bool? isActive;

  MedicineRequest({
    required this.name,
    this.category,
    required this.price,
    this.sellingPrice,
    required this.stockQuantity,
    this.expiryDate,
    this.manufacturer,
    this.barcode,
    this.sku,
    this.batchNumber,
    this.manufactureDate,
    this.dosageForm,
    this.strength,
    this.genericName,
    this.brandName,
    this.prescriptionRequired,
    this.minStockLevel,
    this.maxStockLevel,
    this.unitOfMeasure,
    this.rackLocation,
    this.costPrice,
    this.taxRate,
    this.imageUrl,
    this.activeIngredient,
    this.storageConditions,
    this.isActive,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'category': category,
      'price': price,
      'sellingPrice': sellingPrice,
      'stockQuantity': stockQuantity,
      'expiryDate': expiryDate,
      'manufacturer': manufacturer,
      'barcode': barcode,
      'sku': sku,
      'batchNumber': batchNumber,
      'manufactureDate': manufactureDate,
      'dosageForm': dosageForm,
      'strength': strength,
      'genericName': genericName,
      'brandName': brandName,
      'prescriptionRequired': prescriptionRequired,
      'minStockLevel': minStockLevel,
      'maxStockLevel': maxStockLevel,
      'unitOfMeasure': unitOfMeasure,
      'rackLocation': rackLocation,
      'costPrice': costPrice,
      'taxRate': taxRate,
      'imageUrl': imageUrl,
      'activeIngredient': activeIngredient,
      'storageConditions': storageConditions,
      'isActive': isActive,
    };
  }
}
