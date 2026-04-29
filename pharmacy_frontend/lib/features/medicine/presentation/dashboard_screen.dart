import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../auth/providers/auth_provider.dart';
import '../providers/medicine_provider.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  String _searchQuery = '';

  @override
  Widget build(BuildContext context) {
    final medicinesAsync = ref.watch(searchMedicinesProvider(_searchQuery));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard - Medicines'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(authProvider.notifier).logout();
              if (mounted) context.go('/login');
            },
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: TextField(
              decoration: const InputDecoration(
                labelText: 'Search Medicine',
                prefixIcon: Icon(Icons.search),
                border: OutlineInputBorder(),
              ),
              onChanged: (value) => setState(() => _searchQuery = value),
            ),
          ),
          Expanded(
            child: medicinesAsync.when(
              data: (medicines) {
                if (medicines.isEmpty) {
                  return const Center(child: Text('No medicines found.'));
                }
                return ListView.builder(
                  itemCount: medicines.length,
                  itemBuilder: (context, index) {
                    final medicine = medicines[index];
                    return ListTile(
                      title: Text(medicine.name),
                      subtitle: Text('Category: ${medicine.category} | Stock: ${medicine.stockQuantity}'),
                      trailing: Text('\$${medicine.price.toStringAsFixed(2)}'),
                      onTap: () {
                        context.push('/medicine_form', extra: medicine);
                      },
                    );
                  },
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (error, stack) => Center(child: Text('Error: $error')),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          context.push('/medicine_form');
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
