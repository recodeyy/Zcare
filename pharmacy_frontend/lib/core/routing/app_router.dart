import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/dio_client.dart';

import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/medicine/presentation/dashboard_screen.dart';
import '../../features/medicine/presentation/medicine_form_screen.dart';
import '../../features/medicine/models/medicine_model.dart';

import '../../core/presentation/shell_screen.dart';

// Import placeholders for now (I will create these screens soon)
import '../../features/billing/presentation/billing_screen.dart';
import '../../features/stock_adjustment/presentation/stock_adjustment_screen.dart';
import '../../features/profile/presentation/profile_screen.dart';

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
      ShellRoute(
        builder: (context, state, child) => ShellScreen(child: child),
        routes: [
          GoRoute(
            path: '/dashboard',
            builder: (context, state) => const DashboardScreen(),
          ),
          GoRoute(
            path: '/billing',
            builder: (context, state) => const BillingScreen(),
          ),
          GoRoute(
            path: '/adjustments',
            builder: (context, state) => const StockAdjustmentScreen(),
          ),
          GoRoute(
            path: '/profile',
            builder: (context, state) => const ProfileScreen(),
          ),
        ],
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
