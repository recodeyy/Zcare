import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_client.dart';
import '../models/medicine_model.dart';

final medicineRepositoryProvider = Provider<MedicineRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return MedicineRepository(dio: dio);
});

class MedicineRepository {
  final Dio dio;

  MedicineRepository({required this.dio});

  Future<List<MedicineResponse>> getMedicines() async {
    final response = await dio.get('/medicines');
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<MedicineResponse> createMedicine(MedicineRequest request) async {
    final response = await dio.post('/medicines', data: request.toJson());
    return MedicineResponse.fromJson(response.data);
  }

  Future<MedicineResponse> updateMedicine(int id, MedicineRequest request) async {
    final response = await dio.put('/medicines/$id', data: request.toJson());
    return MedicineResponse.fromJson(response.data);
  }

  Future<void> deleteMedicine(int id) async {
    await dio.delete('/medicines/$id');
  }

  Future<List<MedicineResponse>> searchMedicines(String query) async {
    final response = await dio.get('/medicines/search', queryParameters: {'name': query});
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<List<MedicineResponse>> getExpiringSoon({int days = 30}) async {
    final response = await dio.get('/medicines/expiring-soon', queryParameters: {'days': days});
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<List<MedicineResponse>> getLowStock({int threshold = 10}) async {
    final response = await dio.get('/medicines/low-stock', queryParameters: {'threshold': threshold});
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<List<MedicineResponse>> getExpired() async {
    final response = await dio.get('/medicines/expired');
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<List<MedicineResponse>> getInactiveMedicines() async {
    final response = await dio.get('/medicines/inactive');
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }

  Future<MedicineResponse> restoreMedicine(int id) async {
    final response = await dio.patch('/medicines/$id/restore');
    return MedicineResponse.fromJson(response.data);
  }
}
