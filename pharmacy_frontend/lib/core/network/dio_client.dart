import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

const String baseUrl = 'http://localhost:8080/api';

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('SharedPreferences not initialized');
});

final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(BaseOptions(
    baseUrl: baseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 10),
    headers: {
      'Content-Type': 'application/json',
    },
  ));

  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) {
      final prefs = ref.read(sharedPreferencesProvider);
      final token = prefs.getString('jwt_token');
      if (token != null) {
        options.headers['Authorization'] = 'Bearer $token';
      }
      return handler.next(options);
    },
    onError: (DioException e, handler) {
      // Handle unauthorized errors globally if needed
      if (e.response?.statusCode == 401) {
        // e.g., trigger a logout or token refresh
        final prefs = ref.read(sharedPreferencesProvider);
        prefs.remove('jwt_token');
      }
      return handler.next(e);
    },
  ));

  return dio;
});
