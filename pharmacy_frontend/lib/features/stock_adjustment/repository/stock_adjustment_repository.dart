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

  Future<List<StockAdjustmentResponse>> getAdjustments() async {
    final response = await dio.get('/stock-adjustments');
    return (response.data as List)
        .map((e) => StockAdjustmentResponse.fromJson(e))
        .toList();
  }
}
