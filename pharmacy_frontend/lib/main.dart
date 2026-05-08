import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:google_fonts/google_fonts.dart';

import 'core/network/dio_client.dart';
import 'core/routing/app_router.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize SharedPreferences
  final sharedPreferences = await SharedPreferences.getInstance();

  runApp(
    ProviderScope(
      overrides: [
        sharedPreferencesProvider.overrideWithValue(sharedPreferences),
      ],
      child: const PharmacyApp(),
    ),
  );
}

class PharmacyApp extends ConsumerWidget {
  const PharmacyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'Zcare Pharmacy',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF0F172A), // Slate 900
          primary: const Color(0xFF0F172A),
          secondary: const Color(0xFF10B981), // Emerald 500
          surface: const Color(0xFFF8FAFC), // Slate 50
          error: const Color(0xFFEF4444), // Red 500
        ),
        textTheme: GoogleFonts.outfitTextTheme(
          const TextTheme(
            headlineMedium: TextStyle(
              fontWeight: FontWeight.bold,
              color: Color(0xFF0F172A),
            ),
            titleLarge: TextStyle(
              fontWeight: FontWeight.w600,
              letterSpacing: 0.2,
            ),
          ),
        ),
        cardTheme: CardThemeData(
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
            side: BorderSide(color: Colors.blueGrey.shade200.withOpacity(0.2)),
          ),
          color: Colors.white,
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide(color: Colors.blueGrey.shade100),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide(color: Colors.blueGrey.shade100),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: const BorderSide(color: Color(0xFF0F172A), width: 1.5),
          ),
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 18),
          hintStyle: TextStyle(color: Colors.blueGrey.shade400),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF0F172A),
            foregroundColor: Colors.white,
            minimumSize: const Size(double.infinity, 60),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
            elevation: 0,
            textStyle: GoogleFonts.outfit(
              fontSize: 16,
              fontWeight: FontWeight.w600,
              letterSpacing: 0.5,
            ),
          ).copyWith(
            overlayColor: WidgetStateProperty.all(Colors.white.withOpacity(0.1)),
          ),
        ),
        appBarTheme: AppBarTheme(
          centerTitle: false,
          backgroundColor: const Color(0xFFF8FAFC),
          elevation: 0,
          scrolledUnderElevation: 0,
          iconTheme: const IconThemeData(color: Color(0xFF0F172A)),
          titleTextStyle: GoogleFonts.outfit(
            color: const Color(0xFF0F172A),
            fontSize: 22,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
      routerConfig: router,
    );
  }
}
