import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_client.dart';
import '../models/stock_adjustment_model.dart';

final stockAdjustmentRepositoryProvider = Provider<StockAdjustmentRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return StockAdjustmentRepository(dio: dio);
});

class StockAdjustmentRepository {
  final Dio dio;

  StockAdjustmentRepository({required this.dio});

  Future<StockAdjustmentResponse> createAdjustment(StockAdjustmentRequest request) async {
    final response = await dio.post('/stock-adjustments', data: request.toJson());
    return StockAdjustmentResponse.fromJson(response.data);
  }

  Future<List<StockAdjustmentResponse>> getAllAdjustments({int page = 0, int size = 20}) async {
    final response = await dio.get('/stock-adjustments', queryParameters: {'page': page, 'size': size});
    // The backend returns a Page object, we need to extract the content
    return (response.data['content'] as List)
        .map((e) => StockAdjustmentResponse.fromJson(e))
        .toList();
  }

  Future<List<StockAdjustmentResponse>> getAdjustmentsByMedicine(int medicineId, {int page = 0, int size = 20}) async {
    final response = await dio.get('/stock-adjustments/medicine/$medicineId', queryParameters: {'page': page, 'size': size});
    return (response.data['content'] as List)
        .map((e) => StockAdjustmentResponse.fromJson(e))
        .toList();
  }
}
