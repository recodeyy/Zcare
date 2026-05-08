import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_provider.dart';
import '../../../core/network/dio_client.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefs = ref.watch(sharedPreferencesProvider);
    final username = prefs.getString('username') ?? 'User';
    final role = prefs.getString('role') ?? 'PHARMACIST';
    final isAdmin = role == 'ADMIN';

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('My Profile', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF0F172A),
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            _buildProfileHeader(ref, username, role, isAdmin),
            const SizedBox(height: 32),
            _buildInfoSection(context, username, role, isAdmin),
            const SizedBox(height: 32),
            _buildActionSection(ref, context),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileHeader(WidgetRef ref, String username, String role, bool isAdmin) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(32),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF0F172A), Color(0xFF1E293B)],
        ),
      ),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: isAdmin ? const Color(0xFF10B981) : Colors.blue.shade400,
              shape: BoxShape.circle,
            ),
            child: CircleAvatar(
              radius: 50,
              backgroundColor: const Color(0xFF1E293B),
              child: Icon(isAdmin ? Icons.admin_panel_settings_rounded : Icons.person_rounded, size: 60, color: Colors.white),
            ),
          ),
          const SizedBox(height: 20),
          Text(
            username,
            style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 4),
          Text(
            '${username.toLowerCase()}@zcare.com',
            style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 16),
          ),
          const SizedBox(height: 24),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _buildBadge(isAdmin ? 'System Admin' : 'Pharmacist', isAdmin ? Colors.teal : Colors.blue),
              const SizedBox(width: 12),
              _buildBadge('Verified', Colors.teal),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildBadge(String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.2)),
      ),
      child: Text(
        label,
        style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildInfoSection(BuildContext context, String username, String role, bool isAdmin) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.blueGrey.shade50),
      ),
      child: Column(
        children: [
          _buildInfoTile(Icons.badge_outlined, 'Display Name', username),
          const Divider(height: 1),
          _buildInfoTile(Icons.security_rounded, 'System Role', isAdmin ? 'Administrator' : 'Pharmacist'),
          const Divider(height: 1),
          _buildInfoTile(Icons.location_on_outlined, 'Current Branch', 'Central City Pharmacy'),
          const Divider(height: 1),
          _buildInfoTile(Icons.calendar_month_outlined, 'Access Period', 'Active - 2026'),
        ],
      ),
    );
  }

  Widget _buildInfoTile(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.all(20.0),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFFF1F5F9),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: const Color(0xFF64748B), size: 20),
          ),
          const SizedBox(width: 16),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 12)),
              const SizedBox(height: 2),
              Text(value, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15, color: Color(0xFF0F172A))),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildActionSection(WidgetRef ref, BuildContext context) {
    return Column(
      children: [
        _buildActionButton(
          Icons.settings_outlined,
          'Account Settings',
          () {},
          isDestructive: false,
        ),
        const SizedBox(height: 12),
        _buildActionButton(
          Icons.logout_rounded,
          'Log Out',
          () => ref.read(authProvider.notifier).logout(),
          isDestructive: true,
        ),
      ],
    );
  }

  Widget _buildActionButton(IconData icon, String label, VoidCallback onTap, {required bool isDestructive}) {
    final color = isDestructive ? Colors.red : const Color(0xFF0F172A);
    return SizedBox(
      width: double.infinity,
      height: 60,
      child: ElevatedButton(
        onPressed: onTap,
        style: ElevatedButton.styleFrom(
          backgroundColor: isDestructive ? Colors.red.withValues(alpha: 0.05) : Colors.white,
          foregroundColor: color,
          elevation: 0,
          side: BorderSide(color: isDestructive ? Colors.red.withValues(alpha: 0.2) : Colors.blueGrey.shade100),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: 20),
            const SizedBox(width: 12),
            Text(label, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          ],
        ),
      ),
    );
  }
}
