import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/medicine_model.dart';
import '../repository/medicine_repository.dart';

final medicinesProvider = FutureProvider<List<MedicineResponse>>((ref) async {
  final repository = ref.watch(medicineRepositoryProvider);
  return await repository.getMedicines();
});

final searchMedicinesProvider = FutureProvider.family<List<MedicineResponse>, String>((ref, query) async {
  final repository = ref.watch(medicineRepositoryProvider);
  if (query.isEmpty) return await repository.getMedicines();
  return await repository.searchMedicines(query);
});

final expiringSoonMedicinesProvider = FutureProvider.family<List<MedicineResponse>, int>((ref, days) async {
  final repository = ref.watch(medicineRepositoryProvider);
  return await repository.getExpiringSoon(days: days);
});

final lowStockMedicinesProvider = FutureProvider.family<List<MedicineResponse>, int>((ref, threshold) async {
  final repository = ref.watch(medicineRepositoryProvider);
  return await repository.getLowStock(threshold: threshold);
});

final expiredMedicinesProvider = FutureProvider<List<MedicineResponse>>((ref) async {
  final repository = ref.watch(medicineRepositoryProvider);
  return await repository.getExpired();
});

final inactiveMedicinesProvider = FutureProvider<List<MedicineResponse>>((ref) async {
  final repository = ref.watch(medicineRepositoryProvider);
  return await repository.getInactiveMedicines();
});
