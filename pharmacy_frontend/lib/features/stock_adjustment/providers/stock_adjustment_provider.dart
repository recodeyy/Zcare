import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/stock_adjustment_model.dart';
import '../repository/stock_adjustment_repository.dart';

final stockAdjustmentsProvider = FutureProvider<List<StockAdjustmentResponse>>((ref) async {
  final repository = ref.watch(stockAdjustmentRepositoryProvider);
  return await repository.getAllAdjustments();
});

final medicineAdjustmentsProvider = FutureProvider.family<List<StockAdjustmentResponse>, int>((ref, medicineId) async {
  final repository = ref.watch(stockAdjustmentRepositoryProvider);
  return await repository.getAdjustmentsByMedicine(medicineId);
});
