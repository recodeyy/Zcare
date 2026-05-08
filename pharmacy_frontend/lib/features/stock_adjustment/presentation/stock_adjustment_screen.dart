import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../providers/stock_adjustment_provider.dart';
import '../models/stock_adjustment_model.dart';
import '../../medicine/providers/medicine_provider.dart';
import '../repository/stock_adjustment_repository.dart';

class StockAdjustmentScreen extends ConsumerWidget {
  const StockAdjustmentScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final adjustmentsAsync = ref.watch(stockAdjustmentsProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Stock Movements', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF0F172A),
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh_rounded),
            onPressed: () => ref.invalidate(stockAdjustmentsProvider),
          ),
          const SizedBox(width: 8),
        ],
      ),
      body: adjustmentsAsync.when(
        data: (adjustments) => _buildAdjustmentList(context, adjustments),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, s) => Center(child: Text('Error: $e')),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showAdjustmentForm(context),
        backgroundColor: const Color(0xFF0F172A),
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_rounded),
        label: const Text('New Adjustment', style: TextStyle(fontWeight: FontWeight.bold)),
      ),
    );
  }

  Widget _buildAdjustmentList(BuildContext context, List<StockAdjustmentResponse> adjustments) {
    if (adjustments.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: const Color(0xFFF1F5F9),
                shape: BoxShape.circle,
              ),
              child: Icon(Icons.history_rounded, size: 64, color: Colors.blueGrey.shade200),
            ),
            const SizedBox(height: 24),
            const Text(
              'No Movements Recorded',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF1E293B)),
            ),
            const SizedBox(height: 8),
            Text('Track all stock changes here', style: TextStyle(color: Colors.blueGrey.shade500)),
          ],
        ),
      );
    }

    return ListView.separated(
      padding: const EdgeInsets.all(24),
      itemCount: adjustments.length,
      separatorBuilder: (context, index) => const SizedBox(height: 16),
      itemBuilder: (context, index) {
        final adj = adjustments[index];
        final isPositive = adj.quantityChange > 0;
        final dateStr = DateFormat('MMM dd, yyyy • hh:mm a').format(adj.createdAt);
        final statusColor = _getAdjustmentColor(adj.adjustmentType);

        return Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(20),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.03),
                blurRadius: 10,
                offset: const Offset(0, 4),
              ),
            ],
            border: Border.all(color: Colors.blueGrey.shade50),
          ),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: statusColor.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Icon(
                    _getAdjustmentIcon(adj.adjustmentType),
                    color: statusColor,
                    size: 24,
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        adj.medicineName,
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Color(0xFF0F172A)),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        adj.reason?.isNotEmpty == true ? adj.reason! : adj.adjustmentType.name.replaceAll('_', ' '),
                        style: TextStyle(color: Colors.blueGrey.shade500, fontSize: 13),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        dateStr,
                        style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 11),
                      ),
                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                      decoration: BoxDecoration(
                        color: isPositive ? Colors.teal.withValues(alpha: 0.1) : Colors.red.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        '${isPositive ? "+" : ""}${adj.quantityChange}',
                        style: TextStyle(
                          color: isPositive ? Colors.teal.shade700 : Colors.red.shade700,
                          fontWeight: FontWeight.bold,
                          fontSize: 16,
                        ),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Result: ${adj.newStockQuantity}',
                      style: TextStyle(color: Colors.blueGrey.shade500, fontSize: 12, fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Color _getAdjustmentColor(AdjustmentType type) {
    switch (type) {
      case AdjustmentType.SALE: return Colors.blue;
      case AdjustmentType.PURCHASE: return Colors.teal;
      case AdjustmentType.RETURN: return Colors.orange;
      case AdjustmentType.DAMAGE: return Colors.red;
      case AdjustmentType.EXPIRY: return Colors.purple;
      case AdjustmentType.WRITE_OFF: return Colors.blueGrey;
      case AdjustmentType.MANUAL_ADJUSTMENT: return Colors.indigo;
    }
  }

  IconData _getAdjustmentIcon(AdjustmentType type) {
    switch (type) {
      case AdjustmentType.SALE: return Icons.shopping_cart_rounded;
      case AdjustmentType.PURCHASE: return Icons.add_business_rounded;
      case AdjustmentType.RETURN: return Icons.assignment_return_rounded;
      case AdjustmentType.DAMAGE: return Icons.dangerous_rounded;
      case AdjustmentType.EXPIRY: return Icons.event_busy_rounded;
      case AdjustmentType.WRITE_OFF: return Icons.money_off_rounded;
      case AdjustmentType.MANUAL_ADJUSTMENT: return Icons.tune_rounded;
    }
  }

  void _showAdjustmentForm(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => const _StockAdjustmentForm(),
    );
  }
}

class _StockAdjustmentForm extends ConsumerStatefulWidget {
  const _StockAdjustmentForm();

  @override
  ConsumerState<_StockAdjustmentForm> createState() => _StockAdjustmentFormState();
}

class _StockAdjustmentFormState extends ConsumerState<_StockAdjustmentForm> {
  final _formKey = GlobalKey<FormState>();
  int? _selectedMedicineId;
  AdjustmentType _selectedType = AdjustmentType.MANUAL_ADJUSTMENT;
  final _quantityController = TextEditingController();
  final _reasonController = TextEditingController();
  bool _isSubmitting = false;

  @override
  Widget build(BuildContext context) {
    final medicinesAsync = ref.watch(medicinesProvider);

    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
      ),
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom + 32,
        left: 32,
        right: 32,
        top: 32,
      ),
      child: Form(
        key: _formKey,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('New Movement', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, letterSpacing: -0.5)),
                IconButton(
                  onPressed: () => Navigator.pop(context),
                  icon: const Icon(Icons.close_rounded),
                ),
              ],
            ),
            const SizedBox(height: 32),
            medicinesAsync.when(
              data: (medicines) => DropdownButtonFormField<int>(
                decoration: _inputDecoration('Select Medicine', Icons.medication_rounded),
                items: medicines.map((m) => DropdownMenuItem(value: m.id, child: Text(m.name))).toList(),
                onChanged: (val) => setState(() => _selectedMedicineId = val),
                validator: (val) => val == null ? 'Required' : null,
              ),
              loading: () => const LinearProgressIndicator(),
              error: (e, s) => Text('Error loading medicines: $e'),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<AdjustmentType>(
              value: _selectedType,
              decoration: _inputDecoration('Movement Type', Icons.category_rounded),
              items: AdjustmentType.values.map((t) => DropdownMenuItem(value: t, child: Text(t.name.replaceAll('_', ' ')))).toList(),
              onChanged: (val) => setState(() => _selectedType = val!),
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _quantityController,
              decoration: _inputDecoration('Quantity Change', Icons.exposure_rounded).copyWith(hintText: 'Negative for stock reduction'),
              keyboardType: TextInputType.number,
              validator: (val) => (val == null || int.tryParse(val) == null) ? 'Invalid number' : null,
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _reasonController,
              decoration: _inputDecoration('Reason / Note', Icons.note_rounded),
              maxLines: 2,
            ),
            const SizedBox(height: 32),
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton(
                onPressed: _isSubmitting ? null : _submit,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF0F172A),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                  elevation: 0,
                ),
                child: _isSubmitting 
                  ? const SizedBox(height: 24, width: 24, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                  : const Text('Confirm Movement', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  InputDecoration _inputDecoration(String label, IconData icon) {
    return InputDecoration(
      labelText: label,
      prefixIcon: Icon(icon, size: 20),
      filled: true,
      fillColor: const Color(0xFFF8FAFC),
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
        borderSide: const BorderSide(color: Color(0xFF10B981), width: 2),
      ),
    );
  }

  void _submit() async {
    if (!_formKey.currentState!.validate() || _selectedMedicineId == null) return;

    setState(() => _isSubmitting = true);
    try {
      final request = StockAdjustmentRequest(
        medicineId: _selectedMedicineId!,
        adjustmentType: _selectedType,
        quantityChange: int.parse(_quantityController.text),
        reason: _reasonController.text,
      );

      await ref.read(stockAdjustmentRepositoryProvider).createAdjustment(request);
      
      ref.invalidate(stockAdjustmentsProvider);
      ref.invalidate(medicinesProvider);
      
      if (!mounted) return;
      Navigator.pop(context);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Movement recorded successfully'),
          backgroundColor: Colors.teal,
          behavior: SnackBarBehavior.floating,
        )
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red, behavior: SnackBarBehavior.floating)
      );
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }
}
