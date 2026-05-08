import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_client.dart';
import '../models/order_model.dart';

final billingRepositoryProvider = Provider<BillingRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return BillingRepository(dio: dio);
});

class BillingRepository {
  final Dio dio;

  BillingRepository({required this.dio});

  Future<CustomerOrderResponse> createOrder(List<OrderItemRequest> items) async {
    final response = await dio.post('/orders', data: items.map((e) => e.toJson()).toList());
    return CustomerOrderResponse.fromJson(response.data);
  }

  Future<List<CustomerOrderResponse>> getAllOrders() async {
    final response = await dio.get('/orders');
    return (response.data as List)
        .map((e) => CustomerOrderResponse.fromJson(e))
        .toList();
  }

  Future<CustomerOrderResponse> getOrderById(int id) async {
    final response = await dio.get('/orders/$id');
    return CustomerOrderResponse.fromJson(response.data);
  }
}
