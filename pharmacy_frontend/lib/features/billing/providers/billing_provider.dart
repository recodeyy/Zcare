import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../medicine/models/medicine_model.dart';
import '../models/order_model.dart';
import '../repository/billing_repository.dart';

class CartItem {
  final MedicineResponse medicine;
  int quantity;

  CartItem({required this.medicine, this.quantity = 1});

  double get totalPrice => (medicine.price) * quantity;
}

class CartNotifier extends Notifier<List<CartItem>> {
  @override
  List<CartItem> build() {
    return [];
  }

  void addToCart(MedicineResponse medicine) {
    final existingIndex = state.indexWhere((item) => item.medicine.id == medicine.id);
    if (existingIndex >= 0) {
      final updatedCart = List<CartItem>.from(state);
      updatedCart[existingIndex].quantity++;
      state = updatedCart;
    } else {
      state = [...state, CartItem(medicine: medicine)];
    }
  }

  void removeFromCart(int medicineId) {
    state = state.where((item) => item.medicine.id != medicineId).toList();
  }

  void updateQuantity(int medicineId, int quantity) {
    if (quantity <= 0) {
      removeFromCart(medicineId);
      return;
    }
    state = [
      for (final item in state)
        if (item.medicine.id == medicineId)
          CartItem(medicine: item.medicine, quantity: quantity)
        else
          item,
    ];
  }

  void clearCart() {
    state = [];
  }

  double get totalAmount => state.fold<double>(0.0, (double sum, CartItem item) => sum + item.totalPrice);
}

final cartProvider = NotifierProvider<CartNotifier, List<CartItem>>(() {
  return CartNotifier();
});

final ordersProvider = FutureProvider<List<CustomerOrderResponse>>((ref) async {
  final repository = ref.watch(billingRepositoryProvider);
  return await repository.getAllOrders();
});
