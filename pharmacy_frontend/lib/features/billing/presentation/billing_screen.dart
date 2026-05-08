import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:lottie/lottie.dart';
import '../../medicine/providers/medicine_provider.dart';
import '../../medicine/models/medicine_model.dart';
import '../providers/billing_provider.dart';
import '../models/order_model.dart';
import '../repository/billing_repository.dart';
import '../../../core/presentation/widgets/shimmer_loading.dart';

class BillingScreen extends ConsumerStatefulWidget {
  const BillingScreen({super.key});

  @override
  ConsumerState<BillingScreen> createState() => _BillingScreenState();
}

class _BillingScreenState extends ConsumerState<BillingScreen> {
  String _searchQuery = '';
  bool _isProcessing = false;

  @override
  Widget build(BuildContext context) {
    final medicinesAsync = ref.watch(searchMedicinesProvider(_searchQuery));
    final cart = ref.watch(cartProvider);
    final totalAmount = ref.read(cartProvider.notifier).totalAmount;
    final isDesktop = MediaQuery.of(context).size.width > 1100;

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        title: const Text('Pharmacy Billing', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: const Color(0xFF0F172A),
        elevation: 0,
        actions: [
          if (!isDesktop)
            Stack(
              alignment: Alignment.center,
              children: [
                IconButton(
                  icon: const Icon(Icons.shopping_cart_rounded),
                  onPressed: () => _showCartDrawer(context),
                ),
                if (cart.isNotEmpty)
                  Positioned(
                    right: 8,
                    top: 8,
                    child: Container(
                      padding: const EdgeInsets.all(4),
                      decoration: const BoxDecoration(color: Color(0xFF10B981), shape: BoxShape.circle),
                      constraints: const BoxConstraints(minWidth: 16, minHeight: 16),
                      child: Text(
                        '${cart.length}',
                        style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ),
              ],
            ),
          const SizedBox(width: 8),
        ],
      ),
      body: Row(
        children: [
          Expanded(
            flex: 3,
            child: Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(24.0),
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
                        hintText: 'Search medicine by name or batch...',
                        hintStyle: TextStyle(color: Colors.blueGrey.shade300),
                        prefixIcon: Icon(Icons.search_rounded, color: Colors.blueGrey.shade400),
                        suffixIcon: _searchQuery.isNotEmpty 
                          ? IconButton(icon: const Icon(Icons.clear_rounded), onPressed: () => setState(() => _searchQuery = ''))
                          : null,
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
                Expanded(
                  child: medicinesAsync.when(
                    data: (medicines) => _buildMedicineGrid(medicines),
                    loading: () => GridView.builder(
                      padding: const EdgeInsets.all(24),
                      gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
                        maxCrossAxisExtent: 280,
                        childAspectRatio: 0.75,
                        crossAxisSpacing: 20,
                        mainAxisSpacing: 20,
                      ),
                      itemCount: 8,
                      itemBuilder: (context, index) => const ShimmerLoading(width: double.infinity, height: 200, borderRadius: 24),
                    ),
                    error: (e, s) => Center(child: Text('Error: $e')),
                  ),
                ),
              ],
            ),
          ),
          if (isDesktop)
            Expanded(
              flex: 1,
              child: Container(
                margin: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(32),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.05),
                      blurRadius: 30,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: _buildCartSummary(cart, totalAmount),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildMedicineGrid(List<MedicineResponse> medicines) {
    return GridView.builder(
      padding: const EdgeInsets.all(24),
      gridDelegate: const SliverGridDelegateWithMaxCrossAxisExtent(
        maxCrossAxisExtent: 280,
        childAspectRatio: 0.75,
        crossAxisSpacing: 20,
        mainAxisSpacing: 20,
      ),
      itemCount: medicines.length,
      itemBuilder: (context, index) {
        final medicine = medicines[index];
        final isOutOfStock = medicine.stockQuantity <= 0;

        return Container(
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
              onTap: isOutOfStock ? null : () => ref.read(cartProvider.notifier).addToCart(medicine),
              borderRadius: BorderRadius.circular(24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Container(
                      width: double.infinity,
                      decoration: BoxDecoration(
                        color: const Color(0xFFF1F5F9),
                        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
                      ),
                      child: Center(
                        child: Icon(
                          Icons.medication_rounded,
                          size: 48,
                          color: Colors.blueGrey.shade300,
                        ),
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          medicine.name,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Batch: ${medicine.batchNumber ?? "N/A"}',
                          style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 12),
                        ),
                        const SizedBox(height: 12),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              '\$${medicine.price.toStringAsFixed(2)}',
                              style: const TextStyle(
                                color: Color(0xFF0F172A),
                                fontWeight: FontWeight.bold,
                                fontSize: 18,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: isOutOfStock ? Colors.red.withValues(alpha: 0.1) : Colors.teal.withValues(alpha: 0.1),
                                borderRadius: BorderRadius.circular(8),
                              ),
                              child: Text(
                                isOutOfStock ? 'Empty' : '${medicine.stockQuantity}',
                                style: TextStyle(
                                  color: isOutOfStock ? Colors.red : Colors.teal.shade700,
                                  fontSize: 10,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildCartSummary(List<CartItem> cart, double totalAmount) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.all(24.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Current Order',
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, letterSpacing: -0.5),
              ),
              if (cart.isNotEmpty)
                TextButton(
                  onPressed: () => ref.read(cartProvider.notifier).clearCart(),
                  child: const Text('Clear', style: TextStyle(color: Colors.red)),
                ),
            ],
          ),
        ),
        Expanded(
          child: cart.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Container(
                        padding: const EdgeInsets.all(24),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF1F5F9),
                          shape: BoxShape.circle,
                        ),
                        child: Icon(Icons.shopping_basket_rounded, size: 48, color: Colors.blueGrey.shade200),
                      ),
                      const SizedBox(height: 16),
                      Text('Cart is empty', style: TextStyle(color: Colors.blueGrey.shade400, fontSize: 15)),
                    ],
                  ),
                )
              : ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  itemCount: cart.length,
                  separatorBuilder: (context, index) => const SizedBox(height: 12),
                  itemBuilder: (context, index) {
                    final item = cart[index];
                    return Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF8FAFC),
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Row(
                        children: [
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(item.medicine.name, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                                Text('\$${item.medicine.price.toStringAsFixed(2)}',
                                    style: TextStyle(color: Colors.blueGrey.shade500, fontSize: 12)),
                              ],
                            ),
                          ),
                          Container(
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(12),
                              border: Border.all(color: Colors.blueGrey.shade100),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.remove_rounded, size: 16),
                                  onPressed: () => ref.read(cartProvider.notifier).updateQuantity(item.medicine.id!, item.quantity - 1),
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                                ),
                                Text('${item.quantity}', style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                                IconButton(
                                  icon: const Icon(Icons.add_rounded, size: 16),
                                  onPressed: () => ref.read(cartProvider.notifier).updateQuantity(item.medicine.id!, item.quantity + 1),
                                  padding: EdgeInsets.zero,
                                  constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    );
                  },
                ),
        ),
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: const Color(0xFFF8FAFC),
            borderRadius: const BorderRadius.vertical(bottom: Radius.circular(32)),
          ),
          child: Column(
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text('Total Amount', style: TextStyle(fontSize: 14, color: Colors.blueGrey, fontWeight: FontWeight.w500)),
                  Text('\$${totalAmount.toStringAsFixed(2)}',
                      style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF0F172A))),
                ],
              ),
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 56,
                child: ElevatedButton(
                  onPressed: cart.isEmpty || _isProcessing ? null : _handleCheckout,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF0F172A),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                    elevation: 0,
                  ),
                  child: _isProcessing 
                    ? const SizedBox(height: 24, width: 24, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                    : const Text('Complete Order', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  void _handleCheckout() async {
    setState(() => _isProcessing = true);
    try {
      final cart = ref.read(cartProvider);
      final items = cart.map((e) => OrderItemRequest(
        medicineId: e.medicine.id!,
        quantity: e.quantity,
        batchNumber: e.medicine.batchNumber,
      )).toList();

      await ref.read(billingRepositoryProvider).createOrder(items);
      
      ref.read(cartProvider.notifier).clearCart();
      ref.invalidate(medicinesProvider);
      ref.invalidate(searchMedicinesProvider);
      
      if (!mounted) return;
      
      showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Lottie.network(
                'https://lottie.host/85633842-881c-43a0-80d8-f5424c148281/mK9C8gZz6d.json',
                height: 150,
                repeat: false,
              ),
              const Text(
                'Order Success!',
                style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              const Text('The transaction has been completed successfully.', textAlign: TextAlign.center),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('Great!'),
              ),
            ],
          ),
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
    } finally {
      if (mounted) setState(() => _isProcessing = false);
    }
  }

  void _showCartDrawer(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(24))),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        maxChildSize: 0.9,
        expand: false,
        builder: (context, scrollController) => Consumer(
          builder: (context, ref, child) {
            final cart = ref.watch(cartProvider);
            final totalAmount = ref.read(cartProvider.notifier).totalAmount;
            return _buildCartSummary(cart, totalAmount);
          },
        ),
      ),
    );
  }
}
