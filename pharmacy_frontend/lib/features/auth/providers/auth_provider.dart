import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../core/network/dio_client.dart';
import '../models/auth_request.dart';
import '../repository/auth_repository.dart';

final authProvider = AsyncNotifierProvider<AuthNotifier, void>(() {
  return AuthNotifier();
});

class AuthNotifier extends AsyncNotifier<void> {
  late AuthRepository _authRepository;
  late SharedPreferences _prefs;

  @override
  FutureOr<void> build() {
    _authRepository = ref.watch(authRepositoryProvider);
    _prefs = ref.watch(sharedPreferencesProvider);
  }

  Future<void> login(String username, String password) async {
    state = const AsyncValue.loading();
    try {
      final response = await _authRepository.login(AuthRequest(username: username, password: password));
      await _prefs.setString('jwt_token', response.token);
      await _prefs.setString('username', response.username);
      await _prefs.setString('role', response.role);
      state = const AsyncValue.data(null);
    } catch (e, stack) {
      state = AsyncValue.error(e, stack);
    }
  }

  Future<void> logout() async {
    await _prefs.remove('jwt_token');
    await _prefs.remove('username');
    await _prefs.remove('role');
    state = const AsyncValue.data(null);
  }
}

final userRoleProvider = Provider<String?>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  return prefs.getString('role');
});
