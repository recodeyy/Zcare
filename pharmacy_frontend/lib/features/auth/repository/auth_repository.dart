import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_client.dart';
import '../models/auth_request.dart';
import '../models/auth_response.dart';
import '../models/register_request.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return AuthRepository(dio: dio);
});

class AuthRepository {
  final Dio dio;

  AuthRepository({required this.dio});

  Future<AuthResponse> login(AuthRequest request) async {
    try {
      final response = await dio.post('/auth/login', data: request.toJson());
      return AuthResponse.fromJson(response.data);
    } catch (e) {
      throw Exception('Failed to login. Please check credentials.');
    }
  }

  Future<AuthResponse> register(RegisterRequest request) async {
    try {
      final response = await dio.post('/auth/register', data: request.toJson());
      return AuthResponse.fromJson(response.data);
    } catch (e) {
      throw Exception('Failed to register.');
    }
  }
}