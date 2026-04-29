import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/dio_client.dart';

import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/medicine/presentation/dashboard_screen.dart';
import '../../features/medicine/presentation/medicine_form_screen.dart';
import '../../features/medicine/models/medicine_model.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final prefs = ref.read(sharedPreferencesProvider);

  return GoRouter(
    initialLocation: prefs.getString('jwt_token') != null ? '/dashboard' : '/login',
    redirect: (context, state) {
      final loggedIn = prefs.getString('jwt_token') != null;
      final loggingIn = state.matchedLocation == '/login' || state.matchedLocation == '/register';

      if (!loggedIn && !loggingIn) return '/login';
      if (loggedIn && loggingIn) return '/dashboard';

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/dashboard',
        builder: (context, state) => const DashboardScreen(),
      ),
      GoRoute(
        path: '/medicine_form',
        builder: (context, state) {
          final medicine = state.extra as MedicineResponse?;
          return MedicineFormScreen(medicine: medicine);
        },
      ),
    ],
  );
});
