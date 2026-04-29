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
    final response = await dio.get('/medicines/search?name=$query');
    return (response.data as List)
        .map((e) => MedicineResponse.fromJson(e))
        .toList();
  }
}
