import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../features/auth/providers/auth_provider.dart';
import '../network/dio_client.dart';

class ShellScreen extends ConsumerWidget {
  final Widget child;

  const ShellScreen({super.key, required this.child});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final location = GoRouterState.of(context).matchedLocation;
    final isDesktop = MediaQuery.of(context).size.width > 900;

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: Row(
        children: [
          if (isDesktop)
            _buildPremiumSidebar(context, ref, location),
          Expanded(
            child: Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: isDesktop ? const BorderRadius.only(topLeft: Radius.circular(32), bottomLeft: Radius.circular(32)) : null,
                boxShadow: [
                  if (isDesktop)
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.05),
                      blurRadius: 20,
                      offset: const Offset(-10, 0),
                    ),
                ],
              ),
              child: ClipRRect(
                borderRadius: isDesktop ? const BorderRadius.only(topLeft: Radius.circular(32), bottomLeft: Radius.circular(32)) : BorderRadius.zero,
                child: child,
              ),
            ),
          ),
        ],
      ),
      bottomNavigationBar: !isDesktop ? _buildBottomBar(context, location) : null,
    );
  }

  Widget _buildPremiumSidebar(BuildContext context, WidgetRef ref, String location) {
    final role = ref.watch(userRoleProvider);
    final isAdmin = role == 'ADMIN';
    final username = ref.watch(sharedPreferencesProvider).getString('username') ?? 'User';

    return Container(
      width: 280,
      decoration: const BoxDecoration(
        color: Color(0xFF0F172A),
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFF0F172A), Color(0xFF1E293B)],
        ),
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 40, horizontal: 24),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: const Color(0xFF10B981),
                    borderRadius: BorderRadius.circular(16),
                    boxShadow: [
                      BoxShadow(
                        color: const Color(0xFF10B981).withValues(alpha: 0.3),
                        blurRadius: 15,
                        offset: const Offset(0, 5),
                      ),
                    ],
                  ),
                  child: const Icon(Icons.health_and_safety, color: Colors.white, size: 28),
                ),
                const SizedBox(width: 16),
                const Text(
                  'Zcare',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 26,
                    fontWeight: FontWeight.bold,
                    letterSpacing: 1,
                  ),
                ),
              ],
            ),
          ),
          Expanded(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                children: [
                  _SidebarItem(
                    icon: Icons.dashboard_rounded,
                    label: isAdmin ? 'Inventory Management' : 'Product Catalog',
                    isActive: location.startsWith('/dashboard'),
                    onTap: () => context.go('/dashboard'),
                  ),
                  const SizedBox(height: 12),
                  _SidebarItem(
                    icon: Icons.receipt_long_rounded,
                    label: isAdmin ? 'Billing Overview' : 'Sales Terminal',
                    isActive: location.startsWith('/billing'),
                    onTap: () => context.go('/billing'),
                  ),
                  if (isAdmin) ...[
                    const SizedBox(height: 12),
                    _SidebarItem(
                      icon: Icons.history_rounded,
                      label: 'Stock Movements',
                      isActive: location.startsWith('/adjustments'),
                      onTap: () => context.go('/adjustments'),
                    ),
                  ],
                  const SizedBox(height: 12),
                  _SidebarItem(
                    icon: Icons.person_rounded,
                    label: isAdmin ? 'System Profile' : 'My Profile',
                    isActive: location.startsWith('/profile'),
                    onTap: () => context.go('/profile'),
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(24.0),
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(20),
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    backgroundColor: isAdmin ? const Color(0xFF10B981) : const Color(0xFF334155),
                    child: Icon(isAdmin ? Icons.admin_panel_settings_rounded : Icons.person, color: Colors.white, size: 20),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          username,
                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 14),
                          overflow: TextOverflow.ellipsis,
                        ),
                        Text(
                          isAdmin ? 'Administrator' : 'Pharmacist',
                          style: const TextStyle(color: Colors.white54, fontSize: 11),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.logout_rounded, color: Colors.white54),
                    onPressed: () {
                      ref.read(authProvider.notifier).logout();
                      context.go('/login');
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomBar(BuildContext context, String location) {
    return NavigationBar(
      backgroundColor: Colors.white,
      elevation: 0,
      indicatorColor: const Color(0xFF10B981).withValues(alpha: 0.1),
      selectedIndex: _getSelectedIndex(location),
      onDestinationSelected: (index) => _onItemTapped(context, index),
      destinations: const [
        NavigationDestination(icon: Icon(Icons.dashboard_rounded), label: 'Inventory'),
        NavigationDestination(icon: Icon(Icons.receipt_long_rounded), label: 'Billing'),
        NavigationDestination(icon: Icon(Icons.history_rounded), label: 'Adjustments'),
        NavigationDestination(icon: Icon(Icons.person_rounded), label: 'Profile'),
      ],
    );
  }

  int _getSelectedIndex(String location) {
    if (location.startsWith('/dashboard')) return 0;
    if (location.startsWith('/billing')) return 1;
    if (location.startsWith('/adjustments')) return 2;
    if (location.startsWith('/profile')) return 3;
    return 0;
  }

  void _onItemTapped(BuildContext context, int index) {
    switch (index) {
      case 0: context.go('/dashboard'); break;
      case 1: context.go('/billing'); break;
      case 2: context.go('/adjustments'); break;
      case 3: context.go('/profile'); break;
    }
  }
}

class _SidebarItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  const _SidebarItem({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 16),
        decoration: BoxDecoration(
          color: isActive ? const Color(0xFF10B981) : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
          boxShadow: isActive ? [
            BoxShadow(
              color: const Color(0xFF10B981).withValues(alpha: 0.3),
              blurRadius: 10,
              offset: const Offset(0, 4),
            )
          ] : null,
        ),
        child: Row(
          children: [
            Icon(icon, color: isActive ? Colors.white : Colors.blueGrey, size: 22),
            const SizedBox(width: 16),
            Text(
              label,
              style: TextStyle(
                color: isActive ? Colors.white : Colors.blueGrey,
                fontSize: 15,
                fontWeight: isActive ? FontWeight.bold : FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
