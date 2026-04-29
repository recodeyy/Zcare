import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../../core/network/dio_client.dart';
import '../models/auth_request.dart';
import '../repository/auth_repository.dart';

final authProvider = StateNotifierProvider<AuthNotifier, AsyncValue<void>>((ref) {
  final authRepository = ref.watch(authRepositoryProvider);
  final sharedPreferences = ref.watch(sharedPreferencesProvider);
  return AuthNotifier(authRepository, sharedPreferences);
});

class AuthNotifier extends StateNotifier<AsyncValue<void>> {
  final AuthRepository _authRepository;
  final SharedPreferences _prefs;

  AuthNotifier(this._authRepository, this._prefs) : super(const AsyncValue.data(null));

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
