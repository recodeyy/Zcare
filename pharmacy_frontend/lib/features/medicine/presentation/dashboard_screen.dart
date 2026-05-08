import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:animations/animations.dart';
import 'package:intl/intl.dart';
import '../../auth/providers/auth_provider.dart';
import '../providers/medicine_provider.dart';
import '../models/medicine_model.dart';
import 'package:intl/intl.dart';
import 'medicine_form_screen.dart';
import '../../../core/presentation/widgets/shimmer_loading.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  String _searchQuery = '';
  String? _selectedCategory;

  @override
  Widget build(BuildContext context) {
    final medicinesAsync = ref.watch(searchMedicinesProvider(_searchQuery));
    final lowStockAsync = ref.watch(lowStockMedicinesProvider(10));
    final expiringSoonAsync = ref.watch(expiringSoonMedicinesProvider(30));

    final isDesktop = MediaQuery.of(context).size.width > 900;
    final role = ref.watch(userRoleProvider);
    final isAdmin = role == 'ADMIN';

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(24, 32, 24, 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            isAdmin ? 'Admin Dashboard' : 'Pharmacist Portal',
                            style: const TextStyle(
                              fontSize: 32,
                              fontWeight: FontWeight.bold,
                              color: Color(0xFF0F172A),
                              letterSpacing: -1,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            DateFormat('EEEE, MMMM dd').format(DateTime.now()),
                            style: TextStyle(
                              fontSize: 16,
                              color: Colors.blueGrey.shade400,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ],
                      ),
                      if (isDesktop)
                        Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(16),
                            border: Border.all(color: Colors.blueGrey.shade50),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.wb_sunny_rounded, color: Colors.orange, size: 20),
                              const SizedBox(width: 8),
                              Text(isAdmin ? 'Admin Access' : 'Pharmacist Access', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                            ],
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 32),
                  _buildStatsGrid(context, medicinesAsync, lowStockAsync, expiringSoonAsync),
                  if (isAdmin) ...[
                    const SizedBox(height: 32),
                    _buildChartSection(context, medicinesAsync),
                  ] else ...[
                    const SizedBox(height: 32),
                    _buildPharmacistQuickActions(context),
                  ],
                ],
              ),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16),
              child: Row(
                children: [
                  Expanded(
                    child: Container(
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(20),
                        boxShadow: [
                          BoxShadow(
                            color: Colors.black.withValues(alpha: 0.05),
                            blurRadius: 15,
                            offset: const Offset(0, 5),
                          ),
                        ],
                      ),
                      child: TextField(
                        decoration: InputDecoration(
                          hintText: 'Search inventory...',
                          hintStyle: TextStyle(color: Colors.blueGrey.shade300),
                          prefixIcon: Icon(Icons.search_rounded, color: Colors.blueGrey.shade400),
                          filled: true,
                          fillColor: Colors.white,
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(20),
                            borderSide: BorderSide(color: Colors.blueGrey.shade50),
                          ),
                          enabledBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(20),
                            borderSide: BorderSide(color: Colors.blueGrey.shade50),
                          ),
                          focusedBorder: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(20),
                            borderSide: const BorderSide(color: Color(0xFF10B981), width: 2),
                          ),
                        ),
                        onChanged: (value) => setState(() => _searchQuery = value),
                      ),
                    ),
                  ),
                  if (isDesktop) const SizedBox(width: 16),
                  if (isDesktop)
                    _buildCategoryFilter(),
                ],
              ),
            ),
          ),
          const SliverPadding(padding: EdgeInsets.only(top: 8)),
          medicinesAsync.when(
            data: (medicines) {
              final filtered = medicines.where((m) {
                final matchQuery = m.name.toLowerCase().contains(_searchQuery.toLowerCase()) || 
                                 (m.category?.toLowerCase().contains(_searchQuery.toLowerCase()) ?? false);
                final matchCategory = _selectedCategory == null || m.category == _selectedCategory;
                return matchQuery && matchCategory;
              }).toList();
              
              if (filtered.isEmpty) {
                return SliverFillRemaining(
                  hasScrollBody: false,
                  child: _buildEmptyState(),
                );
              }
              
              return SliverPadding(
                padding: const EdgeInsets.all(24),
                sliver: SliverGrid(
                  gridDelegate: SliverGridDelegateWithMaxCrossAxisExtent(
                    maxCrossAxisExtent: isDesktop ? 450 : 600,
                    mainAxisExtent: 140,
                    crossAxisSpacing: 16,
                    mainAxisSpacing: 16,
                  ),
                  delegate: SliverChildBuilderDelegate(
                    (context, index) => _buildMedicineItem(context, filtered[index]),
                    childCount: filtered.length,
                  ),
                ),
              );
            },
            loading: () => SliverPadding(
              padding: const EdgeInsets.all(24),
              sliver: SliverGrid(
                gridDelegate: SliverGridDelegateWithMaxCrossAxisExtent(
                  maxCrossAxisExtent: isDesktop ? 450 : 600,
                  mainAxisExtent: 140,
                  crossAxisSpacing: 16,
                  mainAxisSpacing: 16,
                ),
                delegate: SliverChildBuilderDelegate(
                  (context, index) => const ShimmerLoading(width: double.infinity, height: 140, borderRadius: 24),
                  childCount: 6,
                ),
              ),
            ),
            error: (e, s) => SliverFillRemaining(child: Center(child: Text('Error: $e'))),
          ),
          const SliverPadding(padding: EdgeInsets.only(bottom: 100)),
        ],
      ),
      floatingActionButton: isAdmin ? OpenContainer(
        transitionType: ContainerTransitionType.fadeThrough,
        openBuilder: (context, _) => MedicineFormScreen(),
        closedElevation: 6,
        closedShape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        closedColor: const Color(0xFF0F172A),
        closedBuilder: (context, openContainer) => Container(
          height: 56,
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: const Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.add, color: Colors.white),
              SizedBox(width: 8),
              Text('Add Medicine', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
            ],
          ),
        ),
      ) : null,
    );
  }

  Widget _buildStatsGrid(
    BuildContext context,
    AsyncValue<List<MedicineResponse>> medicines,
    AsyncValue<List<MedicineResponse>> lowStock,
    AsyncValue<List<MedicineResponse>> expiring,
  ) {
    if (medicines.isLoading) return const DashboardStatsShimmer();

    return LayoutBuilder(
      builder: (context, constraints) {
        final crossAxisCount = constraints.maxWidth > 1200 ? 4 : (constraints.maxWidth > 600 ? 2 : 1);
        return GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: crossAxisCount,
          mainAxisSpacing: 16,
          crossAxisSpacing: 16,
          childAspectRatio: 2.5,
          children: [
            _buildStatCard(
              'Total Medicines',
              medicines.maybeWhen(data: (m) => m.length.toString(), orElse: () => '...'),
              Icons.inventory_2_rounded,
              const Color(0xFF0F172A),
            ),
            _buildStatCard(
              'Low Stock Items',
              lowStock.maybeWhen(data: (m) => m.length.toString(), orElse: () => '...'),
              Icons.warning_amber_rounded,
              const Color(0xFFF59E0B),
            ),
            _buildStatCard(
              'Expiring Soon',
              expiring.maybeWhen(data: (m) => m.length.toString(), orElse: () => '...'),
              Icons.event_busy_rounded,
              const Color(0xFFEF4444),
            ),
            _buildStatCard(
              'Total Valuation',
              medicines.maybeWhen(
                data: (List<MedicineResponse> m) {
                  final double total = m.fold<double>(0.0, (double sum, MedicineResponse med) => sum + (med.price * med.stockQuantity));
                  return '\$${total.toStringAsFixed(0)}';
                },
                orElse: () => '...',
              ),
              Icons.account_balance_wallet_rounded,
              const Color(0xFF10B981),
            ),
          ],
        );
      },
    );
  }

  Widget _buildStatCard(String title, String value, IconData icon, Color color) {
    return Container(
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            color,
            color.withValues(alpha: 0.8),
          ],
        ),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.3),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            right: -20,
            top: -20,
            child: Icon(icon, size: 100, color: Colors.white.withValues(alpha: 0.1)),
          ),
          Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Row(
                  children: [
                    Icon(icon, color: Colors.white70, size: 20),
                    const SizedBox(width: 8),
                    Text(
                      title,
                      style: const TextStyle(color: Colors.white70, fontSize: 13, fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
                const SizedBox(height: 8),
                Text(
                  value,
                  style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.bold),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoryFilter() {
    final categories = ['All', 'Tablet', 'Syrup', 'Capsule', 'Injection', 'Ointment'];
    return SizedBox(
      height: 50,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        shrinkWrap: true,
        itemCount: categories.length,
        separatorBuilder: (context, index) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final cat = categories[index];
          final isSelected = (_selectedCategory == null && cat == 'All') || (_selectedCategory == cat);
          return ChoiceChip(
            label: Text(cat),
            selected: isSelected,
            onSelected: (selected) {
              setState(() => _selectedCategory = cat == 'All' ? null : cat);
            },
            selectedColor: const Color(0xFF0F172A),
            labelStyle: TextStyle(color: isSelected ? Colors.white : Colors.blueGrey.shade700),
            backgroundColor: Colors.white,
            side: BorderSide(color: isSelected ? Colors.transparent : Colors.blueGrey.shade200),
            showCheckmark: false,
          );
        },
      ),
    );
  }

  Widget _buildMedicineItem(BuildContext context, MedicineResponse medicine) {
    final bool isLowStock = medicine.stockQuantity <= 10;
    
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.blueGrey.shade50),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () => context.push('/medicine_form', extra: medicine),
          borderRadius: BorderRadius.circular(24),
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.blueGrey.shade50,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(Icons.medication_rounded, color: Colors.blueGrey.shade700, size: 28),
                ),
                const SizedBox(width: 20),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        medicine.name,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18, letterSpacing: -0.5),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      const SizedBox(height: 4),
                      SingleChildScrollView(
                        scrollDirection: Axis.horizontal,
                        child: Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.blueGrey.shade100,
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: Text(
                                medicine.category ?? "General",
                                style: TextStyle(color: Colors.blueGrey.shade700, fontSize: 11, fontWeight: FontWeight.bold),
                              ),
                            ),
                            const SizedBox(width: 8),
                            Text(
                              'Batch: ${medicine.batchNumber ?? "N/A"}',
                              style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 12),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '\$${medicine.price.toStringAsFixed(2)}',
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 20, color: Color(0xFF0F172A)),
                    ),
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                      decoration: BoxDecoration(
                        color: isLowStock ? Colors.red.withValues(alpha: 0.1) : Colors.teal.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Container(
                            width: 6,
                            height: 6,
                            decoration: BoxDecoration(
                              color: isLowStock ? Colors.red : Colors.teal,
                              shape: BoxShape.circle,
                            ),
                          ),
                          const SizedBox(width: 6),
                          Text(
                            'Stock: ${medicine.stockQuantity}',
                            style: TextStyle(
                              color: isLowStock ? Colors.red : Colors.teal.shade700,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(width: 16),
                Icon(Icons.chevron_right_rounded, color: Colors.blueGrey.shade200),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 20,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: Icon(Icons.inventory_2_rounded, size: 64, color: Colors.blueGrey.shade200),
          ),
          const SizedBox(height: 32),
          const Text(
            'No matches found',
            style: TextStyle(
              color: Color(0xFF0F172A),
              fontSize: 22,
              fontWeight: FontWeight.bold,
              letterSpacing: -0.5,
            ),
          ),
          const SizedBox(height: 12),
          Text(
            'We couldn\'t find any medicines matching\nyour current search or filters.',
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.blueGrey.shade500, fontSize: 15, height: 1.5),
          ),
          const SizedBox(height: 32),
          OutlinedButton.icon(
            onPressed: () => setState(() {
              _searchQuery = '';
              _selectedCategory = null;
            }),
            icon: const Icon(Icons.refresh_rounded, size: 18),
            label: const Text('Clear all filters'),
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              side: BorderSide(color: Colors.blueGrey.shade100),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChartSection(BuildContext context, AsyncValue<List<MedicineResponse>> medicinesAsync) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Row(
          children: [
            Icon(Icons.analytics_rounded, color: Color(0xFF10B981), size: 20),
            SizedBox(width: 10),
            Text(
              'Inventory Analytics',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, letterSpacing: -0.5, color: Color(0xFF0F172A)),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: Colors.blueGrey.shade50),
          ),
          child: Padding(
            padding: const EdgeInsets.all(24.0),
            child: SizedBox(
              height: 280,
              child: medicinesAsync.when(
                data: (medicines) => _buildBarChart(medicines),
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (e, s) => Center(child: Text('Error: $e')),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildPharmacistQuickActions(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Row(
          children: [
            Icon(Icons.bolt_rounded, color: Colors.orange, size: 20),
            SizedBox(width: 10),
            Text(
              'Quick Operations',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, letterSpacing: -0.5, color: Color(0xFF0F172A)),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Row(
          children: [
            Expanded(
              child: _buildQuickActionCard(
                context,
                'New Sales Bill',
                'Generate a bill for a customer',
                Icons.add_shopping_cart_rounded,
                const Color(0xFF10B981),
                () => context.go('/billing'),
              ),
            ),
            if (MediaQuery.of(context).size.width > 600) ...[
              const SizedBox(width: 20),
              Expanded(
                child: _buildQuickActionCard(
                  context,
                  'View Catalog',
                  'Browse items and prices',
                  Icons.menu_book_rounded,
                  const Color(0xFF6366F1),
                  () {},
                ),
              ),
            ],
          ],
        ),
      ],
    );
  }

  Widget _buildQuickActionCard(
    BuildContext context,
    String title,
    String subtitle,
    IconData icon,
    Color color,
    VoidCallback onTap,
  ) {
    return Container(
      height: 120,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.blueGrey.shade50),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.05),
            blurRadius: 20,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(24),
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: color.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(icon, color: color, size: 28),
                ),
                const SizedBox(width: 20),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        title,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18, letterSpacing: -0.5),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        subtitle,
                        style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 13),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildBarChart(List<MedicineResponse> medicines) {
    final Map<String, int> categories = {};
    for (var med in medicines) {
      final cat = med.category ?? 'General';
      categories[cat] = (categories[cat] ?? 0) + 1;
    }

    final sorted = categories.entries.toList()..sort((a, b) => b.value.compareTo(a.value));
    final top5 = sorted.take(5).toList();

    return BarChart(
      BarChartData(
        alignment: BarChartAlignment.spaceAround,
        maxY: top5.isEmpty ? 10 : (top5.map((e) => e.value).fold(0, (a, b) => a > b ? a : b) + 2).toDouble(),
        barTouchData: BarTouchData(
          touchTooltipData: BarTouchTooltipData(
            getTooltipColor: (_) => const Color(0xFF0F172A),
            getTooltipItem: (group, groupIndex, rod, rodIndex) {
              return BarTooltipItem(
                '${top5[groupIndex].key}\n',
                const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                children: [
                  TextSpan(
                    text: '${rod.toY.toInt()} Items',
                    style: const TextStyle(color: Color(0xFF10B981), fontWeight: FontWeight.w500),
                  ),
                ],
              );
            },
          ),
        ),
        titlesData: FlTitlesData(
          show: true,
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 40,
              getTitlesWidget: (value, meta) {
                if (value.toInt() < 0 || value.toInt() >= top5.length) return const Text('');
                return Padding(
                  padding: const EdgeInsets.only(top: 12.0),
                  child: Text(
                    top5[value.toInt()].key,
                    style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold, color: Colors.blueGrey.shade600),
                  ),
                );
              },
            ),
          ),
          leftTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 30,
              getTitlesWidget: (value, meta) => Text(
                value.toInt().toString(),
                style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 10),
              ),
            ),
          ),
          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        ),
        gridData: FlGridData(
          show: true,
          drawVerticalLine: false,
          getDrawingHorizontalLine: (value) => FlLine(color: Colors.blueGrey.shade50, strokeWidth: 1),
        ),
        borderData: FlBorderData(show: false),
        barGroups: List.generate(top5.length, (i) {
          return BarChartGroupData(
            x: i,
            barRods: [
              BarChartRodData(
                toY: top5[i].value.toDouble(),
                gradient: const LinearGradient(
                  colors: [Color(0xFF10B981), Color(0xFF059669)],
                  begin: Alignment.bottomCenter,
                  end: Alignment.topCenter,
                ),
                width: 24,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(8)),
              ),
            ],
          );
        }),
      ),
    );
  }
}
